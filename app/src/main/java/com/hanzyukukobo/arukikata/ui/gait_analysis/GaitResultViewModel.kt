package com.hanzyukukobo.arukikata.ui.gait_analysis

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.data.*
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.databinding.FragmentGaitResultBinding
import com.hanzyukukobo.arukikata.domain.BuildVideoPlayerUseCase
import com.hanzyukukobo.arukikata.domain.GetFramesChartDataUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.*
import java.io.*
import java.util.*
import javax.inject.Inject

data class GaitResultUiState(
    val saveLogButtonEnable: Boolean = true
)

@HiltViewModel
class GaitResultViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val buildVideoPlayerUseCase: BuildVideoPlayerUseCase,
    private val getFramesChartDataUseCase: GetFramesChartDataUseCase
) : ViewModel() {

    private val _uiState = MutableLiveData(GaitResultUiState())
    val uiState: LiveData<GaitResultUiState>
        get() = _uiState

    private var player: ExoPlayer? = null

    fun buildPlayer(context: Context, playerView: PlayerView) {
        player = buildVideoPlayerUseCase(context, playerView, true)
    }

    fun buildCharts(binding: FragmentGaitResultBinding) {
        viewModelScope.launch {
            binding.scoreChartsPreview.buildCharts(
                "", getFramesChartDataUseCase, getLogChartDataUseCase = null)
        }
    }

    fun releasePlayer() {
        player?.release()
        player = null
    }

    fun canBuildResult(): VideoInfo? {
        return gaitAnalysisRepository.getVideoInfo()
    }

    fun createDocumentFile(launcher: ActivityResultLauncher<Intent>) {
        val intent = Intent(Intent.ACTION_CREATE_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "application/csv"
            putExtra(Intent.EXTRA_TITLE, "jointAngles_Arkkt.csv")
        }
        launcher.launch(Intent.createChooser(intent, "Output CSV File"))
    }

    fun copyResultVideo(fileName: String = "${Date().time}_Arkkt") {
        val directory = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_MOVIES)
        val file = File(directory, "$fileName.mp4")

        val videoInfo = gaitAnalysisRepository.getVideoInfo()!!
        videoInfo.resultVideoFile.copyTo(file)
    }

    fun saveLocalDatabase(name: String = "") {
        viewModelScope.launch {
            val logs: List<AnalysisLogEntity>? = try {
                // データがないとエラーを吐くので回収する
                gaitAnalysisRepository.getAllLogs()
            } catch (e: java.lang.IllegalStateException) {
                e.printStackTrace()
                null
            }
            val id: Int = if (logs != null && logs.isNotEmpty()) {
                logs.last().id + 1
            } else {
                1
            }

            gaitAnalysisRepository.insertLog(
                AnalysisLogEntity(
                    id,
                    name.ifEmpty { "log($id)" },
                    Date(),
                    gaitAnalysisRepository.getAllFrame()
                )
            )

            withContext(Dispatchers.Main) {
                _uiState.value = _uiState.value?.copy(
                    saveLogButtonEnable = false
                )
            }
        }
    }

    /**
     * @param
     * context: Context,
     * writtenDataList: List<Map<String, Any>> このパラメータの配列の構造を下記に記す
     * [{time : 0.0s // Float}
     *  {jointsAngle :
     *   [ // JointsAngle
     *    180, // LEFT_SHOULDER
     *    180, // RIGHT_SHOULDER
     *    ...
     *   ]
     *  }
     * ]
     */
    fun exportCSVFile(uri: Uri, context: Context, view: View) {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                val fileDescriptor = context.contentResolver.openFileDescriptor(uri, "w")
                val outputStream = FileOutputStream(fileDescriptor!!.fileDescriptor)
                val outputStreamWriter = OutputStreamWriter(outputStream, "UTF-8")

                val printWriter = PrintWriter(outputStreamWriter)
                printWriter.print("時間")
                printWriter.print(",")
                printWriter.print("左肩")
                printWriter.print(",")
                printWriter.print("右肩")
                printWriter.print(",")
                printWriter.print("左ひじ")
                printWriter.print(",")
                printWriter.print("右ひじ")
                printWriter.print(",")
                printWriter.print("左手首")
                printWriter.print(",")
                printWriter.print("右手首")
                printWriter.print(",")
                printWriter.print("左股関節")
                printWriter.print(",")
                printWriter.print("右股関節")
                printWriter.print(",")
                printWriter.print("左ひざ")
                printWriter.print(",")
                printWriter.print("右ひざ")
                printWriter.print(",")
                printWriter.print("左足首")
                printWriter.print(",")
                printWriter.print("右足首")
                printWriter.println()

                val frames = gaitAnalysisRepository.getAllFrame()
                for (frame in frames) {
                    // mapのkeyをどっかに定義したい
                    printWriter.print(frame.currentTime)
                    printWriter.print(",")

                    val jointsAngle: JointsAngle = frame.jointsAngle
                    for (joint in jointsAngle.getAllJointLandmark()) {
                        printWriter.print(joint.angle)
                        if (joint.landmark == JointLandmark.RIGHT_ANKLE) {
                            printWriter.println()
                        } else {
                            printWriter.print(",")
                        }
                    }
                }

                printWriter.close()
                outputStreamWriter.close()
                outputStream.close()
                fileDescriptor.close()

                Snackbar.make(view, "ダウンロードが完了しました", Snackbar.LENGTH_SHORT).show()
            } catch (e: IOException) {
                e.printStackTrace()
            }
        }
    }
}