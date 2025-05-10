package com.example.filmguide.logic.network.hotmovie

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HotMovieClient {

    val hotMovieApi: HotMovieApi by lazy {
        Retrofit.Builder()
            .baseUrl(Url.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create()) // 用于解析JSON
            .build()
            .create(HotMovieApi::class.java)
    }
}
