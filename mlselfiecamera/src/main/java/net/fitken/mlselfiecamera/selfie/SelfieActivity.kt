package net.fitken.mlselfiecamera.selfie

import android.Manifest
import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.graphics.Bitmap
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_selfie.*
import net.fitken.mlselfiecamera.R
import net.fitken.mlselfiecamera.camera.CameraSource
import net.fitken.mlselfiecamera.facedetection.FaceContourDetectorProcessor
import net.fitken.mlselfiecamera.util.PermissionUtil
import net.fitken.rose.Rose
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileOutputStream
import java.io.IOException

class SelfieActivity : AppCompatActivity(),
    FaceContourDetectorProcessor.FaceContourDetectorListener {


    companion object {
        const val KEY_IMAGE_PATH = "image_path"
        private const val KEY_TEXT_BACK = "text_back"
        private const val KEY_TEXT_DESCRIPTION = "text_description"
        private const val PERMISSION_CAMERA_REQUEST_CODE = 2

        fun createBundle(textBack: String, textDescription: String): Bundle {
            val bundle = Bundle()
            bundle.putString(KEY_TEXT_BACK, textBack)
            bundle.putString(KEY_TEXT_DESCRIPTION, textDescription)
            return bundle
        }
    }

    private var mCameraSource: CameraSource? = null
    private var mCapturedBitmap: Bitmap? = null
    private lateinit var mFaceContourDetectorProcessor: FaceContourDetectorProcessor

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_selfie)

        val textBack = intent?.extras?.getString(KEY_TEXT_BACK)
        val textDescription = intent?.extras?.getString(KEY_TEXT_DESCRIPTION)
        textBack?.let {
            tv_back.text = it
        }
        textDescription?.let {
            tv_description.text = it
        }

        if (PermissionUtil.isHavePermission(
                this, PERMISSION_CAMERA_REQUEST_CODE, Manifest.permission.CAMERA
            )
        ) {
            createCameraSource()
        }

        startCameraSource()

        tv_back.setOnClickListener {
            onBackPressed()
        }

        iv_capture.setOnClickListener {
            createSelfiePictureAndReturn()
        }
    }

    private fun createSelfiePictureAndReturn() {
        val file = File(cacheDir, "selfie.jpg")
        file.createNewFile()

        //Convert bitmap to byte array
        val bos = ByteArrayOutputStream()
        mCapturedBitmap?.compress(
            Bitmap.CompressFormat.PNG, 100, bos
        )
        val bitmapData = bos.toByteArray()

        //write the bytes in file
        val fos = FileOutputStream(file)
        fos.write(bitmapData)
        fos.flush()
        fos.close()
        val intent = Intent()
        intent.putExtra(KEY_IMAGE_PATH, file.absolutePath)
        setResult(Activity.RESULT_OK, intent)
        finish()
    }

    private fun createCameraSource() {
        // If there's no existing cameraSource, create one.
        if (mCameraSource == null) {
            mCameraSource = CameraSource(this, face_overlay)
        }

        Rose.error("Using Face Contour Detector Processor")
        mFaceContourDetectorProcessor = FaceContourDetectorProcessor(this, false)
        mCameraSource?.setMachineLearningFrameProcessor(mFaceContourDetectorProcessor)
    }

    /**
     * Starts or restarts the camera source, if it exists. If the camera source doesn't exist yet
     * (e.g., because onResume was called before the camera source was created), this will be called
     * again when the camera source is created.
     */
    private fun startCameraSource() {
        mCameraSource?.let {
            try {
                camera_preview.start(mCameraSource, face_overlay)
            } catch (e: IOException) {
                Rose.error("Unable to start camera source.  $e")
                mCameraSource?.release()
                mCameraSource = null
            }
        }
    }

    public override fun onResume() {
        super.onResume()
        startCameraSource()
    }

    /** Stops the camera.  */
    override fun onPause() {
        super.onPause()
        camera_preview.stop()
    }

    public override fun onDestroy() {
        super.onDestroy()
        mCameraSource?.release()
    }

    @SuppressLint("MissingPermission")
    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            PERMISSION_CAMERA_REQUEST_CODE -> {
                if (PermissionUtil.isPermissionGranted(
                        requestCode, PERMISSION_CAMERA_REQUEST_CODE, grantResults
                    )
                ) {
                    createCameraSource()
                } else {
                    onBackPressed()
                }
            }
        }
    }

    override fun onCapturedFace(originalCameraImage: Bitmap) {
        mCapturedBitmap = originalCameraImage
        iv_capture.alpha = 1F
        iv_capture.isEnabled = true
    }

    override fun onNoFaceDetected() {
        mCapturedBitmap = null
        iv_capture.alpha = 0.3F
        iv_capture.isEnabled = false
    }
}
