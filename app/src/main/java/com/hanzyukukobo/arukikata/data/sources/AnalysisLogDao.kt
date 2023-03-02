package com.hanzyukukobo.arukikata.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.FrameEntity

@Dao
interface AnalysisLogDao {
    @Query("SELECT * FROM logs")
    fun getAll(): List<AnalysisLogEntity>

    @Query("SELECT * FROM logs WHERE name = :itemName")
    fun getLogFrames(itemName: String): AnalysisLogEntity

    @Insert
    fun insertLog(analysisLog: AnalysisLogEntity)

    // 削除系や変更系を追加していきたい
}