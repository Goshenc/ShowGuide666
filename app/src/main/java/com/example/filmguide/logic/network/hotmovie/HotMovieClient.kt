package com.example.filmguide.logic.network.hotmovie

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object HotMovieClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://i.maoyan.com/api/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val hotMoviesApi: HotMovieApi = retrofit.create(HotMovieApi::class.java)
}