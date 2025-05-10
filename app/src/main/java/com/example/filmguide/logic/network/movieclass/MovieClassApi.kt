package com.example.filmguide.logic.network.movieclass

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieClassApi {
    @GET("movie/class")
    suspend fun getMoviesByClass(
        @Query("sortId") sortId: Int = 1,
        @Query("showType") showType: Int = 3,
        @Query("limit") limit: Int = 10,
        @Query("offset") offset: Int = 0
    ): MovieClassResponse
}