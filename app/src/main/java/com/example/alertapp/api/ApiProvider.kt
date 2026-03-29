package com.example.alertapp.api

import android.content.Context
import com.example.alertapp.BuildConfig
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object ApiProvider {

    @Volatile
    private var retrofit: Retrofit? = null

    fun getAlertApi(context: Context): AlertApi {
        val existing = retrofit
        if (existing != null) return existing.create(AlertApi::class.java)

        synchronized(this) {
            val again = retrofit
            if (again != null) return again.create(AlertApi::class.java)

            val okHttp = OkHttpClient.Builder()
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .addInterceptor(AuthInterceptor(context.applicationContext))
                .apply {
                    if (BuildConfig.DEBUG) {
                        addInterceptor(
                            HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
                        )
                    }
                }
                .build()

            val created = Retrofit.Builder()
                .baseUrl(BuildConfig.API_BASE_URL)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create())
                .build()

            retrofit = created
            return created.create(AlertApi::class.java)
        }
    }
}
