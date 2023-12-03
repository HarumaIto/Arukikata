package com.hanzyukukobo.arukikata.domain

import android.content.Context
import com.hanzyukukobo.arukikata.data.repositories.CompareDataRepository
import com.hanzyukukobo.arukikata.data.repositories.GaitAnalysisRepository
import javax.inject.Inject

class GetFramesChartDataUseCase @Inject constructor(
    private val gaitAnalysisRepository: GaitAnalysisRepository,
    private val compareDataRepository: CompareDataRepository,
    private val getChartDataUseCase: GetChartDataUseCase
) {
    suspend operator fun invoke(
        context: Context, refresh: Boolean, landmark: Int): List<List<Double>> {
        val frameData = gaitAnalysisRepository.getAllFrame(refresh)
        val compareData = compareDataRepository.getCompareData(context, refresh)

        return getChartDataUseCase(frameData, compareData, landmark)
    }
}