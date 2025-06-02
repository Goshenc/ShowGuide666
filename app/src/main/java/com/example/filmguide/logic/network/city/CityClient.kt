// CityClient.kt
package com.example.filmguide.logic.network.city

import com.example.filmguide.logic.network.Url
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object CityClient {
    private val retrofit = Retrofit.Builder()
        .baseUrl("http://m.maoyan.com/")
        .addConverterFactory(GsonConverterFactory.create())
        .build()

    val cityApi: CityApi = retrofit.create(CityApi::class.java)
}