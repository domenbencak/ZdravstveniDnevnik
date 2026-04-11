package com.example.zdravstvenidnevnik.network

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.os.Handler
import android.os.Looper
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withContext
import retrofit2.HttpException
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.io.IOException
import kotlin.coroutines.resume
import kotlin.math.roundToInt

class SensorRepository(
    context: Context
) {
    private val sensorManager = context.getSystemService(Context.SENSOR_SERVICE) as SensorManager

    private val mockHealthApis: List<MockHealthApi> = BASE_URL_CANDIDATES.map(::createMockHealthApi)

    fun hasHeartRateSensor(): Boolean {
        return sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE) != null
    }

    suspend fun readHeartRateFromSensor(timeoutMillis: Long = 4_000L): Int? {
        return withContext(Dispatchers.Main.immediate) {
            val heartRateSensor = sensorManager.getDefaultSensor(Sensor.TYPE_HEART_RATE)
                ?: return@withContext null
            val handler = Handler(Looper.getMainLooper())

            suspendCancellableCoroutine { continuation ->
                var completed = false
                lateinit var timeoutRunnable: Runnable

                fun complete(value: Int?) {
                    if (!completed && continuation.isActive) {
                        completed = true
                        continuation.resume(value)
                    }
                }

                val listener = object : SensorEventListener {
                    override fun onSensorChanged(event: SensorEvent) {
                        val measuredValue = event.values.firstOrNull()
                            ?.roundToInt()
                            ?.takeIf { it > 0 }

                        if (measuredValue != null) {
                            handler.removeCallbacks(timeoutRunnable)
                            sensorManager.unregisterListener(this)
                            complete(measuredValue)
                        }
                    }

                    override fun onAccuracyChanged(sensor: Sensor?, accuracy: Int) = Unit
                }

                timeoutRunnable = Runnable {
                    sensorManager.unregisterListener(listener)
                    complete(null)
                }

                continuation.invokeOnCancellation {
                    handler.removeCallbacks(timeoutRunnable)
                    sensorManager.unregisterListener(listener)
                }

                try {
                    sensorManager.registerListener(
                        listener,
                        heartRateSensor,
                        SensorManager.SENSOR_DELAY_NORMAL
                    )
                    handler.postDelayed(timeoutRunnable, timeoutMillis)
                } catch (_: SecurityException) {
                    handler.removeCallbacks(timeoutRunnable)
                    sensorManager.unregisterListener(listener)
                    complete(null)
                }
            }
        }
    }

    suspend fun readHeartRateFromApi(): Int {
        return performApiCall { api ->
            api.getHeartRate().bpm
        }
    }

    suspend fun readSpO2FromApi(): Int {
        return performApiCall { api ->
            api.getSpO2().percentage
        }
    }

    suspend fun readTemperatureFromApi(): Double {
        return performApiCall { api ->
            api.getTemperature().celsius
        }
    }

    private suspend fun <T> performApiCall(call: suspend (MockHealthApi) -> T): T {
        var lastFailure: Throwable? = null

        for (api in mockHealthApis) {
            try {
                return call(api)
            } catch (exception: IOException) {
                lastFailure = exception
            } catch (exception: HttpException) {
                lastFailure = exception
            }
        }

        throw IllegalStateException(
            "Mock API call failed for all configured base URLs.",
            lastFailure
        )
    }

    private fun createMockHealthApi(baseUrl: String): MockHealthApi {
        return Retrofit.Builder()
            .baseUrl(baseUrl)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(MockHealthApi::class.java)
    }

    private companion object {
        private val BASE_URL_CANDIDATES = listOf(
            "http://10.0.2.2:3000/",
            "http://10.0.3.2:3000/",
            "http://localhost:3000/"
        )
    }
}
