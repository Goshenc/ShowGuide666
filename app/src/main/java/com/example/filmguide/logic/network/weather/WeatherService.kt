package com.example.filmguide.logic.network.weather



import com.example.filmguide.logic.model.CityItem
import com.example.filmguide.logic.model.WeatherItem
import retrofit2.Response
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("lookup")
    suspend fun getCity(
        @Query("key") key: String="670ca929136a456992608cd2e794df24",
        @Query("location") location: String
    ): Response<CityItem>

    @GET("3d")
    suspend fun getWeather(
        @Query("key") key: String="670ca929136a456992608cd2e794df24",
        @Query("location") locationId: String
    ): Response<WeatherItem>
}
/*
//使用enqueue回调时的weatherService
package com.example.diary_final.service

import com.example.diary_final.apiitem.CityItem
import com.example.diary_final.apiitem.WeatherItem
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface WeatherService {
    @GET("lookup")
    fun getCity(
        @Query("key") key: String = "670ca929136a456992608cd2e794df24",
        @Query("location") location: String
    ): Call<CityItem>

    @GET("3d")
    fun getWeather(
        @Query("key") key: String = "670ca929136a456992608cd2e794df24",
        @Query("location") locationId: String
    ): Call<WeatherItem>
}

*/