package com.example.filmguide.logic.network.hotmovie

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HotMovieClient {

    private const val BASE_URL = "https://your.api.base.url/" // 替换为你实际的API基础URL

    val hotMovieApi: HotMovieApi by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // 用于解析JSON
            .build()
            .create(HotMovieApi::class.java)
    }
}
