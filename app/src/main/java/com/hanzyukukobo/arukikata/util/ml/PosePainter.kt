package com.hanzyukukobo.arukikata.util.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Matrix
import android.graphics.Paint
import com.google.android.gms.common.internal.Preconditions
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseLandmark
import com.hanzyukukobo.arukikata.data.JointLandmark
import com.hanzyukukobo.arukikata.data.JointsAngle

class PosePainter internal constructor(
    private val bitmap: Bitmap,
    private val pose: Pose,
    private val angles: JointsAngle,
    private val showInFrameLikelihood: Boolean,
    private val visualizeZ: Boolean,
    private val rescaleZForVisualization: Boolean,
    private val showUpperAngleText: Boolean,
    private val showLowerAngleText: Boolean
): ResultPainter(pose) {
    private val lock = Any()

    // Matrix for transforming from image coordinates to overlay view coordinates.
    private val transformationMatrix = Matrix()
    private var imageWidth = 0
    private var imageHeight = 0

    // The factor of overlay View size to image size. Anything in the image coordinates need to be
    // scaled by this amount to fit with the area of overlay View.
    private var scaleFactor = 1.0f

    // The number of horizontal pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleWidthOffset = 0f

    // The number of vertical pixels needed to be cropped on each side to fit the image with the
    // area of overlay View after scaling.
    private var postScaleHeightOffset = 0f

    private var isImageFlipped = false
    private var needUpdateTransformation = true

    fun draw(): Bitmap {
        updateTransformationIfNeeded(bitmap)

        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)

        val landmarks = pose.allPoseLandmarks
        if (landmarks.isEmpty()) {
            return bitmap
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

        return resultBitmap
    }

    override fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {
        val point = landmark.position
        canvas.drawCircle(
            translateX(point.x),
            translateY(point.y),
            DOT_RADIUS,
            paint
        )
    }

    override fun drawLine(
        canvas: Canvas,
        startLandmark: PoseLandmark?,
        endLandmark: PoseLandmark?,
        paint: Paint
    ) {
        val start = startLandmark!!.position
        val end = endLandmark!!.position

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

    private fun scale(bitmapPixel: Float): Float {
        return bitmapPixel * scaleFactor
    }

    private fun translateX(x: Float): Float {
        return scale(x) - postScaleWidthOffset
    }

    private fun translateY(y: Float): Float {
        return scale(y) - postScaleHeightOffset
    }

    /**
     * Sets the source information of the image being processed by detectors, including size and
     * whether it is flipped, which informs how to transform image coordinates later.
     *
     * @param imageWidth the width of the image sent to ML Kit detectors
     * @param imageHeight the height of the image sent to ML Kit detectors
     * @param isFlipped whether the image is flipped. Should set it to true when the image is from the
     * front camera.
     */
    fun setImageSourceInfo(imageWidth: Int, imageHeight: Int, isFlipped: Boolean) {
        Preconditions.checkState(imageWidth > 0, "image width must be positive")
        Preconditions.checkState(imageHeight > 0, "image height must be positive")
        synchronized(lock) {
            this.imageWidth = imageWidth
            this.imageHeight = imageHeight
            isImageFlipped = isFlipped
            needUpdateTransformation = true
        }
    }

    private fun updateTransformationIfNeeded(bitmap: Bitmap) {
        if (!needUpdateTransformation || (imageWidth <= 0) || (imageHeight <= 0)) {
            return
        }
        val viewAspectRatio = bitmap.width.toFloat() / bitmap.height
        val imageAspectRatio = imageWidth.toFloat() / imageHeight
        postScaleWidthOffset = 0f
        postScaleHeightOffset = 0f
        if (viewAspectRatio > imageAspectRatio) {
            // The image needs to be vertically cropped to be displayed in this view.
            scaleFactor = bitmap.width.toFloat() / imageWidth
            postScaleHeightOffset = (bitmap.width.toFloat() / imageAspectRatio - bitmap.height) / 2
        } else {
            // The image needs to be horizontally cropped to be displayed in this view.
            scaleFactor = bitmap.height.toFloat() / imageHeight
            postScaleWidthOffset = (bitmap.height.toFloat() * imageAspectRatio - bitmap.width) / 2
        }
        transformationMatrix.reset()
        transformationMatrix.setScale(scaleFactor, scaleFactor)
        transformationMatrix.postTranslate(-postScaleWidthOffset, -postScaleHeightOffset)
        if (isImageFlipped) {
            transformationMatrix.postScale(-1f, 1f, bitmap.width / 2f, bitmap.height / 2f)
        }
        needUpdateTransformation = false
    }
}