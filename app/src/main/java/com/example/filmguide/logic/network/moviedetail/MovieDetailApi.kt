package com.example.filmguide.logic.network.moviedetail

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieDetailApi {
    @GET("movie/detail")
    suspend fun getMovieDetail(
        @Query("movieId") movieId: Int
    ): MovieDetailResponse
}