package com.example.filmguide.logic.network.hotmovie

import retrofit2.http.GET
import retrofit2.http.Query

interface HotMovieApi {
    @GET("mmdb/movie/v3/list/hot.json")
    suspend fun getHotMovies(
        @Query("channelId") channelId: Int = 4,
        @Query("ci") cityId: Int,
        @Query("ct") cityName: String
    ): HotMoviesRoot
}