package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.media.MediaMetadataRetriever
import android.os.Build
import android.util.Log
import android.view.View
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.android.material.snackbar.Snackbar
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.hanzyukukobo.arukikata.data.FrameEntity
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.data.JointsAngle
import com.hanzyukukobo.arukikata.data.VideoInfo
import com.hanzyukukobo.arukikata.util.BitmapToVideoEncoder
import com.hanzyukukobo.arukikata.util.ml.PoseCalculator
import com.hanzyukukobo.arukikata.util.ml.PoseDetectorProcessor
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.kotlin.subscribeBy
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import javax.inject.Inject
import kotlin.math.roundToInt

data class GaitAnalyzerUiState(
    val isStartButtonEnable: Boolean = true,
    val circleProgress: Int = 0,
    val progressText: String = "0%",
)

@HiltViewModel
class GaitAnalyzerViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository
)  : ViewModel() {

    private val poseProcessor: PoseDetectorProcessor

    private var needUpDataSourceInfo: Boolean = true

    private val _uiState = MutableLiveData(GaitAnalyzerUiState())
    val uiState: LiveData<GaitAnalyzerUiState>
        get() = _uiState

    init {
        poseProcessor = PoseDetectorProcessor(
            PoseDetectorOptions.SINGLE_IMAGE_MODE,
            showUpperAngleText = true,
            showLowerAngleText = true)
    }

    @SuppressLint("CheckResult")
    fun processVideoFrames(context: Context, view: View, onCompleteListener: OnCompleteListener) {
        val videoInfo: VideoInfo? = gaitAnalysisRepository.getVideoInfo()
        if (videoInfo == null) {
            Snackbar.make(view, "分析する動画をセットしてください", Snackbar.LENGTH_SHORT).show()
            return
        }
        needUpDataSourceInfo = true
        val timePerFrame = (videoInfo.duration.toFloat()/1000000) / videoInfo.frameCount.toFloat()

        _uiState.value = _uiState.value?.copy(
            isStartButtonEnable = false,
            circleProgress = 10,
            progressText = "10%"
        )

        val bitmapToVideoEncoder = buildVideoEncoder(videoInfo, onCompleteListener)

        Log.d("test", "framecount:${videoInfo.frameCount}")
        // 新しいスレッドでコルーチンを実行する
        viewModelScope.launch(Dispatchers.Default) {
            gaitAnalysisRepository.deleteAllFrame()

            for (i in 0 until videoInfo.frameCount) {
                Log.d("test", "current:$i")
                val bitmap = extractVideoFrames(context, videoInfo, i)

                if (bitmap == null) {
                    Log.d("test", "error handring")
                    continue
                    /*
                    // 何らかの影響で最後のフレームが取得できなかった場合
                    withContext(Dispatchers.Main) {
                        updateProgressValues(100f)
                        bitmapToVideoEncoder.stopEncoding()
                    }
                    return@launch
                     */
                }

                poseProcessor.asyncProcessBitmap(bitmap)
                    .subscribeOn(AndroidSchedulers.mainThread())
                    .subscribeBy (
                        onNext = {
                            val jointsAngle = PoseCalculator.createJointsAngle(it)
                            val resultBitmap = poseProcessor.paintPoseResult(
                                needUpDataSourceInfo, videoInfo, bitmap, it, jointsAngle)

                            insertFrame(i, timePerFrame * i.toFloat(), jointsAngle)
                            bitmapToVideoEncoder.queueFrame(resultBitmap)

                            // 最後のフレームの処理が完了したらtrueでonCompleteが呼ばれる
                            if (i==videoInfo.frameCount-1) {
                                Log.d("test", "endcount:$i")
                                updateProgressValues(100f)
                                bitmapToVideoEncoder.stopEncoding()
                            } else {
                                updateProgressValues(i.toFloat()/videoInfo.frameCount.toFloat() * 90 + 10)
                            }
                        },
                        onError = {
                            it.printStackTrace()
                        },
                    )

                // Poseを描画するための変数を初期化する必要があるから
                if (needUpDataSourceInfo) needUpDataSourceInfo = false
            }
        }
    }

    private fun buildVideoEncoder(videoInfo: VideoInfo, onCompleteListener: OnCompleteListener): BitmapToVideoEncoder {
        val frameRate = (videoInfo.frameCount.toFloat() / (videoInfo.duration.toFloat()/1000000)).roundToInt()
        return BitmapToVideoEncoder({
            // DispatchersをMainにしてメインスレッドで実行させる
            // もともとこのCallback処理は非同期なスレッドから呼ばれているからそのままだとエラーが発生する
            viewModelScope.launch(Dispatchers.Main) {
                onCompleteListener.onComplete(GaitAnalysisFragments.GaitAnalyzer)
            }
        }, frameRate).apply {
            if (videoInfo.rotation == 0 || videoInfo.rotation == 180) {
                startEncoding(videoInfo.width, videoInfo.height, videoInfo.resultVideoFile)
            } else {
                startEncoding(videoInfo.height, videoInfo.width, videoInfo.resultVideoFile)
            }
        }
    }

    // すべてのフレームを取得する
    private fun extractVideoFrames(context:Context, videoInfo: VideoInfo, frameIndex: Int): Bitmap? {
        val retriever = MediaMetadataRetriever().apply {
            setDataSource(context, videoInfo.uri)
        }

        return if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            val bitmapParams = MediaMetadataRetriever.BitmapParams()
            bitmapParams.preferredConfig = Bitmap.Config.ARGB_8888
            try {
                retriever.getFrameAtIndex(frameIndex, bitmapParams)
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
                null
            }
        } else {
            TODO("VERSION.SDK_INT < P")
        }
    }

    private fun insertFrame(index: Int, currentTime: Float, jointsAngle: JointsAngle) {
        val frameEntity = FrameEntity(index, currentTime, jointsAngle)
        viewModelScope.launch {
            gaitAnalysisRepository.insertFrame(frameEntity)
        }
    }

    private fun updateProgressValues(value: Float) {
        _uiState.value = _uiState.value?.copy(
            circleProgress = value.toInt(),
            progressText = "${String.format("%.1f", value)}%"
        )
    }

    fun resetPrivateValues() {
        _uiState.value = _uiState.value?.copy(
            isStartButtonEnable = true,
            circleProgress = 0,
            progressText = "0%",
        )
    }
}