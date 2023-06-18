package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.annotation.SuppressLint
import android.content.ContentValues
import android.content.Context
import android.provider.MediaStore
import android.util.Log
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.core.Preview.SurfaceProvider
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.video.*
import androidx.core.content.ContextCompat
import androidx.core.util.Consumer
import androidx.lifecycle.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.util.VideoMetaDataExtractor
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*
import javax.inject.Inject

data class ShootVideoUiStatus(
    val buttonIcon: Int = R.drawable.baseline_play_circle_filled_44,
    val isRecording: Boolean = false
)

@HiltViewModel
class ShootVideoViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository
) : ViewModel() {

    private val _uiState = MutableLiveData(ShootVideoUiStatus())
    val uiState: LiveData<ShootVideoUiStatus>
        get() = _uiState

    private lateinit var cameraExecutor: ExecutorService
    private lateinit var videoCapture: VideoCapture<Recorder>
    private lateinit var recording: Recording

    fun startCamera(
        context: Context,
        lifecycleOwner: LifecycleOwner,
        surfaceProvider: SurfaceProvider) {
        val qualitySelector = QualitySelector.fromOrderedList(
            listOf(Quality.UHD, Quality.FHD, Quality.HD, Quality.SD),
            FallbackStrategy.lowerQualityOrHigherThan(Quality.SD))

        cameraExecutor = Executors.newSingleThreadExecutor()

        val recorder = Recorder.Builder()
            .setExecutor(cameraExecutor)
            .setQualitySelector(qualitySelector).build()
        videoCapture = VideoCapture.withOutput(recorder)

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(surfaceProvider)

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    lifecycleOwner,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    videoCapture
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("MissingPermission")
    fun startRecording(context: Context, onCompleteListener: OnCompleteListener) {
        _uiState.value = _uiState.value?.copy(
            buttonIcon = R.drawable.baseline_stop_circle_44,
            isRecording = true
        )

        val name = "${Date().time}_Arkkt.mp4"
        val contentValues = ContentValues().apply {
            put(MediaStore.Video.Media.DISPLAY_NAME, name)
        }
        val outputOptions = MediaStoreOutputOptions.Builder(
            context.contentResolver, MediaStore.Video.Media.EXTERNAL_CONTENT_URI
        ).setContentValues(contentValues).build()

        // 2. Configure Recorder and Start recording to the mediaStoreOutput.
        recording = videoCapture.output
            .prepareRecording(context, outputOptions)
            .withAudioEnabled()
            .start(cameraExecutor, videoRecordEvent(context, onCompleteListener))
    }

    private fun videoRecordEvent(context: Context, onCompleteListener: OnCompleteListener) =
        Consumer<VideoRecordEvent> {
            if (it is VideoRecordEvent.Start) {
                // Handle the start of a new active recording
            } else if (it is VideoRecordEvent.Finalize) {
                val finalizeEvent = it
                // Handles a finalize event for the active recording, checking Finalize.getError()d
                val error = finalizeEvent.error
                if (error == VideoRecordEvent.Finalize.ERROR_NONE) {
                    val uri = finalizeEvent.outputResults.outputUri
                    Log.d("test", uri.path!!)
                    val videoInfo = VideoMetaDataExtractor.extract(context, uri)
                    gaitAnalysisRepository.setVideoInfo(videoInfo)

                    viewModelScope.launch(Dispatchers.Main) {
                        onCompleteListener.onComplete(GaitAnalysisFragments.ShootVideo)
                    }
                }
            }

            // All events, including VideoRecordEvent.Status, contain RecordingStats.
            // This can be used to update the UI or track the recording duration.
            val recordingStats = it.recordingStats
            recordingStats.recordedDurationNanos
        }

    fun stopRecording() {
        recording.stop()
        _uiState.value = _uiState.value?.copy(
            buttonIcon = R.drawable.baseline_play_circle_filled_44,
            isRecording = false
        )
    }
}