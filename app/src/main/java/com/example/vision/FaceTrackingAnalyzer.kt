package com.example.vision

import android.annotation.SuppressLint
import androidx.camera.core.ImageAnalysis
import androidx.camera.core.ImageProxy
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.face.FaceDetection
import com.google.mlkit.vision.face.FaceDetectorOptions
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

class FaceTrackingAnalyzer : ImageAnalysis.Analyzer {
    private val _targetX = MutableStateFlow(0f) // -1.0 (left) to 1.0 (right)
    val targetX: StateFlow<Float> = _targetX.asStateFlow()

    private val _targetY = MutableStateFlow(0f) // -1.0 (up) to 1.0 (down)
    val targetY: StateFlow<Float> = _targetY.asStateFlow()

    private val _isFaceDetected = MutableStateFlow(false)
    val isFaceDetected: StateFlow<Boolean> = _isFaceDetected.asStateFlow()

    private val realTimeOpts = FaceDetectorOptions.Builder()
        .setPerformanceMode(FaceDetectorOptions.PERFORMANCE_MODE_FAST)
        .setContourMode(FaceDetectorOptions.CONTOUR_MODE_NONE)
        .setClassificationMode(FaceDetectorOptions.CLASSIFICATION_MODE_NONE)
        .build()

    private val detector = FaceDetection.getClient(realTimeOpts)

    @SuppressLint("UnsafeOptInUsageError")
    override fun analyze(imageProxy: ImageProxy) {
        val mediaImage = imageProxy.image
        if (mediaImage != null) {
            try {
                val image = InputImage.fromMediaImage(mediaImage, imageProxy.imageInfo.rotationDegrees)
                detector.process(image)
                    .addOnSuccessListener { faces ->
                        if (faces.isNotEmpty()) {
                            val firstFace = faces.first()
                            val bounds = firstFace.boundingBox
                            val imgWidth = if (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270) imageProxy.height else imageProxy.width
                            val imgHeight = if (imageProxy.imageInfo.rotationDegrees == 90 || imageProxy.imageInfo.rotationDegrees == 270) imageProxy.width else imageProxy.height

                            // Calculate normalized face centroid (-1f to 1f)
                            val centerX = bounds.centerX().toFloat()
                            val centerY = bounds.centerY().toFloat()

                            // Mirror X for front camera view
                            val normX = -((centerX / imgWidth) * 2f - 1f).coerceIn(-1f, 1f)
                            val normY = ((centerY / imgHeight) * 2f - 1f).coerceIn(-1f, 1f)

                            _targetX.value = normX
                            _targetY.value = normY
                            _isFaceDetected.value = true
                        } else {
                            _isFaceDetected.value = false
                        }
                    }
                    .addOnFailureListener {
                        _isFaceDetected.value = false
                    }
                    .addOnCompleteListener {
                        imageProxy.close()
                    }
            } catch (_: Exception) {
                _isFaceDetected.value = false
                try {
                    imageProxy.close()
                } catch (_: Exception) {}
            }
        } else {
            imageProxy.close()
        }
    }

    fun setSimulatedPosition(x: Float, y: Float, detected: Boolean) {
        _targetX.value = x.coerceIn(-1f, 1f)
        _targetY.value = y.coerceIn(-1f, 1f)
        _isFaceDetected.value = detected
    }
}
