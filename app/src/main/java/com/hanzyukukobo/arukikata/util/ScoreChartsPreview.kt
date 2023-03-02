package com.hanzyukukobo.arukikata.util

import android.content.Context
import android.util.AttributeSet
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.github.mikephil.charting.charts.LineChart
import com.hanzyukukobo.arukikata.R
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.Score
import com.hanzyukukobo.arukikata.domain.GetFramesChartDataUseCase
import com.hanzyukukobo.arukikata.domain.GetLogChartDataUseCase
import com.hanzyukukobo.arukikata.util.chart.HorizontalBarChartBuilder
import com.hanzyukukobo.arukikata.util.chart.LineChartBuilder
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.*
import javax.inject.Inject
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

class ScoreChartsPreview constructor(
    context: Context,
    attrs: AttributeSet?
) : ConstraintLayout(context, attrs) {

    init {
        inflate(context, R.layout.score_preview, this)
    }

    suspend fun buildCharts(
        itemName: String,
        getFramesChartDataUseCase: GetFramesChartDataUseCase?,
        getLogChartDataUseCase: GetLogChartDataUseCase?,
    ) {
        val scores = mutableListOf<Score>()

        for (landmark in JointLandmark.LEFT_HIP .. JointLandmark.RIGHT_ANKLE) {
            val chartForData = if (itemName.isEmpty() || getFramesChartDataUseCase != null) {
                getFramesChartDataUseCase!!(context, landmark== JointLandmark.LEFT_HIP, landmark)
            } else {
                getLogChartDataUseCase!!(context, itemName, landmark== JointLandmark.LEFT_HIP, landmark)
            }

            buildLineCharts(landmark, chartForData[0], chartForData[1], chartForData[2], chartForData[3])
            val differences = calculateDifference(chartForData[1], chartForData[3])
            scores.add(calculateScore(differences))
        }

        buildBarChart(scores)
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
        scores.apply {
            val values = mutableListOf<Double>()
            scores.forEach { values.add(it.value) }
            add(calculateScore(values, false))
        }
        withContext(Dispatchers.Main) {
            setAllScore(scores.last())
            HorizontalBarChartBuilder(
                findViewById(R.id.jointsDifferenceChart),
                xAxisList,
                scores
            ).build()
        }
    }

    private fun getLineChart(landmark: Int): LineChart {
        return when(landmark) {
            JointLandmark.LEFT_HIP -> findViewById(R.id.leftHipAngleChart)
            JointLandmark.RIGHT_HIP -> findViewById(R.id.rightHipAngleChart)
            JointLandmark.LEFT_KNEE -> findViewById(R.id.leftKneeAngleChart)
            JointLandmark.RIGHT_KNEE -> findViewById(R.id.rightKneeAngleChart)
            JointLandmark.LEFT_ANKLE -> findViewById(R.id.leftAnkleAngleChart)
            JointLandmark.RIGHT_ANKLE -> findViewById(R.id.rightAnkleAngleChart)
            else -> findViewById(R.id.leftHipAngleChart)
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

    private fun normalization(value: Double, max: Int) = value / max * 100

    private fun calculateDifference(y0: List<Double>, y1: List<Double>): List<Double> {
        val differences = mutableListOf<Double>()
        for (i in y0.indices) {
            differences.add(abs(y0[i] - y1[i]))
        }
        return differences
    }

    private fun calculateScore(numArray: List<Double>, isJointAngleScore: Boolean = true): Score {
        val average = numArray.average()
        val standardDeviation = calculateStandardDeviation(average, numArray)

        if (!isJointAngleScore) return Score(average, standardDeviation)
        val jointAngleScore = 180 - average
        return Score(normalization(jointAngleScore, 180), standardDeviation)
    }

    private fun calculateStandardDeviation(average: Double, numArray: List<Double>): Double {
        var deviation = 0.0

        for (num in numArray) {
            deviation += (num - average).pow(2.0)
        }
        val dispersion = deviation/numArray.size

        return sqrt(dispersion)
    }

    private fun setAllScore(score: Score) {
        val allScoreText = findViewById<TextView>(R.id.allScoreText)
        allScoreText.text = "${score.value.roundToInt()}±${String.format("%.2f", score.standardDeviation)}点"
    }
}