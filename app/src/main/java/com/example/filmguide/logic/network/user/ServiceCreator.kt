package com.example.filmguide.logic.network.user

import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.scalars.ScalarsConverterFactory
import retrofit2.converter.gson.GsonConverterFactory

object ServiceCreator {
    private const val BASE_URL = "http://26.152.170.72:8082/"

    // 1. logging interceptor
    private val logging = HttpLoggingInterceptor().apply {
        level = HttpLoggingInterceptor.Level.BODY
    }

    // 2. OkHttpClient
    private val httpClient = OkHttpClient.Builder()
        .addInterceptor(logging)
        .build()

    // 3. Retrofit 配置，先 scalars 再 gson
    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(httpClient)
        // **先处理纯文本响应**，保证 text/plain 能被当 String 直接返回
        .addConverterFactory(ScalarsConverterFactory.create())
        // 再处理 JSON，如果接口返回 JSON 时才会用到
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val apiService: ApiService = retrofit.create(ApiService::class.java)
}
