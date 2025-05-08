package com.example.filmguide.logic.network.hotmovie

import retrofit2.http.GET
import retrofit2.http.Query

interface HotMovieApi {

    // 假设API的URL是以 cityId 作为查询参数获取热映电影列表
    @GET("path/to/your/api/endpoint")
    suspend fun getHotMovies(@Query("cityId") cityId: Int): HotMoviesResponse
}
