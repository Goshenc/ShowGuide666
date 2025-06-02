package com.example.filmguide.logic.network.moviedetail

import retrofit2.http.GET
import retrofit2.http.Query

interface MovieDetailApi {
    @GET("ajax/detailmovie")
    suspend fun getMovieDetail(
        @Query("movieId") movieId: Int
    ): MovieDetailData
}