package com.example.signiai

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import androidx.camera.core.CameraSelector
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.content.ContextCompat

class VideoCallActivity : AppCompatActivity() {

    private lateinit var tflite: Interpreter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)
        val previewView = findViewById<PreviewView>(R.id.previewView)

        // Camera permission
        if (ContextCompat.checkSelfPermission(
                this,
                android.Manifest.permission.CAMERA
            ) != android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            requestPermissions(arrayOf(android.Manifest.permission.CAMERA), 100)

        } else {

            startCamera()

        }

        // Load TensorFlow model safely
        try {
            tflite = Interpreter(loadModelFile())
            Toast.makeText(this, "Model loaded successfully", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Toast.makeText(this, "Model loading failed", Toast.LENGTH_LONG).show()
            e.printStackTrace()
        }

        // Get meeting ID
        val data = intent?.data
        val meetingIdFromLink = data?.lastPathSegment
        val meetingIdFromApp = intent.getStringExtra("MEETING_ID")

        val meetingId = meetingIdFromLink ?: meetingIdFromApp

        Toast.makeText(this, "Joining meeting: $meetingId", Toast.LENGTH_LONG).show()

        if (::tflite.isInitialized) {
            runModelTest()
        }
    }

    // ⭐ CAMERA FUNCTION (OUTSIDE onCreate)
    private fun startCamera() {
        Toast.makeText(this,"Camera started",Toast.LENGTH_SHORT).show()

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()

            val previewView = findViewById<PreviewView>(R.id.previewView)

            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this,
                cameraSelector,
                preview
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun loadModelFile(): MappedByteBuffer {

        val fileDescriptor = assets.openFd("sign_language_model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    private fun runModelTest() {

        val input = Array(1) { FloatArray(63) }
        val output = Array(1) { FloatArray(26) }

        tflite.run(input, output)

        Toast.makeText(this, "Model test run successful", Toast.LENGTH_SHORT).show()
    }

    // ⭐ ADD THIS FUNCTION HERE
    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == 100 && grantResults.isNotEmpty() &&
            grantResults[0] == android.content.pm.PackageManager.PERMISSION_GRANTED
        ) {

            startCamera()

        } else {

            Toast.makeText(this, "Camera permission denied", Toast.LENGTH_LONG).show()

        }
    }
}