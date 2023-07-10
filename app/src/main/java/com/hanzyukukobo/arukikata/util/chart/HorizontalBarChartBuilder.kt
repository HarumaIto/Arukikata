package com.hanzyukukobo.arukikata.util.chart

import android.app.Application
import android.content.res.Resources.Theme
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import com.github.mikephil.charting.charts.HorizontalBarChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.components.YAxis
import com.github.mikephil.charting.data.BarData
import com.github.mikephil.charting.data.BarDataSet
import com.github.mikephil.charting.data.BarEntry
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet
import com.hanzyukukobo.arukikata.data.Score

class HorizontalBarChartBuilder(
    private val barChart: HorizontalBarChart,
    private val xAxisValues: List<Int>, // index
    private val yAxisValues: List<Score>  // score
) {

    private val labels = arrayListOf(
        "左-股関節", "右-股関節",
        "左-ひざ", "右-ひざ",
        "左-足首", "右-足首",
        "合計"
    )

    init {
        barChart.apply {
            setDrawBarShadow(false)
            setDrawValueAboveBar(true)
            description.isEnabled = false
            setPinchZoom(false)
            setDrawGridBackground(false)
        }
    }

    fun build() {
        val entryList = mutableListOf<BarEntry>()
        for (i in xAxisValues.indices) {
            entryList.add(
                BarEntry(
                    xAxisValues[i].toFloat(),
                    yAxisValues[i].value.toFloat()
                )
            )
        }

        val barDataSet = BarDataSet(entryList, "score")
        barDataSet.apply {
            color = Color.GREEN
        }
        val barDatasets = arrayListOf<IBarDataSet>(barDataSet)
        val barData = BarData(barDatasets)
        barData.apply {
            setValueTextSize(10f)
            barWidth = .9f
        }

        barChart.apply {
            data = barData
            legend.isEnabled = false

            xAxis.apply {
                position = XAxis.XAxisPosition.BOTTOM
                setDrawAxisLine(true)
                setDrawGridLines(false)
                val xAxisFormatter = CustomBarChartXAxisFormatter(labels)
                valueFormatter = xAxisFormatter
                granularity = 1f
            }

            axisLeft.apply {
                setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                setDrawGridLines(false)
                isEnabled = false
                axisMinimum = calculateAxisMinimum()
                axisMaximum = 100f
            }
            axisRight.apply {
                setPosition(YAxis.YAxisLabelPosition.INSIDE_CHART)
                setDrawGridLines(false)
                axisMaximum = 100f
            }

            invalidate()
        }
    }

    private fun calculateAxisMinimum(): Float {
        var minimum = 90f
        yAxisValues.forEach {
            val value = it.value-10
            if (0f < value && value <= minimum) {
                minimum = value.toFloat()
            }
        }
        return minimum
    }
}