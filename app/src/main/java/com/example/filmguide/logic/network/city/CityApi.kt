package com.example.filmguide.logic.network.city

// CityApi.kt
import retrofit2.http.Field
import retrofit2.http.FormUrlEncoded
import retrofit2.http.GET
import retrofit2.http.POST
import retrofit2.http.Query

interface CityApi {
    @GET("city")
    suspend fun getCities(): CityResponse


}
