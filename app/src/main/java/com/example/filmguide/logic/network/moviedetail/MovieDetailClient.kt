package com.example.filmguide.logic.network.moviedetail

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MovieDetailClient {
    private const val BASE_URL = "https://m.maoyan.com/"

    private val retrofit = Retrofit.Builder()
        .baseUrl(BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val movieDetailApi: MovieDetailApi = retrofit.create(MovieDetailApi::class.java)
}