package com.example.filmguide.logic.network.expectedmovies

// ExpectedMoviesApi.kt
import retrofit2.http.GET
import retrofit2.http.Query

interface ExpectedMoviesApi {
    @GET("movie/expected")
    suspend fun getExpectedMovies(
        @Query("ci") cityId: Int,
        @Query("ct") cityName: String,
        @Query("limit") limit: Int = 100,
        @Query("offset") offset: Int = 0
    ): ExpectedMoviesResponse
}