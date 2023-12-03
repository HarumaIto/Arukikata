package com.hanzyukukobo.arukikata.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import java.util.Date

@Entity(tableName = "logs")
data class AnalysisLogEntity(
    @PrimaryKey val id: Int,
    @ColumnInfo val name: String,
    @ColumnInfo val time: Date,
    @ColumnInfo val frames: List<FrameEntity>
)
