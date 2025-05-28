package com.example.filmguide.logic.network.performancedetail

import com.example.filmguide.logic.network.Url
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object PerformanceDetailClient {
    private val okHttpClient = OkHttpClient.Builder()
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wx.maoyan.com") // 基础URL与请求地址匹配
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val performanceDetailApi: PerformanceDetailApi = retrofit.create(PerformanceDetailApi::class.java)
}