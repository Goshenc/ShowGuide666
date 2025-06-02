// CityApi.kt
package com.example.filmguide.logic.network.city

import retrofit2.http.GET

interface CityApi {
    @GET("dianying/cities.json")
    suspend fun getCities(): CityResponse
}