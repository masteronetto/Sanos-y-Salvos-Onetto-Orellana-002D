package com.sanosysalvos.androidapp.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    // Cambia la BASE_URL por la URL pública de tu BFF
    // Ejemplo: private const val BASE_URL = "https://mi-bff.example.com/"
    private const val BASE_URL = "https://REPLACE_WITH_YOUR_BFF/" // <- cambiar aquí

    private val gson = GsonBuilder().create()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}
