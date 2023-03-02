package com.hanzyukukobo.arukikata.domain

import android.content.Context
import com.hanzyukukobo.arukikata.data.repositories.CompareDataRepository
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import javax.inject.Inject

class GetLogChartDataUseCase @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val compareDataRepository: CompareDataRepository,
    private val getChartDataUseCase: GetChartDataUseCase
) {

    suspend operator fun invoke(
        context: Context, itemName: String, refresh: Boolean, landmark: Int): List<List<Double>> {
        val logFramesData = gaitAnalysisRepository.getLogFrames(itemName).frames
        val compareData = compareDataRepository.getCompareData(context, refresh)

        return getChartDataUseCase(logFramesData, compareData, landmark)
    }
}