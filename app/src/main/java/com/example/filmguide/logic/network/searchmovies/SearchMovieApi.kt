package com.example.filmguide.logic.network.searchmovies

import com.example.filmguide.logic.network.searchmovies.SearchMovieResponse
import retrofit2.http.GET
import retrofit2.http.Query

interface SearchMovieApi {
    @GET("/search/movies")
    suspend fun searchMovies(
        @Query("ci") cityId: Int,
        @Query("kw") keyword: String,
        @Query("limit") limit: Int = 30,
        @Query("offset") offset: Int = 1
    ): SearchMovieResponse
}