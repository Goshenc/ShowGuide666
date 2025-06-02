package com.example.filmguide.logic.network.searchmovies

import com.google.gson.annotations.SerializedName

data class SearchMovieResponse(
    @SerializedName("type") val type: String,
    @SerializedName("total") val total: Int,
    @SerializedName("movies") val movies: List<Movie>? = emptyList(),
    @SerializedName("keyword") val keyword: String,
    @SerializedName("allowRefund") val allowRefund: Boolean,
    @SerializedName("endorse") val endorse: Boolean,
    @SerializedName("snack") val snack: Boolean,
    @SerializedName("vipTag") val vipTag: Boolean
)

data class Movie(
    // 基本信息
    @SerializedName("id") val movieId: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("enm") val englishName: String? = null,
    @SerializedName("cat") val category: String,
    @SerializedName("src") val source: String,

    // 多媒体信息
    @SerializedName("img") val imageUrl: String,
    @SerializedName("ver") val versionFormat: String? = null,
    @SerializedName("version") val versionType: String? = null,

    // 时间信息
    @SerializedName("dur") val duration: Int,
    @SerializedName("rt") val releaseTime: String,
    @SerializedName("frt") val franchiseReleaseTime: String? = null,
    @SerializedName("ftime") val firstTime: String? = null,
    @SerializedName("pubDesc") val releaseDescription: String? = null,

    // 评分信息
    @SerializedName("sc") val score: Double,
    @SerializedName("wish") val wishCount: Int,
    @SerializedName("wishst") val wishStatus: Int = 0,

    // 人员信息
    @SerializedName("dir") val director: String? = null,
    @SerializedName("star") val starring: String? = null,
    @SerializedName("act") val actors: String? = null,

    // 状态信息
    @SerializedName("globalReleased") val isGlobalReleased: Boolean,
    @SerializedName("onlinePlay") val isOnlinePlay: Boolean,
    @SerializedName("vodPlay") val isVodPlay: Boolean,

    // 类型信息
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("movieTypeDesc") val movieTypeDesc: String,

    // 其他可选字段
    @SerializedName("fra") val franchise: String? = null,
    @SerializedName("movieAlias") val alias: String? = null,
    @SerializedName("showst") val showStatus: Int = 0,
    @SerializedName("stype") val specialType: Int = 0,
    @SerializedName("renderStyle") val renderStyle: Int = 0,
    @SerializedName("show") val showInfo: String? = null
)