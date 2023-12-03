package com.hanzyukukobo.arukikata.data.repositories

import android.content.Context
import com.hanzyukukobo.arukikata.data.sources.CompareDataLocalDataSource
import javax.inject.Inject

class CompareDataRepository @Inject constructor(
    private val compareDataLocalDataSource: CompareDataLocalDataSource
) {
    private var comparisonData: List<Map<String, Any>> = emptyList()

    fun getCompareData(
        context: Context,
        refresh: Boolean = false,
    ): List<Map<String, Any>> {
        if (refresh || comparisonData.isEmpty()) {
            comparisonData = compareDataLocalDataSource.loadDataForAssets(context)
        }
        return comparisonData
    }
}