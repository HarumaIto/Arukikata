package com.hanzyukukobo.arukikata.util.chart

import com.github.mikephil.charting.formatter.ValueFormatter

class CustomBarChartXAxisFormatter(
    private val values: ArrayList<String>
) : ValueFormatter() {

    override fun getFormattedValue(value: Float): String {
        val intValue = value.toInt()
        return if (intValue >= 0 && intValue < values.size) {
            values[intValue]
        } else {
            ""
        }
    }
}