package com.example.alertapp.auth

import android.content.Context
import androidx.core.content.edit

private const val PREFS_NAME = "auth_prefs"
private const val KEY_API_TOKEN = "api_token"

object AuthTokenStore {

    fun getApiToken(context: Context): String? =
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .getString(KEY_API_TOKEN, null)

    fun setApiToken(context: Context, token: String) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { putString(KEY_API_TOKEN, token) }
    }

    fun clear(context: Context) {
        context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)
            .edit { remove(KEY_API_TOKEN) }
    }
}

