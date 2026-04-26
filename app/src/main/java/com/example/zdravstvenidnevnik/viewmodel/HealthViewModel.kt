package com.example.zdravstvenidnevnik.viewmodel

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.zdravstvenidnevnik.health.HealthClassifier
import com.example.zdravstvenidnevnik.health.HealthStatus
import com.example.zdravstvenidnevnik.health.classifyFallback
import com.example.zdravstvenidnevnik.health.mapToStatus
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

data class HealthUiState(
    val hr: Int? = null,
    val spo2: Int? = null,
    val temp: Double? = null,
    val status: HealthStatus? = null,
    val confidence: Float? = null,
    val isLoading: Boolean = false,
    val usedFallback: Boolean = false
)

class HealthViewModel(application: Application) : AndroidViewModel(application) {
    private val classifier: HealthClassifier? = runCatching {
        HealthClassifier(application.applicationContext)
    }.onFailure { exception ->
        Log.w(TAG, "TFLite model unavailable, fallback classification will be used.", exception)
    }.getOrNull()

    private val _uiState = MutableStateFlow(HealthUiState())
    val uiState: StateFlow<HealthUiState> = _uiState.asStateFlow()

    fun classifyMeasurements(hr: Int, spo2: Int, temp: Double) {
        _uiState.update {
            it.copy(
                hr = hr,
                spo2 = spo2,
                temp = temp,
                isLoading = true
            )
        }

        viewModelScope.launch {
            val result = withContext(Dispatchers.Default) {
                runCatching {
                    val activeClassifier = classifier
                        ?: error("TFLite classifier not initialized.")
                    val probabilities = activeClassifier.classify(
                        hr = hr,
                        spo2 = spo2,
                        temp = temp.toFloat()
                    )
                    val (status, confidence) = mapToStatus(probabilities)
                    ClassificationResult(
                        status = status,
                        confidence = confidence,
                        usedFallback = false
                    )
                }.getOrElse { exception ->
                    Log.w(TAG, "TFLite classification failed, fallback used.", exception)
                    ClassificationResult(
                        status = classifyFallback(hr, spo2, temp),
                        confidence = 1.0f,
                        usedFallback = true
                    )
                }
            }

            _uiState.update {
                it.copy(
                    status = result.status,
                    confidence = result.confidence,
                    isLoading = false,
                    usedFallback = result.usedFallback
                )
            }
        }
    }

    override fun onCleared() {
        classifier?.close()
        super.onCleared()
    }

    private data class ClassificationResult(
        val status: HealthStatus,
        val confidence: Float,
        val usedFallback: Boolean
    )

    private companion object {
        private const val TAG = "HealthViewModel"
    }
}
