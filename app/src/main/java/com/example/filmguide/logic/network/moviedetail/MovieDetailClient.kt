package com.example.filmguide.logic.network.moviedetail

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MovieDetailClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Url.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val movieDetailApi: MovieDetailApi = retrofit.create(MovieDetailApi::class.java)
}