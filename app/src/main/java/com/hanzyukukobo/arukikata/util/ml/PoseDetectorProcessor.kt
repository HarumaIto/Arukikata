package com.hanzyukukobo.arukikata.util.ml

import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.util.Log
import android.widget.Toast
import androidx.camera.core.ExperimentalGetImage
import androidx.camera.core.ImageProxy
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.TaskExecutors
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.pose.Pose
import com.google.mlkit.vision.pose.PoseDetection
import com.google.mlkit.vision.pose.PoseDetector
import com.google.mlkit.vision.pose.PoseDetectorOptionsBase
import com.google.mlkit.vision.pose.defaults.PoseDetectorOptions
import com.hanzyukukobo.arukikata.data.JointsAngle
import com.hanzyukukobo.arukikata.data.VideoInfo
import io.reactivex.rxjava3.core.BackpressureStrategy
import io.reactivex.rxjava3.core.Flowable
import io.reactivex.rxjava3.core.FlowableEmitter
import io.reactivex.rxjava3.core.FlowableOnSubscribe
import io.reactivex.rxjava3.core.Observable
import io.reactivex.rxjava3.core.ObservableEmitter
import io.reactivex.rxjava3.core.ObservableOnSubscribe

class PoseDetectorProcessor(
    detectorMode: Int,
    showUpperAngleText: Boolean,
    showLowerAngleText: Boolean
) {
    private val detector: PoseDetector
    private val executor: ScopedExecutor

    private val showUpperAngleText: Boolean
    private val showLowerAngleText: Boolean

    init {
        val options = PoseDetectorOptions.Builder()
            .setDetectorMode(detectorMode)
            .setPreferredHardwareConfigs(PoseDetectorOptionsBase.CPU_GPU)
            .build()

        detector = PoseDetection.getClient(options)
        executor = ScopedExecutor(TaskExecutors.MAIN_THREAD)

        this.showUpperAngleText = showUpperAngleText
        this.showLowerAngleText = showLowerAngleText
    }

    fun asyncProcessBitmap(bitmap: Bitmap): Observable<Pose> {
        // = で戻り値にするとprocessBitmapでexecutorが使えなくなるバグがある
        return Observable.create(ObservableOnSubscribe {
            if (it.isDisposed) {
                return@ObservableOnSubscribe
            }
            // この中でonNextとonErrorを処理する
            processBitmap(bitmap, it)
        })
    }

    private fun processBitmap(bitmap: Bitmap, emitter: ObservableEmitter<Pose>) {
        val inputImage = InputImage.fromBitmap(bitmap, 0)

        // 非同期Taskの結果をRxで通知して処理する
        detectInImage(inputImage)
            .addOnSuccessListener(executor) {
                emitter.onNext(it)
            }.addOnFailureListener(executor) {
                emitter.onError(it)
            }
    }

    @ExperimentalGetImage
    fun processImageProxy(image: ImageProxy, graphicOverlay: GraphicOverlay) {
        val bitmap = BitmapUtils.getBitmap(image)
        val inputImage = InputImage.fromMediaImage(image.image!!, image.imageInfo.rotationDegrees)
        setUpListener(
            detectInImage(inputImage),
            graphicOverlay,
            bitmap
        ).addOnCompleteListener { image.close() }
        // When the image is from CameraX analysis use case, must call image.close() on received
        // images when finished using them. Otherwise, new images may not be received or the camera
        // may stall.
    }

    @ExperimentalGetImage
    fun postGraphicOverlay(image: ImageProxy, graphicOverlay: GraphicOverlay) {
        val bitmap = BitmapUtils.getBitmap(image)

        graphicOverlay.clear()
        if (bitmap != null) {
            graphicOverlay.add(CameraImageGraphic(graphicOverlay, bitmap))
        }
        graphicOverlay.postInvalidate()

        image.close()
    }

    private fun setUpListener(
        task: Task<Pose>,
        graphicOverlay: GraphicOverlay,
        originalCameraImage: Bitmap?
    ): Task<Pose> {
        return task.addOnSuccessListener(executor) {
            graphicOverlay.clear()
            if (originalCameraImage != null) {
                graphicOverlay.add(CameraImageGraphic(graphicOverlay, originalCameraImage))
            }
            graphicOverlay.add(
                PoseGraphic(
                    graphicOverlay,
                    it,
                    PoseCalculator.createJointsAngle(it),
                    showInFrameLikelihood = false,
                    visualizeZ = false,
                    rescaleZForVisualization = false,
                    showUpperAngleText,
                    showLowerAngleText
                )
            )
            graphicOverlay.postInvalidate()
        }.addOnFailureListener(executor) {
            graphicOverlay.clear()
            graphicOverlay.postInvalidate()
            val error = "Failed to process. Error: " + it.localizedMessage
            Toast.makeText(
                graphicOverlay.context,
                "$error\nCause: ${it.cause}",
                Toast.LENGTH_SHORT
            ).show()
            Log.d("PoseDetectorProcessor", error)
            it.printStackTrace()
            Log.e("PoseDetectorProcessor.kt", "task failure!", it)
        }
    }

    fun paintPoseResult(
        needUpDataSourceInfo: Boolean,
        videoInfo: VideoInfo,
        bitmap: Bitmap,
        pose: Pose?,
        jointsAngle: JointsAngle
    ): Bitmap {
        val resultBitmap = Bitmap.createBitmap(bitmap.width, bitmap.height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(resultBitmap)
        canvas.drawBitmap(bitmap, 0f, 0f, Paint())

        if (pose != null) {
            val posePainter = PosePainter(
                bitmap,
                pose,
                jointsAngle,
                showInFrameLikelihood = false,
                visualizeZ = false,
                rescaleZForVisualization = false,
                showUpperAngleText = true,
                showLowerAngleText = true
            )
            if (needUpDataSourceInfo) {
                // 初回にリサイズするときに必要なサイズなどを初期化するため
                if (videoInfo.rotation == 0 || videoInfo.rotation == 180) {
                    posePainter.setImageSourceInfo(videoInfo.width, videoInfo.height, false)
                } else {
                    posePainter.setImageSourceInfo(videoInfo.height, videoInfo.width, false)
                }
            }
            val poseBitmap = posePainter.draw()
            canvas.drawBitmap(poseBitmap, 0f, 0f, Paint())
        }

        return resultBitmap
    }

    fun stop() {
        detector.close()
        executor.shutdown()
    }

    private fun detectInImage(image: InputImage): Task<Pose> {
        return detector.process(image)
    }
}