package com.example.filmguide.logic.network.hotmovie

import retrofit2.http.GET
import retrofit2.http.Query

// HotMoviesApi.kt
interface HotMovieApi {
    @GET("movie/hot")
    suspend fun getHotMovies(
        @Query("ci") cityId: Int,
        @Query("ct") cityName: String
    ): HotMoviesResponse
}