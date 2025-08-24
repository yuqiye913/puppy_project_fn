package com.vipulasri.jetinstagram.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor

object RetrofitInstance {
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
        setLevel(HttpLoggingInterceptor.Level.HEADERS)
    }
    private val tokenInterceptor = TokenInterceptor()
    private val client = OkHttpClient.Builder()
        .addInterceptor(logging)
        .addInterceptor(tokenInterceptor)
        .build()

    val api: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl("http://10.0.2.2:8080/api/") // Local development (Android emulator)
        // Alternative URLs:
        // .baseUrl("https://backend-production-fae6.up.railway.app/api/") // Railway production URL
        // .baseUrl("http://192.168.1.100:8080/api/") // Local network IP
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
} 