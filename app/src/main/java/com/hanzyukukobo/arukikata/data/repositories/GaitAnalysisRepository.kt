package com.hanzyukukobo.arukikata.data.repositories

import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.FrameEntity
import com.hanzyukukobo.arukikata.data.VideoInfo
import com.hanzyukukobo.arukikata.data.sources.FrameLocalDataSource
import com.hanzyukukobo.arukikata.data.sources.LogsLocalDataSource
import javax.inject.Inject

class GaitAnalysisRepository @Inject constructor(
    private val frameLocalDataSource: FrameLocalDataSource,
    private val logsLocalDataSource: LogsLocalDataSource
) {
    private var videoInfo: VideoInfo? = null

    private var frames: List<FrameEntity> = emptyList()

    // Frame
    suspend fun deleteAllFrame() {
        frameLocalDataSource.deleteAll()
    }

    suspend fun insertFrame(frameEntity: FrameEntity) {
        frameLocalDataSource.insertFrame(frameEntity)
    }

    suspend fun getAllFrame(refresh: Boolean = false): List<FrameEntity> {
        if (refresh || frames.isEmpty()) {
            frames = frameLocalDataSource.getAll()
        }
        return frames
    }

    // Log
    suspend fun insertLog(analysisLogEntity: AnalysisLogEntity) {
        logsLocalDataSource.insertLog(analysisLogEntity)
    }

    suspend fun getLogFrames(itemName: String): AnalysisLogEntity {
        return logsLocalDataSource.getLogFrames(itemName)
    }

    suspend fun getAllLogs(): List<AnalysisLogEntity> {
        return logsLocalDataSource.getAll()
    }

    // Video info
    fun setVideoInfo(videoInfo: VideoInfo) {
        this.videoInfo = videoInfo
    }

    fun getVideoInfo(): VideoInfo? = videoInfo

    fun resetVideoInfo() {
        this.videoInfo = null
    }
}