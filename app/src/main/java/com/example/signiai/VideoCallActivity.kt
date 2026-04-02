package com.example.signiai

import android.Manifest
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.util.AttributeSet
import android.util.Log
import android.view.SurfaceView
import android.view.View
import android.widget.FrameLayout
import android.widget.ImageButton
import android.widget.TextView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.android.material.floatingactionbutton.FloatingActionButton
import io.agora.rtc2.*
import io.agora.rtc2.video.VideoCanvas
import org.tensorflow.lite.support.image.TensorImage
import org.tensorflow.lite.task.vision.classifier.ImageClassifier
import java.io.ByteArrayOutputStream
import java.util.concurrent.ExecutorService
import java.util.concurrent.Executors

class VideoCallActivity : AppCompatActivity() {

    private val AGORA_APP_ID = "a53f1351470f4ecd8f38ee7a4fc35c5a"

    private lateinit var localContainer: FrameLayout
    private lateinit var remoteContainer: FrameLayout
    private lateinit var tvGesture: TextView

    private lateinit var btnEndCall: FloatingActionButton
    private lateinit var btnFlipCamera: ImageButton

    private var mRtcEngine: RtcEngine? = null
    private var channelName = "test_room"

    // ML
    private lateinit var cameraExecutor: ExecutorService
    private var imageClassifier: ImageClassifier? = null

    private val rtcHandler = object : IRtcEngineEventHandler() {
        override fun onUserJoined(uid: Int, elapsed: Int) {
            runOnUiThread { setupRemoteVideo(uid) }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        localContainer = findViewById(R.id.local_video_card)
        remoteContainer = findViewById(R.id.remote_video_view_container)
        tvGesture = findViewById(R.id.tvDetectedGesture)

        btnEndCall = findViewById(R.id.btnEndCall)
        btnFlipCamera = findViewById(R.id.btnFlipCamera)

        channelName = intent.getStringExtra("MEETING_ID") ?: "test_room"

        cameraExecutor = Executors.newSingleThreadExecutor()

        setupModel()

        if (checkPermissions()) {
            initAgora()
            startDetectionCamera()
        } else {
            permissionLauncher.launch(
                arrayOf(Manifest.permission.CAMERA, Manifest.permission.RECORD_AUDIO)
            )
        }

        btnEndCall.setOnClickListener { endCall() }
        btnFlipCamera.setOnClickListener { mRtcEngine?.switchCamera() }
    }

    // ================= AGORA =================

    private fun initAgora() {
        val config = RtcEngineConfig()
        config.mContext = applicationContext
        config.mAppId = AGORA_APP_ID
        config.mEventHandler = rtcHandler

        mRtcEngine = RtcEngine.create(config)
        mRtcEngine?.enableVideo()

        val localView = SurfaceView(this)
        localView.setZOrderMediaOverlay(true)
        localView.setZOrderOnTop(true)

        localContainer.removeAllViews()
        localContainer.addView(localView)

        mRtcEngine?.setupLocalVideo(
            VideoCanvas(localView, VideoCanvas.RENDER_MODE_HIDDEN, 0)
        )

        mRtcEngine?.startPreview()

        val options = ChannelMediaOptions()
        options.channelProfile = Constants.CHANNEL_PROFILE_COMMUNICATION
        options.clientRoleType = Constants.CLIENT_ROLE_BROADCASTER
        options.publishCameraTrack = true
        options.publishMicrophoneTrack = true

        mRtcEngine?.joinChannel(null, channelName, 0, options)
    }

    private fun setupRemoteVideo(uid: Int) {
        val remoteView = SurfaceView(this)
        remoteContainer.removeAllViews()
        remoteContainer.addView(remoteView)

        mRtcEngine?.setupRemoteVideo(
            VideoCanvas(remoteView, VideoCanvas.RENDER_MODE_HIDDEN, uid)
        )
    }

    // ================= ML MODEL =================

    private fun setupModel() {
        try {
            val options = ImageClassifier.ImageClassifierOptions.builder()
                .setMaxResults(1)
                .build()

            imageClassifier = ImageClassifier.createFromFileAndOptions(
                this,
                "gesture_model.tflite" // 🔥 YOUR FILE NAME
                , options
            )

        } catch (e: Exception) {
            Log.e("ML", "Model load error: ${e.message}")
        }
    }

    private fun startDetectionCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val analysis = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()
                .also {
                    it.setAnalyzer(cameraExecutor) { image ->
                        processFrame(image)
                    }
                }

            cameraProvider.unbindAll()
            cameraProvider.bindToLifecycle(
                this,
                CameraSelector.DEFAULT_FRONT_CAMERA,
                analysis
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun processFrame(image: ImageProxy) {

        Log.d("ML", "Frame running")

        val bitmap = imageToBitmap(image)
        val rotated = rotateBitmap(bitmap, image.imageInfo.rotationDegrees)

        if (imageClassifier != null && rotated != null) {

            val results = imageClassifier!!.classify(TensorImage.fromBitmap(rotated))

            runOnUiThread {

                if (!results.isNullOrEmpty() && results[0].categories.isNotEmpty()) {
                    val label = results[0].categories[0].label
                    tvGesture.text = "Gesture: $label"
                } else {
                    tvGesture.text = "No gesture"
                }
            }
        }

        image.close()
    }

    // ================= IMAGE UTILS =================

    private fun imageToBitmap(image: ImageProxy): Bitmap? {
        val yBuffer = image.planes[0].buffer
        val uBuffer = image.planes[1].buffer
        val vBuffer = image.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(nv21, ImageFormat.NV21, image.width, image.height, null)
        val out = ByteArrayOutputStream()
        yuvImage.compressToJpeg(Rect(0, 0, image.width, image.height), 100, out)

        return BitmapFactory.decodeByteArray(out.toByteArray(), 0, out.size())
    }

    private fun rotateBitmap(bitmap: Bitmap?, rotation: Int): Bitmap? {
        if (bitmap == null) return null
        val matrix = Matrix()
        matrix.postRotate(rotation.toFloat())
        return Bitmap.createBitmap(bitmap, 0, 0, bitmap.width, bitmap.height, matrix, true)
    }

    // ================= UTILS =================

    private fun endCall() {
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
        finish()
    }

    private fun checkPermissions(): Boolean {
        return ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA) ==
                PackageManager.PERMISSION_GRANTED
    }

    private val permissionLauncher =
        registerForActivityResult(ActivityResultContracts.RequestMultiplePermissions()) {
            initAgora()
            startDetectionCamera()
        }

    override fun onDestroy() {
        super.onDestroy()
        cameraExecutor.shutdown()
        mRtcEngine?.leaveChannel()
        RtcEngine.destroy()
    }
}