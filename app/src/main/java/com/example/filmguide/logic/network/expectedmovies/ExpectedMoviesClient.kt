package com.example.filmguide.logic.network.expectedmovies


import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ExpectedMoviesClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Url.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val expectedMoviesApi: ExpectedMoviesApi = retrofit.create(ExpectedMoviesApi::class.java)
}