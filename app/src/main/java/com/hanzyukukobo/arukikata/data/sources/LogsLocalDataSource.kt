package com.hanzyukukobo.arukikata.data.sources

import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.FrameEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class LogsLocalDataSource @Inject constructor(
    private val logDao: AnalysisLogDao,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun getAll(): List<AnalysisLogEntity> = withContext(dispatcher) {
        logDao.getAll()
    }

    suspend fun getLogFrames(name: String): AnalysisLogEntity = withContext(dispatcher) {
        logDao.getLogFrames(name)
    }

    suspend fun insertLog(analysisLogEntity: AnalysisLogEntity) {
        withContext(dispatcher) {
            logDao.insertLog(analysisLogEntity)
        }
    }
}