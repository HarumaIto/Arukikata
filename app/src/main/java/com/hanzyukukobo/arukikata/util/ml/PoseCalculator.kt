package com.hanzyukukobo.arukikata.util.ml

import android.graphics.PointF
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle
import kotlin.math.abs
import kotlin.math.atan2
import kotlin.math.round

class PoseCalculator {
    companion object {
        fun createJointsAngle(pose: Pose): JointsAngle {
            val leftShoulder = pose.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
            val rightShoulder = pose.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
            val leftElbow = pose.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
            val rightElbow = pose.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
            val leftWrist = pose.getPoseLandmark(PoseLandmark.LEFT_WRIST)
            val rightWrist = pose.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
            val leftHip = pose.getPoseLandmark(PoseLandmark.LEFT_HIP)
            val rightHip = pose.getPoseLandmark(PoseLandmark.RIGHT_HIP)
            val leftKnee = pose.getPoseLandmark(PoseLandmark.LEFT_KNEE)
            val rightKnee = pose.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
            val leftAnkle = pose.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
            val rightAnkle = pose.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)
            val leftIndex = pose.getPoseLandmark(PoseLandmark.LEFT_INDEX)
            val rightIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
            val leftFootIndex = pose.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
            val rightFootIndex = pose.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

            return JointsAngle(
                arrayListOf(
                    JointLandmark(JointLandmark.LEFT_SHOULDER, angle(leftElbow?.position, leftShoulder?.position, leftHip?.position)),
                    JointLandmark(JointLandmark.RIGHT_SHOULDER, angle(rightElbow?.position, rightShoulder?.position, rightHip?.position)),
                    JointLandmark(JointLandmark.LEFT_ELBOW, angle(leftWrist?.position, leftElbow?.position, leftShoulder?.position)),
                    JointLandmark(JointLandmark.RIGHT_ELBOW, angle(rightWrist?.position, rightElbow?.position, rightShoulder?.position)),
                    JointLandmark(JointLandmark.LEFT_WRIST, angle(leftIndex?.position, leftWrist?.position, leftElbow?.position)),
                    JointLandmark(JointLandmark.RIGHT_WRIST, angle(rightIndex?.position, rightWrist?.position, rightElbow?.position)),
                    JointLandmark(JointLandmark.LEFT_HIP, angle(leftShoulder?.position, leftHip?.position, leftKnee?.position)),
                    JointLandmark(JointLandmark.RIGHT_HIP, angle(rightShoulder?.position, rightHip?.position, rightKnee?.position)),
                    JointLandmark(JointLandmark.LEFT_KNEE, angle(leftHip?.position, leftKnee?.position, leftAnkle?.position)),
                    JointLandmark(JointLandmark.RIGHT_KNEE, angle(rightHip?.position, rightKnee?.position, rightAnkle?.position)),
                    JointLandmark(JointLandmark.LEFT_ANKLE, angle(leftKnee?.position, leftAnkle?.position, leftFootIndex?.position)),
                    JointLandmark(JointLandmark.RIGHT_ANKLE, angle(rightKnee?.position, rightAnkle?.position, rightFootIndex?.position))
                )
            )
        }

        private fun angle(firstPoint: PointF?, midPoint: PointF?, lastPoint: PointF?): Double {
            if (firstPoint == null && midPoint == null && lastPoint == null) return 0.0
            var result = Math.toDegrees(
                atan2(
                    (lastPoint!!.y - midPoint!!.y).toDouble(),
                    (lastPoint.x - midPoint.x).toDouble())
                - atan2(
                    (firstPoint!!.y - midPoint.y).toDouble(),
                    (firstPoint.x - midPoint.x).toDouble()
                )
            )

            // Angle should never be negative
            result = abs(result)

            // Always get the acute representation of the angle
            if (result > 180) {
                result = (360.0 - result)
            }

            // 小数点四捨五入
            return round(result)
        }
    }
}