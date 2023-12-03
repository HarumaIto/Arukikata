package com.hanzyukukobo.arukikata.data.sources

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.hanzyukukobo.arukikata.data.FrameEntity

@Dao
interface FrameDao {
    @Query("SELECT * FROM frame")
    fun getAll(): List<FrameEntity>

    @Insert
    fun insertFrame(frame: FrameEntity)

    // すべて削除する方法は @Delete パラメータではできない
    @Query("DELETE FROM frame")
    fun deleteAll()
}