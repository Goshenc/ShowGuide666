package com.example.filmguide.logic.network.expectedmovies

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExpectedMoviesClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://m.maoyan.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val expectedMoviesApi: ExpectedMoviesApi = retrofit.create(ExpectedMoviesApi::class.java)
}