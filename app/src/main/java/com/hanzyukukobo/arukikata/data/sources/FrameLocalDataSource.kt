package com.hanzyukukobo.arukikata.data.sources

import com.hanzyukukobo.arukikata.data.FrameEntity
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.withContext
import javax.inject.Inject

class FrameLocalDataSource @Inject constructor(
    private val frameDao: FrameDao,
    private val dispatcher: CoroutineDispatcher
) {

    suspend fun deleteAll() {
        withContext(dispatcher) {
            frameDao.deleteAll()
        }
    }

   suspend fun insertFrame(frameEntry: FrameEntity) {
       withContext(dispatcher) {
           frameDao.insertFrame(frameEntry)
       }
   }

    suspend fun getAll(): List<FrameEntity> = withContext(dispatcher) {
        frameDao.getAll()
    }
}