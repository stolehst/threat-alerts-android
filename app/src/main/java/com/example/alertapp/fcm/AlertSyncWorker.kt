package com.example.alertapp.fcm

import android.content.Context
import androidx.work.CoroutineWorker
import androidx.work.WorkerParameters
import androidx.work.workDataOf
import com.example.alertapp.auth.AuthTokenStore
import com.example.alertapp.repo.AlertsRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

/**
 * Triggered by FCM to refresh Room cache without user action.
 * If alert_id is present, sync only that alert; otherwise refresh list.
 */
class AlertSyncWorker(
    context: Context,
    params: WorkerParameters
) : CoroutineWorker(context, params) {

    override suspend fun doWork(): Result = withContext(Dispatchers.IO) {
        // If user isn't activated, don't attempt authorized calls.
        if (AuthTokenStore.getApiToken(applicationContext).isNullOrBlank()) {
            return@withContext Result.success()
        }

        val repo = AlertsRepository(applicationContext)
        val rawId = inputData.getString(KEY_ALERT_ID)?.trim().orEmpty()
        val id = rawId.toIntOrNull()

        try {
            if (id != null) {
                repo.refreshAlertFromNetwork(id)
            } else {
                repo.refreshAlertsFromNetwork()
            }
            Result.success()
        } catch (e: Exception) {
            Result.retry()
        }
    }

    companion object {
        const val KEY_ALERT_ID = "alert_id"

        fun enqueue(context: Context, alertId: String?) {
            val data = if (!alertId.isNullOrBlank()) {
                workDataOf(KEY_ALERT_ID to alertId)
            } else {
                workDataOf()
            }
            val request = androidx.work.OneTimeWorkRequestBuilder<AlertSyncWorker>()
                .setInputData(data)
                .build()
            androidx.work.WorkManager.getInstance(context).enqueue(request)
        }
    }
}

