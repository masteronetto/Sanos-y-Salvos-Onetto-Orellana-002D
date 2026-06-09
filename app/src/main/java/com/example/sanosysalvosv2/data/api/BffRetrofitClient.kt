package com.example.sanosysalvosv2.data.api

import com.example.sanosysalvosv2.BuildConfig
import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.ConcurrentHashMap
import java.util.concurrent.TimeUnit

object BffRetrofitClient {
    private val gson = GsonBuilder().create()
    private val retrofitCache = ConcurrentHashMap<String, Retrofit>()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(4, TimeUnit.SECONDS)
        .readTimeout(10, TimeUnit.SECONDS)
        .build()

    fun retrofit(): Retrofit {
        // Expected Android emulator URL is http://10.0.2.2:8080/ (injected via BuildConfig.BFF_BASE_URL).
        val baseUrl = BuildConfig.BFF_BASE_URL
        return retrofitCache.getOrPut(baseUrl) {
            Retrofit.Builder()
                .baseUrl(baseUrl)
                .client(okHttp)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build()
        }
    }
}
