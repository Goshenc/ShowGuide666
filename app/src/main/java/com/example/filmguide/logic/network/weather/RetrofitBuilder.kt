package com.example.filmguide.logic.network.weather


import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory

object RetrofitBuilder {
    val getCityInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://geoapi.qweather.com/v2/city/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }

    val getWeatherInstance: Retrofit by lazy {
        Retrofit.Builder()
            .baseUrl("https://devapi.qweather.com/v7/weather/")
            .addConverterFactory(GsonConverterFactory.create())
            .build()
    }
}