package com.hanzyukukobo.arukikata.ui.realtime_analysis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import androidx.core.view.drawToBitmap
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.common.MlKitException
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.PartialAngle
import com.hanzyukukobo.arukikata.databinding.ActivityCameraPreviewBinding
import com.hanzyukukobo.arukikata.util.BitmapToVideoEncoder
import com.hanzyukukobo.arukikata.util.ml.FpsTimer
import com.hanzyukukobo.arukikata.util.ml.PoseDetectorProcessor
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.io.File
import java.util.*
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

data class CameraPreviewUiState(
    val upperAngleVisible: Boolean = false,
    val lowerAngleVisible: Boolean = false,
    val fpsText: String = "fps",
    val detectionState: Boolean = true,
    val recordStateVisibility: Int = View.INVISIBLE,
    val recordButtonSrc: Int = R.drawable.baseline_play_circle_filled_44,
)

class CameraPreviewViewModel : ViewModel() {

    private val _uiState = MutableLiveData(CameraPreviewUiState())
    val uiState: LiveData<CameraPreviewUiState>
        get() = _uiState

    private lateinit var cameraExecutor: ExecutorService

    private var poseProcessor: PoseDetectorProcessor? = null
    private val detectorMode = PoseDetectorOptions.STREAM_MODE
    private var needUpdateImageSourceInfo: Boolean = true

    private val fpsTimer = FpsTimer()

    private var bitmapToVideoEncoder: BitmapToVideoEncoder? = null
    val isRecording: Boolean
        get() {
            return if (bitmapToVideoEncoder != null) {
                try {
                    bitmapToVideoEncoder!!.isEncodingStarted
                } catch (e: UninitializedPropertyAccessException) {
                    false
                }
            } else {
                false
            }
        }
    private var videoWidth = 720
    private var videoHeight = 1600

