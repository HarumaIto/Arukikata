package com.hanzyukukobo.arukikata.util.chart

import android.graphics.Color
import android.util.Log
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet

class LineChartBuilder(
    private val lineChart: LineChart,
    private var xAxisCompareValues: List<Double>,
    private val yAxisCompareValues: List<Double>,
    private var xAxisValues: List<Double>,
    private val yAxisValues: List<Double>
) {
    var chartDescriptionName = ""

    fun build() {
        val compareEntryList = mutableListOf<Entry>()
        val entryList = mutableListOf<Entry>()
        Log.d("test", "${yAxisCompareValues.size} : ${yAxisValues.size}")
        for (i in xAxisCompareValues.indices) {
            compareEntryList.add(Entry(xAxisCompareValues[i].toFloat(), yAxisCompareValues[i].toFloat()))
        }
        for (i in xAxisValues.indices) {
            entryList.add(Entry(xAxisValues[i].toFloat(), yAxisValues[i].toFloat()))
        }

        val lineDataSets = mutableListOf<ILineDataSet>()

        val compareLineDataSet = LineDataSet(compareEntryList, "比較元")
            .apply {
                color = Color.BLUE
                setDrawValues(false)
                setDrawCircles(false)
            }

        val lineDataSet = LineDataSet(entryList, "分析結果")
            .apply {
                color = Color.RED
                setDrawValues(false)
                setDrawCircles(false)
            }

        lineDataSets.apply {
            add(compareLineDataSet)
            add(lineDataSet)
        }

        val lineData = LineData(lineDataSets)

        lineChart.apply {
            data = lineData

            xAxis.apply {
                isEnabled = false
                textColor = Color.BLACK
            }

            axisLeft.apply {
                isEnabled = true
                textColor = Color.BLACK
            }

            axisRight.apply {
                isEnabled = false
            }

            legend.isEnabled = false
            description.text = chartDescriptionName

            invalidate()
        }
    }
}