package com.example.filmguide.logic.network.expectedmovies

import retrofit2.http.GET
import retrofit2.http.Query

interface ExpectedMoviesApi {
    @GET("ajax/comingList")
    suspend fun getExpectedMovies(
        @Query("token") token: String = "",
        @Query("limit") limit: Int = 20,
        @Query("offset") offset: Int = 0
    ): ExpectedMoviesResponse
}