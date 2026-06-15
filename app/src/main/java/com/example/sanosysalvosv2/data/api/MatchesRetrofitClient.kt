package com.example.sanosysalvosv2.data.api

import com.google.gson.GsonBuilder
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object MatchesRetrofitClient {
    private val gson = GsonBuilder().create()

    private val okHttp = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://x8ki-letl-twmt.n7.xano.io/api:sanos-y-salvos-matches/")
        .client(okHttp)
        .addConverterFactory(GsonConverterFactory.create(gson))
        .build()
}
