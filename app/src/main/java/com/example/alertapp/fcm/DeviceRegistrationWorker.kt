package com.example.alertapp.fcm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import com.example.alertapp.api.ApiProvider
import com.example.alertapp.api.RegisterDeviceRequest
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Відправляє FCM-токен на бекенд (POST /api/device/register).
 * Викликається при onNewToken і при першому запуску / з налаштувань.
 */
class DeviceRegistrationWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        val token = inputData.getString(KEY_TOKEN)
            ?: DeviceTokenHolder.getToken(applicationContext)
        if (token.isNullOrBlank()) return@withContext Result.failure()
        try {
            val response = ApiProvider.alertApi.registerDevice(RegisterDeviceRequest(token))
            if (response.isSuccessful) Result.success() else Result.retry()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_TOKEN = "fcm_token"
        const val WORK_NAME = "device_register"

        fun enqueueRegister(context: Context, token: String) {
            val request = androidx.work.OneTimeWorkRequestBuilder<DeviceRegistrationWorker>()
                .setInputData(androidx.work.workDataOf(KEY_TOKEN to token))
                .build()
            androidx.work.WorkManager.getInstance(context).enqueue(request)
        }
    }
}
