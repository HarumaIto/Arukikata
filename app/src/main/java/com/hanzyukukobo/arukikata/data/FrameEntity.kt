package com.hanzyukukobo.arukikata.data

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "frame")
data class FrameEntity(
    @PrimaryKey val index: Int,
    @ColumnInfo(name = "current_time") val currentTime: Float,
    @ColumnInfo(name = "joints_angle") val jointsAngle: JointsAngle
)
