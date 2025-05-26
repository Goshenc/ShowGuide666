package com.example.filmguide.logic.network.searchperformance

import com.example.filmguide.logic.network.Url
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SearchPerformanceClient {
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wx.maoyan.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val searchPerformanceApi: SearchPerformanceApi = retrofit.create(SearchPerformanceApi::class.java)
}