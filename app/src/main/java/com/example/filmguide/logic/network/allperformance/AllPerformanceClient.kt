package com.example.filmguide.logic.network.allperformance

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object AllPerformanceClient {

    val retrofit: Retrofit = Retrofit.Builder()
        .baseUrl("https://m.dianping.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val allPerformanceApi: AllPerformanceApi = retrofit.create(AllPerformanceApi::class.java)
}