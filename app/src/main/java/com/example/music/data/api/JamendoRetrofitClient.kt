package com.example.music.data.api

import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object JamendoRetrofitClient {

    private const val BASE_URL = "https://api.jamendo.com/v3.0/"

    // Free demo client_id from Jamendo's official getting-started docs.
    // Register your own free key at https://developer.jamendo.com to get higher limits.
    const val CLIENT_ID = "b6747d04"

    private val okHttpClient = OkHttpClient.Builder()
        .connectTimeout(30, TimeUnit.SECONDS)
        .readTimeout(30, TimeUnit.SECONDS)
        .build()

    val api: JamendoApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(okHttpClient)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(JamendoApiService::class.java)
    }
}
