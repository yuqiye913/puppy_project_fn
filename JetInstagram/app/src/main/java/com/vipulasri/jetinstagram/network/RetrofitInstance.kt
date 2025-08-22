package com.vipulasri.jetinstagram.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }
    private val tokenInterceptor = TokenInterceptor()
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(tokenInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("https://backend-production-fae6.up.railway.app/api/") // Railway production URL
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
} 