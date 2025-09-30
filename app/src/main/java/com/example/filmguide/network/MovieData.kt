package com.example.filmguide.network

import java.io.Serializable

/**
 * 电影数据模型
 */
data class MovieData(
    val id: String,
    val title: String,
    val genre: String,
    val rating: Double,
    val releaseDate: String,
    val director: String,
    val actors: List<String>,
    val description: String,
    val posterUrl: String,
    val boxOffice: String? = null
) : Serializable
