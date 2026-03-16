package com.example.alertapp.api

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Path

data class RegisterDeviceRequest(val fcm_token: String)

data class AlertItem(
    val id: Int,
    val threat_type: String,
    val detected_at: String,
    val video_path: String,
    val created_at: String
)

data class AlertListResponse(val alerts: List<AlertItem>)

interface AlertApi {

    @POST("api/device/register")
    suspend fun registerDevice(@Body body: RegisterDeviceRequest): Response<DeviceResponse>

    @GET("api/alerts")
    suspend fun getAlerts(): Response<AlertListResponse>

    @GET("api/alerts/{id}/video")
    suspend fun getAlertVideoUrl(@Path("id") id: Int): Response<VideoUrlResponse>
}

data class VideoUrlResponse(val url: String?)

data class DeviceResponse(
    val id: Int,
    val fcm_token: String,
    val name: String?,
    val created_at: String
)
