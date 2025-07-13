package com.vipulasri.jetinstagram.network

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitProvider {
    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("http://10.0.2.2:8080/") // Android emulator to host machine
        .addConverterFactory(GsonConverterFactory.create())
        .build()
} 