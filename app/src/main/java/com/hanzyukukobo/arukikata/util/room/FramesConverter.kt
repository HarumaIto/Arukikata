package com.hanzyukukobo.arukikata.util.room

import androidx.room.TypeConverter
import com.hanzyukukobo.arukikata.data.FrameEntity

class FramesConverter {

    @TypeConverter
    fun stringFromFrames(value: String): List<FrameEntity> {
        val result = mutableListOf<FrameEntity>()

        val strFrames = value.split("/").dropLast(1)
        for (strFrame in strFrames) {
            val strFrameEntity = strFrame.split("|")

            val strJointsAngle = strFrameEntity[2]
            val jointsAngle = JointsAngleConverter().stringFromJointsAngle(strJointsAngle)

            result.add(FrameEntity(
                strFrameEntity[0].toInt(),
                strFrameEntity[1].toFloat(),
                jointsAngle
            ))
        }

        return result
    }

    @TypeConverter
    fun framesToString(frames: List<FrameEntity>): String {
        var result = ""
        for (frame in frames) {
            val jointsAngle = frame.jointsAngle
            val strJointsAngle = JointsAngleConverter().jointsAngleToString(jointsAngle)

            result += "${frame.index}|${frame.currentTime}|$strJointsAngle/"
        }
        return result
    }
}