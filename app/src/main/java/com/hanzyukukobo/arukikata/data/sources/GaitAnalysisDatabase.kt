package com.hanzyukukobo.arukikata.data.sources

import androidx.room.AutoMigration
import androidx.room.Database
import androidx.room.RoomDatabase
import androidx.room.TypeConverters
import com.hanzyukukobo.arukikata.data.AnalysisLogEntity
import com.hanzyukukobo.arukikata.data.FrameEntity
import com.hanzyukukobo.arukikata.util.room.DateConverter
import com.hanzyukukobo.arukikata.util.room.FramesConverter
import com.hanzyukukobo.arukikata.util.room.JointsAngleConverter

@Database(
    entities = [FrameEntity::class, AnalysisLogEntity::class],
    version = 2,
    autoMigrations = [
        AutoMigration(from = 1, to = 2)
    ],
    exportSchema = true
)
@TypeConverters(
    JointsAngleConverter::class,
    FramesConverter::class,
    DateConverter::class
)
abstract class GaitAnalysisDatabase : RoomDatabase() {
    abstract fun frameDao(): FrameDao
    abstract fun logDao(): AnalysisLogDao
}