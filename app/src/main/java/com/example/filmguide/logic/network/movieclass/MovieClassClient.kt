package com.example.filmguide.logic.network.movieclass

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object MovieClassClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl(Url.BASE_URL)
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val movieClassApi: MovieClassApi = retrofit.create(MovieClassApi::class.java)
}