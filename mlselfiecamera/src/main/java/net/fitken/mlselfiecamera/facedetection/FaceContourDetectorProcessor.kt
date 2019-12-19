package net.fitken.mlselfiecamera.facedetection

import android.graphics.Bitmap
import android.os.SystemClock
import android.util.Log
import com.google.android.gms.tasks.Task
import com.google.firebase.ml.vision.FirebaseVision
import com.google.firebase.ml.vision.common.FirebaseVisionImage
import com.google.firebase.ml.vision.face.FirebaseVisionFace
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetector
import com.google.firebase.ml.vision.face.FirebaseVisionFaceDetectorOptions
import net.fitken.mlselfiecamera.camera.CameraImageGraphic
import net.fitken.mlselfiecamera.camera.FrameMetadata
import net.fitken.mlselfiecamera.camera.GraphicOverlay
import java.io.IOException


/**
 * Face Contour Demo.
 */
class FaceContourDetectorProcessor(
//    private var mType: FaceDetectionType = FaceDetectionType.IDENTITY,
    faceContourDetectorListener: FaceContourDetectorListener? = null,
    isShowDot: Boolean = false
) :
    VisionProcessorBase<List<FirebaseVisionFace>>() {


    private val detector: FirebaseVisionFaceDetector
    private var mFaceContourDetectorListener: FaceContourDetectorListener? = null
    private var mIsAllowDetect = true

    private var mStartTime = SystemClock.uptimeMillis()

    init {
        val options = FirebaseVisionFaceDetectorOptions.Builder()
            .setPerformanceMode(FirebaseVisionFaceDetectorOptions.FAST)
            .setContourMode(if (isShowDot) FirebaseVisionFaceDetectorOptions.ALL_CONTOURS else FirebaseVisionFaceDetectorOptions.NO_CONTOURS)
//            .setClassificationMode(FirebaseVisionFaceDetectorOptions.ALL_CLASSIFICATIONS)
            .build()


        detector = FirebaseVision.getInstance().getVisionFaceDetector(options)
        mFaceContourDetectorListener = faceContourDetectorListener
    }

    override fun stop() {
        try {
            detector.close()
        } catch (e: IOException) {
            Log.e(TAG, "Exception thrown while trying to close Face Contour Detector: $e")
        }
    }

    fun stopDetect() {
        mIsAllowDetect = false
    }

    fun restart() {
        mIsAllowDetect = true
    }

    override fun detectInImage(image: FirebaseVisionImage): Task<List<FirebaseVisionFace>> {
        return detector.detectInImage(image)
    }

    override fun onSuccess(
        originalCameraImage: Bitmap?,
        results: List<FirebaseVisionFace>,
        frameMetadata: FrameMetadata,
        graphicOverlay: GraphicOverlay
    ) {
        graphicOverlay.clear()

        originalCameraImage?.let {
            val imageGraphic = CameraImageGraphic(graphicOverlay, it)
            graphicOverlay.add(imageGraphic)
        }

        results.forEach { face ->
            val faceGraphic = FaceContourGraphic(graphicOverlay, face)
            graphicOverlay.add(faceGraphic)
        }

        if (results.isEmpty()) {
            mFaceContourDetectorListener?.onNoFaceDetected()
        } else {
            originalCameraImage?.let { mFaceContourDetectorListener?.onCapturedFace(it) }
        }

        graphicOverlay.postInvalidate()
    }

    override fun onFailure(e: Exception) {
        Log.e(TAG, "Face detection failed $e")
    }

    companion object {
        private const val TAG = "FaceContourDetectorProc"
        private const val BUFFER_SIZE_FACE = 20
    }

    interface FaceContourDetectorListener {
        fun onCapturedFace(originalCameraImage: Bitmap)
        fun onNoFaceDetected()
    }
}