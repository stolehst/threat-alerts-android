package com.example.alertapp.fcm

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import com.example.alertapp.MainActivity
import com.example.alertapp.R
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage


class AlertAppFirebaseMessagingService : FirebaseMessagingService() {

    override fun onNewToken(token: String) {
        super.onNewToken(token)
        // Токен зберегти і відправити на бекенд (викликається також при реєстрації)
        DeviceTokenHolder.saveToken(this, token)
        DeviceRegistrationWorker.enqueueRegister(this, token)
    }

    override fun onMessageReceived(message: RemoteMessage) {
        val title = message.notification?.title ?: getString(R.string.notification_alert_title)
        val body = message.notification?.body
            ?: message.data.let { data ->
                val type = data["type"] ?: data["threat_type"] ?: ""
                val time = data["time"] ?: ""
                if (type.isNotEmpty() || time.isNotEmpty()) "$type $time".trim() else getString(R.string.notification_alert_body)
            }
        val alertId = message.data["alert_id"] ?: message.data["id"] ?: ""

        showNotification(
            title = title,
            body = body,
            alertId = alertId
        )
    }

    private fun showNotification(title: String, body: String, alertId: String) {
        val channelId = NOTIFICATION_CHANNEL_ID
        createNotificationChannel(channelId)

        val intent = Intent(this, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            if (alertId.isNotEmpty()) putExtra(EXTRA_ALERT_ID, alertId)
        }
        val pendingIntent = PendingIntent.getActivity(
            this,
            alertId.hashCode().and(0x7FFFFFFF),
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(this, channelId)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle(title)
            .setContentText(body)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .build()

        val manager = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        manager.notify(NOTIFICATION_ID_BASE + alertId.hashCode().and(0x7FFF), notification)
    }

    private fun createNotificationChannel(channelId: String) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val channel = NotificationChannel(
            channelId,
            getString(R.string.notification_channel_alerts),
            NotificationManager.IMPORTANCE_HIGH
        ).apply { setShowBadge(true) }
        (getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager)
            .createNotificationChannel(channel)
    }

    companion object {
        const val NOTIFICATION_CHANNEL_ID = "alerts"
        private const val NOTIFICATION_ID_BASE = 1000
        const val EXTRA_ALERT_ID = "alert_id"
    }
}
