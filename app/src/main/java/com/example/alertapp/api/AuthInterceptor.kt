package com.example.alertapp.api

import android.content.Context
import com.example.alertapp.auth.AuthTokenStore
import okhttp3.Interceptor
import okhttp3.Response

class AuthInterceptor(private val appContext: Context) : Interceptor {

    override fun intercept(chain: Interceptor.Chain): Response {
        val req = chain.request()
        val path = req.url.encodedPath
        if (path.endsWith("/api/activate")) return chain.proceed(req)

        val token = AuthTokenStore.getApiToken(appContext)
        if (token.isNullOrBlank()) return chain.proceed(req)

        val authed = req.newBuilder()
            .header("Authorization", "Bearer $token")
            .build()
        return chain.proceed(authed)
    }
}

