package com.hanzyukukobo.arukikata.util.room

import androidx.room.TypeConverter
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle

// Room用
class JointsAngleConverter {
    @TypeConverter
    fun stringFromJointsAngle(value: String): JointsAngle {
        val strJointLandmarks = value.split(",")
        val strJointsAngle = arrayListOf<JointLandmark>()
        for (str in strJointLandmarks) {
            val array = str.split(":")
            strJointsAngle.add(JointLandmark(array[0].toInt(), array[1].toDouble()))
        }
        return JointsAngle(strJointsAngle)
    }

    @TypeConverter
    fun jointsAngleToString(jointsAngle: JointsAngle): String {
        val jointLandmarks = jointsAngle.getAllJointLandmark()
        val resultArray = arrayListOf<String>()
        for (landmark in jointLandmarks) {
            resultArray.add("${landmark.landmark}:${landmark.angle}")
        }
        return resultArray.toTypedArray().joinToString(",")
        // double:int,double:int,double:int
    }
}