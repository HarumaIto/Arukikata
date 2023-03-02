package com.hanzyukukobo.arukikata.util.ml

import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.hanzyukukobo.arukikata.data.JointLandmark

abstract class ResultPainter (
    pose: Pose?,
) {
    var zMin = java.lang.Float.MAX_VALUE
    var zMax = java.lang.Float.MIN_VALUE
    val leftPaint: Paint
    val rightPaint: Paint
    val whitePaint: Paint = Paint()

    val nose = pose?.getPoseLandmark(PoseLandmark.NOSE)
    val leftEyeInner = pose?.getPoseLandmark(PoseLandmark.LEFT_EYE_INNER)
    val leftEye = pose?.getPoseLandmark(PoseLandmark.LEFT_EYE)
    val leftEyeOuter = pose?.getPoseLandmark(PoseLandmark.LEFT_EYE_OUTER)
    val rightEyeInner = pose?.getPoseLandmark(PoseLandmark.RIGHT_EYE_INNER)
    val rightEye = pose?.getPoseLandmark(PoseLandmark.RIGHT_EYE)
    val rightEyeOuter = pose?.getPoseLandmark(PoseLandmark.RIGHT_EYE_OUTER)
    val leftEar = pose?.getPoseLandmark(PoseLandmark.LEFT_EAR)
    val rightEar = pose?.getPoseLandmark(PoseLandmark.RIGHT_EAR)
    val leftMouth = pose?.getPoseLandmark(PoseLandmark.LEFT_MOUTH)
    val rightMouth = pose?.getPoseLandmark(PoseLandmark.RIGHT_MOUTH)

    val leftShoulder = pose?.getPoseLandmark(PoseLandmark.LEFT_SHOULDER)
    val rightShoulder = pose?.getPoseLandmark(PoseLandmark.RIGHT_SHOULDER)
    val leftElbow = pose?.getPoseLandmark(PoseLandmark.LEFT_ELBOW)
    val rightElbow = pose?.getPoseLandmark(PoseLandmark.RIGHT_ELBOW)
    val leftWrist = pose?.getPoseLandmark(PoseLandmark.LEFT_WRIST)
    val rightWrist = pose?.getPoseLandmark(PoseLandmark.RIGHT_WRIST)
    val leftHip = pose?.getPoseLandmark(PoseLandmark.LEFT_HIP)
    val rightHip = pose?.getPoseLandmark(PoseLandmark.RIGHT_HIP)
    val leftKnee = pose?.getPoseLandmark(PoseLandmark.LEFT_KNEE)
    val rightKnee = pose?.getPoseLandmark(PoseLandmark.RIGHT_KNEE)
    val leftAnkle = pose?.getPoseLandmark(PoseLandmark.LEFT_ANKLE)
    val rightAnkle = pose?.getPoseLandmark(PoseLandmark.RIGHT_ANKLE)

    val leftPinky = pose?.getPoseLandmark(PoseLandmark.LEFT_PINKY)
    val rightPinky = pose?.getPoseLandmark(PoseLandmark.RIGHT_PINKY)
    val leftIndex = pose?.getPoseLandmark(PoseLandmark.LEFT_INDEX)
    val rightIndex = pose?.getPoseLandmark(PoseLandmark.RIGHT_INDEX)
    val leftThumb = pose?.getPoseLandmark(PoseLandmark.LEFT_THUMB)
    val rightThumb = pose?.getPoseLandmark(PoseLandmark.RIGHT_THUMB)
    val leftHeel = pose?.getPoseLandmark(PoseLandmark.LEFT_HEEL)
    val rightHeel = pose?.getPoseLandmark(PoseLandmark.RIGHT_HEEL)
    val leftFootIndex = pose?.getPoseLandmark(PoseLandmark.LEFT_FOOT_INDEX)
    val rightFootIndex = pose?.getPoseLandmark(PoseLandmark.RIGHT_FOOT_INDEX)

    init {
        whitePaint.strokeWidth = STROKE_WIDTH
        whitePaint.color = Color.WHITE
        whitePaint.textSize = IN_FRAME_LIKELIHOOD_TEXT_SIZE
        leftPaint = Paint()
        leftPaint.strokeWidth = STROKE_WIDTH
        leftPaint.color = Color.GREEN
        rightPaint = Paint()
        rightPaint.strokeWidth = STROKE_WIDTH
        rightPaint.color = Color.YELLOW
    }

    abstract fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint)

    abstract fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    )

    abstract fun drawAngleText(canvas: Canvas, midPoint: PoseLandmark, text: JointLandmark)

    companion object {
        const val DOT_RADIUS = 8.0f
        private const val IN_FRAME_LIKELIHOOD_TEXT_SIZE = 30.0f
        private const val STROKE_WIDTH = 10.0f
    }
}