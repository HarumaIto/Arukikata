package com.hanzyukukobo.arukikata.util.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import com.google.mlkit.vision.pose.PoseLandmark
import com.hanzyukukobo.arukikata.data.JointLandmark

import com.hanzyukukobo.arukikata.util.ml.GraphicOverlay.Graphic

class CameraImageGraphic(overlay: GraphicOverlay?, private val bitmap: Bitmap) : Graphic(
    overlay!!, null
) {
    override fun draw(canvas: Canvas) {
        canvas.drawBitmap(bitmap, getTransformationMatrix(), null)
    }

    override fun drawPoint(canvas: Canvas, landmark: PoseLandmark, paint: Paint) {}

    override fun drawLine(canvas: Canvas, startLandmark: PoseLandmark?, endLandmark: PoseLandmark?, paint: Paint) {}

    override fun drawAngleText(canvas: Canvas, midPoint: PoseLandmark, text: JointLandmark) {}
}