    fun startCamera(context: Context, binding: ActivityCameraPreviewBinding) {
        val previewView = binding.previewView
        cameraExecutor = Executors.newSingleThreadExecutor()
        Log.i("camera preview view model", "width: ${previewView.width}, height: ${previewView.height}")

        val cameraProviderFuture = ProcessCameraProvider.getInstance(context)
        cameraProviderFuture.addListener({
            try {
                val cameraProvider = cameraProviderFuture.get()

                val preview = Preview.Builder().build()
                preview.setSurfaceProvider(previewView.surfaceProvider)

                val cameraSelector = CameraSelector.DEFAULT_BACK_CAMERA

                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    binding.lifecycleOwner!!,
                    cameraSelector,
                    preview,
                )

                buildAnalysis(binding, cameraProvider, preview, cameraSelector)
            } catch (e: Exception) {
                Log.e("RealtimeActivity", e.message.toString())
            }
        }, ContextCompat.getMainExecutor(context))
    }

    @SuppressLint("UnsafeOptInUsageError")
    fun buildAnalysis(
        binding: ActivityCameraPreviewBinding,
        cameraProvider: ProcessCameraProvider,
        preview: Preview,
        cameraSelector: CameraSelector
    ) {
        val graphicOverlay = binding.graphicOverlay
        fpsTimer.setTimer()

        poseProcessor = PoseDetectorProcessor(
            detectorMode,
            uiState.value!!.upperAngleVisible,
            uiState.value!!.lowerAngleVisible
        )

        val imageAnalysis = ImageAnalysis.Builder().build()
        imageAnalysis.setAnalyzer(
            cameraExecutor
        ) {
            if (it.image != null) {
                try {
                    val isDetection = uiState.value!!.detectionState
                    if (isDetection) {
                        poseProcessor!!.processImageProxy(it, graphicOverlay)
                    } else {
                        poseProcessor!!.postGraphicOverlay(it, graphicOverlay)
                    }
                    if (needUpdateImageSourceInfo) {
                        val rotationDegrees = it.imageInfo.rotationDegrees
                        if (rotationDegrees == 0 || rotationDegrees == 180) {
                            graphicOverlay.setImageSourceInfo(it.width, it.height, false)
                        } else {
                            graphicOverlay.setImageSourceInfo(it.height, it.width, false)
                        }
                        needUpdateImageSourceInfo = false
                    }
                    updateFps()
                    if (isRecording) insertBitmap(binding, isDetection)
                } catch (e: MlKitException) {
                    Log.e(
                        "RealtimeActivity",
                        "Failed to process image. Error: " + e.localizedMessage
                    )
                }
            }
        }
        cameraProvider.unbindAll()

        cameraProvider.bindToLifecycle(
            binding.lifecycleOwner!!,
            cameraSelector,
            preview,
            imageAnalysis
        )
    }

    fun remakePoseProcessor() {
        poseProcessor = PoseDetectorProcessor(
            detectorMode,
            uiState.value!!.upperAngleVisible,
            uiState.value!!.lowerAngleVisible
        )
    }

    fun changeAngleVisibility(view: View) {
        // ここの変化の結果をActivityで監視して、PoseDetectorを再生成する
        _uiState.value = _uiState.value?.let {
            it.copy(
                upperAngleVisible = !it.upperAngleVisible,
                lowerAngleVisible = !it.lowerAngleVisible
            )
        }
        if (uiState.value!!.upperAngleVisible && uiState.value!!.lowerAngleVisible) {
            Snackbar.make(view, "関節角度を表示", Toast.LENGTH_SHORT).show()
        } else if (!uiState.value!!.upperAngleVisible && !uiState.value!!.lowerAngleVisible) {
            Snackbar.make(view, "関節角度を非表示", Toast.LENGTH_SHORT).show()
        }
    }

    fun changeDetection(view: View?) {
        _uiState.value = _uiState.value?.let {
            it.copy(detectionState = !it.detectionState)
        }
        if (view != null) {
            if (_uiState.value!!.detectionState) {
                Snackbar.make(view, "検出を有効化", Snackbar.LENGTH_SHORT).show()
            } else {
                Snackbar.make(view, "検出を無効化", Snackbar.LENGTH_SHORT).show()
            }
        }
    }

    fun changePartialAngleVisibility(partial: PartialAngle, view: View): Boolean {
        var visible: Boolean? = null
        var snackBarText = ""
        _uiState.value = _uiState.value?.let {
            when (partial) {
                PartialAngle.UPPER_HALF_BODY -> {
                    visible = !it.upperAngleVisible
                    snackBarText = "上半身の角度を" + if (visible!!) "表示" else "非表示"
                    it.copy(upperAngleVisible = visible!!)
                }
                PartialAngle.LOWER_HALF_BODY -> {
                    visible = !it.lowerAngleVisible
                    snackBarText = "下半身の角度を" + if (visible!!) "表示" else "非表示"
                    it.copy(lowerAngleVisible = visible!!)
                }
            }
        }
        Snackbar.make(
            view,
            snackBarText,
            Snackbar.LENGTH_SHORT
        ).show()
        return visible!!
    }

    private fun updateFps() {
        val fpsNum = fpsTimer.calculateFps()
        if (fpsNum != null) {
            viewModelScope.launch(Dispatchers.Main) {
                _uiState.value = _uiState.value?.copy(
                    fpsText = "$fpsNum fps"
                )
            }
        }
    }

    fun startRecording(width: Int, height: Int, view: View) {
        bitmapToVideoEncoder = BitmapToVideoEncoder({
            // callback
            viewModelScope.launch(Dispatchers.Main) {
                Snackbar.make(view, "撮影した動画を保存しました", Snackbar.LENGTH_SHORT).show()
                _uiState.value = _uiState.value?.copy(
                    recordButtonSrc = R.drawable.baseline_play_circle_filled_44,
                    recordStateVisibility = View.INVISIBLE
                )
            }
        }, fpsTimer.latestFps)

        _uiState.value = _uiState.value?.copy(
            recordButtonSrc = R.drawable.baseline_stop_circle_44,
            recordStateVisibility = View.VISIBLE
        )

        val name = "${Date().time}_Arkkt.mp4"
        val path = "/storage/emulated/0/Movies/$name"
        val file = File(path)
        try {
            // サイズが640*480じゃないと落ちる
            // 原因は以下を出力しているところにあると思うが見つけられない
            // D/skia: fStrides[0]:640, fStrides[1]:640, width: 640, height:480
            bitmapToVideoEncoder!!.startEncoding(videoWidth, videoHeight, file)
        } catch (e: java.lang.IllegalArgumentException) {
            e.printStackTrace()
        }
    }

    private fun insertBitmap(binding: ActivityCameraPreviewBinding, isOverlay: Boolean) {
        val previewBitmap = binding.previewView.drawToBitmap()
        val resultBitmap: Bitmap = if (isOverlay) {
            val overlayBitmap = binding.graphicOverlay.drawToBitmap()

            // リサイズ
            val resizeScale: Double = if (overlayBitmap.width >= overlayBitmap.height) {
                videoWidth.toDouble() / overlayBitmap.width
            } else {
                videoHeight.toDouble() / overlayBitmap.height
            }
            val resizedOverlayBitmap = Bitmap.createScaledBitmap(
                overlayBitmap,
                (overlayBitmap.width * resizeScale).toInt(),
                (overlayBitmap.height * resizeScale).toInt(),
                true)
            val resizedPreviewBitmap = Bitmap.createScaledBitmap(
                previewBitmap,
                (previewBitmap.width * resizeScale).toInt(),
                (previewBitmap.height * resizeScale).toInt(),
                true,)

            // startRecording時のエラーと同じ
            val newBitmap = Bitmap.createBitmap(
                videoWidth,
                videoHeight,
                Bitmap.Config.ARGB_8888
            )
            val canvas = Canvas(newBitmap)
            canvas.apply {
                drawBitmap(resizedPreviewBitmap, 0f, 0f, Paint())
                drawBitmap(resizedOverlayBitmap, 0f, 0f, Paint())
            }
            newBitmap
        } else {
            previewBitmap
        }
        bitmapToVideoEncoder?.queueFrame(resultBitmap)
    }

    fun stopRecording() {
        // Start時に指定したCallbackメソッドが呼ばれる
        bitmapToVideoEncoder?.stopEncoding()
    }

    fun release() {
        cameraExecutor.shutdown()
        poseProcessor?.stop()
        fpsTimer.resetFpsStatus()
    }
}