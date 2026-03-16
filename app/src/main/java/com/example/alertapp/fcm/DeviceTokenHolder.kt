package com.example.alertapp.fcm

import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit

private const val PREFS_NAME = "device_token_prefs"
private const val KEY_FCM_TOKEN = "fcm_token"

object DeviceTokenHolder {

    private fun prefs(context: Context): SharedPreferences =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

    fun saveToken(context: Context, token: String) {
        prefs(context).edit { putString(KEY_FCM_TOKEN, token) }
    }

    fun getToken(context: Context): String? =
        prefs(context).getString(KEY_FCM_TOKEN, null)
}
