package com.example.filmguide.logic.network.city

// CityApi.kt
import retrofit2.http.GET

interface CityApi {
    @GET("city")
    suspend fun getCities(): CityResponse
}
