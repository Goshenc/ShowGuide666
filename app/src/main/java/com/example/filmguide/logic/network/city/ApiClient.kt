package com.example.filmguide.logic.network.city

import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object ApiClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://26.152.170.72:8082/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val cityApi: CityApi = retrofit.create(CityApi::class.java)
}
