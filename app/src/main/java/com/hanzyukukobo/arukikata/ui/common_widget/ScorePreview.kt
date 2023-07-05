package com.hanzyukukobo.arukikata.ui.common_widget

import android.content.Context
import com.hanzyukukobo.arukikata.data.Score
import com.hanzyukukobo.arukikata.domain.GetFramesChartDataUseCase
import com.hanzyukukobo.arukikata.domain.GetLogChartDataUseCase
import kotlin.math.abs
import kotlin.math.pow
import kotlin.math.roundToInt
import kotlin.math.sqrt

interface ScorePreview {
    fun buildCharts(context: Context, itemName: String)

    private fun normalization(value: Double, max: Int) = value / max * 100

    fun calculateDifference(y0: List<Double>, y1: List<Double>): List<Double> {
        val differences = mutableListOf<Double>()
        for (i in y0.indices) {
            differences.add(abs(y0[i] - y1[i]))
        }
        return differences
    }

    fun calculateScore(numArray: List<Double>, isJointAngleScore: Boolean = true): Score {
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

    // 合計点を計算する
    fun getSumScore(score: Score): String =
        "${score.value.roundToInt()}"

    // 標準偏差を計算する
    fun getStandardDeviation(score: Score): String =
        String.format("%.2f", score.standardDeviation)
}