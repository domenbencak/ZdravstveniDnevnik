package com.example.zdravstvenidnevnik.health

import android.content.Context
import org.tensorflow.lite.Interpreter
import java.io.FileInputStream
import java.nio.MappedByteBuffer
import java.nio.channels.FileChannel
import java.util.Locale

class HealthClassifier(context: Context) : AutoCloseable {
    private val interpreter: Interpreter

    init {
        val model = loadModelFile(context, MODEL_FILENAME)
        interpreter = Interpreter(model)
    }

    fun classify(hr: Int, spo2: Int, temp: Float): FloatArray {
        val input = arrayOf(floatArrayOf(hr.toFloat(), spo2.toFloat(), temp))
        val output = Array(1) { FloatArray(3) }

        interpreter.run(input, output)

        return output[0]
    }

    override fun close() {
        interpreter.close()
    }

    private companion object {
        private const val MODEL_FILENAME = "model.tflite"
    }
}

fun loadModelFile(context: Context, filename: String): MappedByteBuffer {
    context.assets.openFd(filename).use { fileDescriptor ->
        FileInputStream(fileDescriptor.fileDescriptor).use { inputStream ->
            val fileChannel = inputStream.channel
            return fileChannel.map(
                FileChannel.MapMode.READ_ONLY,
                fileDescriptor.startOffset,
                fileDescriptor.declaredLength
            )
        }
    }
}

fun mapToStatus(probs: FloatArray): Pair<HealthStatus, Float> {
    val maxIndex = probs.indices.maxByOrNull { probs[it] } ?: 0
    val confidence = probs[maxIndex]

    val status = when (maxIndex) {
        0 -> HealthStatus.NORMAL
        1 -> HealthStatus.ELEVATED
        else -> HealthStatus.CRITICAL
    }

    return status to confidence
}

fun classifyFallback(hr: Int, spo2: Int, temp: Double): HealthStatus {
    return when {
        hr > 120 || spo2 < 90 || temp > 39.0 -> HealthStatus.CRITICAL
        hr > 100 || spo2 < 95 || temp > 37.5 -> HealthStatus.ELEVATED
        else -> HealthStatus.NORMAL
    }
}

fun formatConfidencePercent(confidence: Float): String {
    return String.format(Locale.getDefault(), "%.0f%%", confidence * 100f)
}
