package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object XanoRetrofitClient {
    // Auth APIs removed. Retained for non-auth Xano calls if any exist.
    // NO serializeNulls() — null fields are omitted from JSON.
    // This is important for report creation: optional String? fields must not be sent as empty strings.
    // If no remaining usages: safe to delete in a future cleanup session.
    private val gson = GsonBuilder().create()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)  // Important for large base64
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BuildConfig.XANO_BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}
