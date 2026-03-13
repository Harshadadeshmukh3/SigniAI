package com.example.signiai

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.*
import android.os.Bundle
import android.widget.ImageButton
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.*
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.camera.view.PreviewView
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import org.tensorflow.lite.Interpreter
import java.io.ByteArrayOutputStream
import java.io.FileInputStream
import java.nio.ByteBuffer
import java.nio.ByteOrder
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel

class VideoCallActivity : AppCompatActivity() {

    private lateinit var previewView: PreviewView
    private lateinit var resultText: TextView
    private lateinit var tflite: Interpreter

    private val cameraPermissionCode = 1001

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_video_call)

        previewView = findViewById(R.id.previewView)
        resultText = findViewById(R.id.resultText)

        val btnEndCall = findViewById<ImageButton>(R.id.btnEndCall)
        btnEndCall.setOnClickListener { finish() }

        // Load ML model
        tflite = Interpreter(loadModelFile())

        checkCameraPermission()
    }

    private fun checkCameraPermission() {

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.CAMERA
            ) == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {

            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.CAMERA),
                cameraPermissionCode
            )
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {

        super.onRequestPermissionsResult(requestCode, permissions, grantResults)

        if (requestCode == cameraPermissionCode &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        }
    }

    private fun startCamera() {
        resultText.text = "Starting camera..."

        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)

        cameraProviderFuture.addListener({

            val cameraProvider = cameraProviderFuture.get()

            val preview = Preview.Builder().build()
            preview.setSurfaceProvider(previewView.surfaceProvider)

            val cameraSelector = CameraSelector.DEFAULT_FRONT_CAMERA

            val imageAnalyzer = ImageAnalysis.Builder()
                .setBackpressureStrategy(ImageAnalysis.STRATEGY_KEEP_ONLY_LATEST)
                .build()

            imageAnalyzer.setAnalyzer(ContextCompat.getMainExecutor(this)) { imageProxy ->

                imageProxy.close()

            }

            cameraProvider.unbindAll()

            cameraProvider.bindToLifecycle(
                this@VideoCallActivity,
                cameraSelector,
                preview,
                imageAnalyzer
            )

        }, ContextCompat.getMainExecutor(this))
    }

    private fun analyzeImage(imageProxy: ImageProxy) {

        val bitmap = imageProxyToBitmap(imageProxy)

        val resizedBitmap = Bitmap.createScaledBitmap(bitmap, 28, 28, true)

        val inputBuffer = ByteBuffer.allocateDirect(4 * 28 * 28)
        inputBuffer.order(ByteOrder.nativeOrder())

        for (y in 0 until 28) {
            for (x in 0 until 28) {

                val pixel = resizedBitmap.getPixel(x, y)

                val gray =
                    ((pixel shr 16 and 0xFF) +
                            (pixel shr 8 and 0xFF) +
                            (pixel and 0xFF)) / 3f / 255f

                inputBuffer.putFloat(gray)
            }
        }

        val output = Array(1) { FloatArray(26) }

        tflite.run(inputBuffer, output)

        val prediction = output[0].indices.maxByOrNull { output[0][it] } ?: -1

        val letters = "ABCDEFGHIJKLMNOPQRSTUVWXYZ"

        if (prediction >= 0) {

            val detectedLetter = letters[prediction]

            runOnUiThread {
                resultText.text = "Detected: $detectedLetter"
            }
        }
    }

    private fun imageProxyToBitmap(imageProxy: ImageProxy): Bitmap {

        val yBuffer = imageProxy.planes[0].buffer
        val uBuffer = imageProxy.planes[1].buffer
        val vBuffer = imageProxy.planes[2].buffer

        val ySize = yBuffer.remaining()
        val uSize = uBuffer.remaining()
        val vSize = vBuffer.remaining()

        val nv21 = ByteArray(ySize + uSize + vSize)

        yBuffer.get(nv21, 0, ySize)
        vBuffer.get(nv21, ySize, vSize)
        uBuffer.get(nv21, ySize + vSize, uSize)

        val yuvImage = YuvImage(
            nv21,
            ImageFormat.NV21,
            imageProxy.width,
            imageProxy.height,
            null
        )

        val out = ByteArrayOutputStream()

        yuvImage.compressToJpeg(
            Rect(0, 0, imageProxy.width, imageProxy.height),
            100,
            out
        )

        val imageBytes = out.toByteArray()

        return BitmapFactory.decodeByteArray(imageBytes, 0, imageBytes.size)
    }

    private fun loadModelFile(): MappedByteBuffer {

        val fileDescriptor = assets.openFd("model.tflite")
        val inputStream = FileInputStream(fileDescriptor.fileDescriptor)
        val fileChannel = inputStream.channel

        return fileChannel.map(
            FileChannel.MapMode.READ_ONLY,
            fileDescriptor.startOffset,
            fileDescriptor.declaredLength
        )
    }

    override fun onDestroy() {
        super.onDestroy()
        tflite.close()
    }
}