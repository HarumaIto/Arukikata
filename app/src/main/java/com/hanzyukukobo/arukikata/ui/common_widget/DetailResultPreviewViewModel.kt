package com.hanzyukukobo.arukikata.ui.common_widget

import android.annotation.SuppressLint
import android.app.Application
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Environment
import android.view.View
import android.widget.TextView
import androidx.activity.result.ActivityResultLauncher
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.github.mikephil.charting.charts.LineChart
import com.google.android.material.snackbar.Snackbar
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle
import com.hanzyukukobo.arukikata.data.Score
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import com.hanzyukukobo.arukikata.databinding.FragmentDetailResultPreviewBinding
import com.hanzyukukobo.arukikata.domain.GetFramesChartDataUseCase
import com.hanzyukukobo.arukikata.domain.GetLogChartDataUseCase
import com.hanzyukukobo.arukikata.util.chart.HorizontalBarChartBuilder
import com.hanzyukukobo.arukikata.util.chart.LineChartBuilder
import dagger.hilt.android.lifecycle.HiltViewModel
import dagger.hilt.android.qualifiers.ActivityContext
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.*
import java.util.*
import javax.inject.Inject

data class DetailResultPreviewUiState(
    val saveLogButtonEnable: Boolean = true,
    val csvLogButtonEnable: Boolean = true,
    val videoExportButtonEnable: Boolean = true
)

@HiltViewModel
class DetailResultPreviewViewModel @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val getFramesChartDataUseCase: GetFramesChartDataUseCase,
    private val getLogChartDataUseCase: GetLogChartDataUseCase
) : ViewModel(), ScorePreview {

    private val _uiState = MutableLiveData(DetailResultPreviewUiState())
    val uiState: LiveData<DetailResultPreviewUiState>
        get() = _uiState

    private lateinit var binding: FragmentDetailResultPreviewBinding

    fun setBinding(_binding: FragmentDetailResultPreviewBinding) {
        binding = _binding
    }

    override fun buildCharts(
        context: Context,
        itemName: String,
    ) {
        if (itemName.isNotEmpty()) {
            // 履歴画面で開かれたとき、ボタンをロックする
            _uiState.value = _uiState.value?.copy(
                saveLogButtonEnable = false,
                csvLogButtonEnable = false,
                videoExportButtonEnable = false
            )
        }

        viewModelScope.launch {
            val scores = mutableListOf<Score>()

            for (landmark in JointLandmark.LEFT_HIP .. JointLandmark.RIGHT_ANKLE) {
                val chartForData = if (itemName.isEmpty()) {
                    getFramesChartDataUseCase(context, landmark== JointLandmark.LEFT_HIP, landmark)
                } else {
                    getLogChartDataUseCase(context, itemName, landmark== JointLandmark.LEFT_HIP, landmark)
                }

                buildLineCharts(landmark, chartForData[0], chartForData[1], chartForData[2], chartForData[3])
                val differences = calculateDifference(chartForData[1], chartForData[3])
                scores.add(calculateScore(differences))
            }

            buildBarChart(scores)
        }
    }

    private suspend fun buildLineCharts(
        landmark: Int, x0: List<Double>, y0: List<Double>, x1: List<Double>, y1: List<Double>) {
        withContext(Dispatchers.Main) {
            val chart = getLineChart(landmark)
            chart.apply {
                invalidate()
                clear()
            }

            LineChartBuilder(chart, x0, y0, x1, y1).apply {
                chartDescriptionName = getChartName(landmark)
                build()
            }
        }
    }

    private suspend fun buildBarChart(scores: MutableList<Score>) {
        val xAxisList = JointLandmark.allLandmarks().filter { it > 5 }.map { it - 6 }.toMutableList()
        xAxisList.add(xAxisList.size) //最大値より+1大きい数値を入力したいから
        val allScore = scores.let {
            val values = mutableListOf<Double>()
            scores.forEach { values.add(it.value) }
            scores.add(calculateScore(values, false))
            scores.last()
        }
        withContext(Dispatchers.Main) {
            setAllScore(allScore)
            HorizontalBarChartBuilder(
                binding.jointsDifferenceChart,
                xAxisList,
                scores
            ).build()
        }
    }

    private fun getLineChart(landmark: Int): LineChart {
        return when(landmark) {
            JointLandmark.LEFT_HIP -> binding.leftHipAngleChart
            JointLandmark.RIGHT_HIP -> binding.rightHipAngleChart
            JointLandmark.LEFT_KNEE -> binding.leftKneeAngleChart
            JointLandmark.RIGHT_KNEE -> binding.rightKneeAngleChart
            JointLandmark.LEFT_ANKLE -> binding.leftAnkleAngleChart
            JointLandmark.RIGHT_ANKLE -> binding.rightAnkleAngleChart
            else -> binding.leftHipAngleChart
        }
    }

    private fun getChartName(landmark: Int): String {
        return when(landmark) {
            JointLandmark.LEFT_HIP -> "Left hip"
            JointLandmark.RIGHT_HIP -> "Right hip"
            JointLandmark.LEFT_KNEE -> "Left knee"
            JointLandmark.RIGHT_KNEE -> "Right knee"
            JointLandmark.LEFT_ANKLE -> "Left ankle"
            JointLandmark.RIGHT_ANKLE -> "Right ankle"
            else -> "Left hip"
        }
    }

    @SuppressLint("SetTextI18n")
    private fun setAllScore(score: Score) {
        binding.allScoreText.text = "${getSumScore(score)}±${getStandardDeviation(score)}点"
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