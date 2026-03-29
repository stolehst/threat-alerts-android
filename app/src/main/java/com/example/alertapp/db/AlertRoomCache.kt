package com.example.alertapp.db

import android.content.Context

object AlertRoomCache {

    suspend fun clearAlerts(context: Context) {
        AppDatabase.get(context.applicationContext).alertDao().clear()
    }
}
