package com.hanzyukukobo.arukikata.data

class JointsAngle(
    private val joints: List<JointLandmark>,
) {

    fun getJointLandmark(jointLandmarkType: Int) : JointLandmark? =
        if (this.joints.isEmpty()) {
            null
        } else {
            joints[jointLandmarkType]
        }

    fun getAllJointLandmark() : List<JointLandmark> {
        return joints
    }
}