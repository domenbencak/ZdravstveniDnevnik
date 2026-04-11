package com.example.zdravstvenidnevnik.network

import retrofit2.http.GET

interface MockHealthApi {
    @GET("heartrate")
    suspend fun getHeartRate(): HeartRateResponse

    @GET("spo2")
    suspend fun getSpO2(): SpO2Response

    @GET("temperature")
    suspend fun getTemperature(): TemperatureResponse
}

data class HeartRateResponse(
    val bpm: Int
)

data class SpO2Response(
    val percentage: Int
)

data class TemperatureResponse(
    val celsius: Double
)
