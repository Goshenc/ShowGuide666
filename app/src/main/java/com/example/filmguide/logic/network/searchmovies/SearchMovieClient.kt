package com.example.filmguide.logic.network.searchmovies

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object SearchMovieClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("https://m.maoyan.com")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val searchMovieApi: SearchMovieApi = retrofit.create(SearchMovieApi::class.java)
}