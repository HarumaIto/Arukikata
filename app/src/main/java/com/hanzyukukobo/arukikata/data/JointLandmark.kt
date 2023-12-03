package com.hanzyukukobo.arukikata.data

class JointLandmark (
    val landmark: Int,
    val angle: Double
){
    companion object {
        val LEFT_SHOULDER = 0
        val RIGHT_SHOULDER = 1

        val LEFT_ELBOW = 2
        val RIGHT_ELBOW = 3

        val LEFT_WRIST = 4
        val RIGHT_WRIST = 5

        val LEFT_HIP = 6
        val RIGHT_HIP = 7

        val LEFT_KNEE = 8
        val RIGHT_KNEE = 9

        val LEFT_ANKLE = 10
        val RIGHT_ANKLE = 11

        fun allLandmarks() = listOf(
            LEFT_SHOULDER,
            RIGHT_SHOULDER,
            LEFT_ELBOW,
            RIGHT_ELBOW,
            LEFT_WRIST,
            RIGHT_WRIST,
            LEFT_HIP,
            RIGHT_HIP,
            LEFT_KNEE,
            RIGHT_KNEE,
            LEFT_ANKLE,
            RIGHT_ANKLE,
        )
    }
}