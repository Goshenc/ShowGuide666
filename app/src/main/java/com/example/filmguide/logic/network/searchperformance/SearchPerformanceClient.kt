package com.example.filmguide.logic.network.searchperformance

import android.util.Log
import com.example.filmguide.logic.network.Url
import com.google.gson.GsonBuilder
import okhttp3.Interceptor
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SearchPerformanceClient {
    private val okHttpClient = OkHttpClient.Builder()
        .addInterceptor { chain ->
            val request = chain.request()

            // 打印请求信息
            Log.d("API 请求", "URL: ${request.url}")
            Log.d("API 请求", "Headers: ${request.headers}")

            val response = chain.proceed(request)
            val responseBody = response.body?.string()

            // 打印原始 JSON 数据
            Log.d("API 响应 JSON", responseBody ?: "响应体为空")

            // 重新创建响应体（关键：必须返回新的响应对象）
            val contentType = response.body?.contentType()
            val newResponse = response.newBuilder()
                .body(okhttp3.ResponseBody.create(contentType, responseBody ?: ""))
                .build()

            return@addInterceptor newResponse // 返回修改后的响应
        }
        .build()

    private val retrofit = Retrofit.Builder()
        .baseUrl("https://wx.maoyan.com")
        .client(okHttpClient)
        .addConverterFactory(GsonConverterFactory.create(
            GsonBuilder()
                .setLenient()
                .serializeNulls()
                .create()
        ))
        .build()

    val searchPerformanceApi: SearchPerformanceApi = retrofit.create(SearchPerformanceApi::class.java)
}