package com.example.filmguide.logic.network.movieclass

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MovieClassResponse(
    @SerializedName("data") val data: MovieClassData
)

data class MovieClassData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("classicMovies") val classicMovies: ClassicMovies
)

data class ClassicMovies(
    @SerializedName("list") val movieList: List<MovieItem>
)

data class MovieItem(
    @SerializedName("act") val actors: String,
    @SerializedName("cat") val categories: String,
    @SerializedName("dir") val director: String,
    @SerializedName("dur") val duration: Int,
    @SerializedName("enm") val englishName: String,
    @SerializedName("globalReleased") val isGlobalReleased: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("movieAlias") val alias: String,
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("movieTypeDesc") val movieTypeDesc: String,
    @SerializedName("nm") val name: String,
    @SerializedName("onlinePlay") val isOnlinePlay: Boolean,
    @SerializedName("pubDesc") val publishDescription: String,
    @SerializedName("renderStyle") val renderStyle: Int,
    @SerializedName("rt") val releaseDate: String,
    @SerializedName("sc") val score: Double,
    @SerializedName("show") val showInfo: String,
    @SerializedName("showst") val showStatus: Int,
    @SerializedName("src") val source: String,
    @SerializedName("star") val starring: String,
    @SerializedName("type") val type: Int,
    @SerializedName("ver") val version: String,
    @SerializedName("vodPlay") val isVodPlay: Boolean,
    @SerializedName("wish") val wishCount: Int,
    @SerializedName("wishst") val wishStatus: Int
): Serializable