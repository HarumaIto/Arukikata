package com.hanzyukukobo.arukikata.util.ml

import android.graphics.Canvas
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle
import java.util.*

class PoseGraphic internal constructor(
    overlay: GraphicOverlay?,
    private val pose: Pose,
    private val angles: JointsAngle,
    private val showInFrameLikelihood: Boolean,
    private val visualizeZ: Boolean,
    private val rescaleZForVisualization: Boolean,
    private val showUpperAngleText: Boolean,
    private val showLowerAngleText: Boolean
) : GraphicOverlay.Graphic(overlay!!, pose) {

    override fun draw(canvas: Canvas) {
        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return
        }

        // Draw all the points
        for (landmark in landmarks) {
            drawPoint(canvas, landmark, whitePaint)
            if (visualizeZ && rescaleZForVisualization) {
                zMin = kotlin.math.min(zMin, landmark.position3D.z)
                zMax = kotlin.math.max(zMax, landmark.position3D.z)
            }
        }

        // Face
        drawLine(canvas, nose, leftEyeInner, whitePaint)
        drawLine(canvas, leftEyeInner, leftEye, whitePaint)
        drawLine(canvas, leftEye, leftEyeOuter, whitePaint)
        drawLine(canvas, leftEyeOuter, leftEar, whitePaint)
        drawLine(canvas, nose, rightEyeInner, whitePaint)
        drawLine(canvas, rightEyeInner, rightEye, whitePaint)
        drawLine(canvas, rightEye, rightEyeOuter, whitePaint)
        drawLine(canvas, rightEyeOuter, rightEar, whitePaint)
        drawLine(canvas, leftMouth, rightMouth, whitePaint)

        drawLine(canvas, leftShoulder, rightShoulder, whitePaint)
        drawLine(canvas, leftHip, rightHip, whitePaint)

        // Left body
        drawLine(canvas, leftShoulder, leftElbow, leftPaint)
        drawLine(canvas, leftElbow, leftWrist, leftPaint)
        drawLine(canvas, leftShoulder, leftHip, leftPaint)
        drawLine(canvas, leftHip, leftKnee, leftPaint)
        drawLine(canvas, leftKnee, leftAnkle, leftPaint)
        drawLine(canvas, leftWrist, leftThumb, leftPaint)
        drawLine(canvas, leftWrist, leftPinky, leftPaint)
        drawLine(canvas, leftWrist, leftIndex, leftPaint)
        drawLine(canvas, leftIndex, leftPinky, leftPaint)
        drawLine(canvas, leftAnkle, leftHeel, leftPaint)
        drawLine(canvas, leftHeel, leftFootIndex, leftPaint)

        // Right body
        drawLine(canvas, rightShoulder, rightElbow, rightPaint)
        drawLine(canvas, rightElbow, rightWrist, rightPaint)
        drawLine(canvas, rightShoulder, rightHip, rightPaint)
        drawLine(canvas, rightHip, rightKnee, rightPaint)
        drawLine(canvas, rightKnee, rightAnkle, rightPaint)
        drawLine(canvas, rightWrist, rightThumb, rightPaint)
        drawLine(canvas, rightWrist, rightPinky, rightPaint)
        drawLine(canvas, rightWrist, rightIndex, rightPaint)
        drawLine(canvas, rightIndex, rightPinky, rightPaint)
        drawLine(canvas, rightAnkle, rightHeel, rightPaint)
        drawLine(canvas, rightHeel, rightFootIndex, rightPaint)

        // Draw inFrameLikelihood for all points
        if (showInFrameLikelihood) {
            for (landmark in landmarks) {
                canvas.drawText(
                    String.format(Locale.US, "%.2f", landmark.inFrameLikelihood),
                    translateX(landmark.position.x),
                    translateY(landmark.position.y),
                    whitePaint
                )
            }
        }

        if (showUpperAngleText) {
            // Draw upper degree text
            drawAngleText(canvas, leftShoulder!!, angles.getJointLandmark(JointLandmark.LEFT_SHOULDER)!!)
            drawAngleText(canvas, rightShoulder!!, angles.getJointLandmark(JointLandmark.RIGHT_SHOULDER)!!)
            drawAngleText(canvas, leftElbow!!, angles.getJointLandmark(JointLandmark.LEFT_ELBOW)!!)
            drawAngleText(canvas, rightElbow!!, angles.getJointLandmark(JointLandmark.RIGHT_ELBOW)!!)
            drawAngleText(canvas, leftWrist!!, angles.getJointLandmark(JointLandmark.LEFT_WRIST)!!)
            drawAngleText(canvas, rightWrist!!, angles.getJointLandmark(JointLandmark.RIGHT_WRIST)!!)
        }
        if (showLowerAngleText) {
            // Draw lower degree text
            drawAngleText(canvas, leftHip!!, angles.getJointLandmark(JointLandmark.LEFT_HIP)!!)
            drawAngleText(canvas, rightHip!!, angles.getJointLandmark(JointLandmark.RIGHT_HIP)!!)
            drawAngleText(canvas, leftKnee!!, angles.getJointLandmark(JointLandmark.LEFT_KNEE)!!)
            drawAngleText(canvas, rightKnee!!, angles.getJointLandmark(JointLandmark.RIGHT_KNEE)!!)
            drawAngleText(canvas, leftAnkle!!, angles.getJointLandmark(JointLandmark.LEFT_ANKLE)!!)
            drawAngleText(canvas, rightAnkle!!, angles.getJointLandmark(JointLandmark.RIGHT_ANKLE)!!)
        }
    }

    override fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
        val point = landmark.position3D
        updatePaintColorByZValue(
            paint,
            canvas,
            visualizeZ,
            rescaleZForVisualization,
            point.z,
            zMin,
            zMax
        )
        canvas.drawCircle(
            translateX(point.x),
            translateY(point.y),
            DOT_RADIUS,
            paint)
    }

    override fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    ) {
        val start = startLandmark!!.position3D
        val end = endLandmark!!.position3D

        // Gets average z for the current body line
        val avgZInImagePixel = (start.z + end.z) / 2
        updatePaintColorByZValue(
            paint,
            canvas,
            visualizeZ,
            rescaleZForVisualization,
            avgZInImagePixel,
            zMin,
            zMax
        )

        canvas.drawLine(
            translateX(start.x),
            translateY(start.y),
            translateX(end.x),
            translateY(end.y),
            paint
        )
    }

    override fun drawAngleText(canvas: Canvas, midPoint: PoseLandmark, text: JointLandmark) {
        canvas.drawText(
            text.angle.toString(),
            translateX(midPoint.position.x) + 10,
            translateY(midPoint.position.y),
            whitePaint
        )
    }
}