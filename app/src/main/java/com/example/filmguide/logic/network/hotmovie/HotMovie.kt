package com.example.filmguide.logic.network.hotmovie

import com.google.gson.annotations.SerializedName

// 顶层响应结构
data class HotMoviesResponse(
    @SerializedName("data") val data: HotMoviesWrapper,
    @SerializedName("cookie") val cookie: Map<String, Any>,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

// 中间层包裹内层数据
data class HotMoviesWrapper(
    @SerializedName("data") val data: InnerData
)

// 核心数据类（原HotMoviesData重命名）
data class InnerData(
    @SerializedName("abbreviation") val abbreviation: String?,
    @SerializedName("chiefBonus") val chiefBonus: Map<String, List<ChiefInfo>>?,
    @SerializedName("coming") val comingMovies: List<Any>?, // 根据实际数据结构调整
    @SerializedName("hot") val hotMovies: List<HotMovie>?,
    @SerializedName("movieIds") val movieIds: List<Int>?,
    @SerializedName("schemaUrl") val schemaUrl: String?,
    @SerializedName("stid") val stid: String?,
    @SerializedName("stids") val stids: List<StidInfo>?,
    @SerializedName("total") val total: Int // 新增total字段
)

// 演员信息
data class ChiefInfo(
    @SerializedName("chiefAvatarUrl") val avatarUrl: String,
    @SerializedName("chiefName") val name: String
)

// 热映电影详细信息（补全所有字段）
data class HotMovie(
    @SerializedName("bingeWatch") val bingeWatch: Int,
    @SerializedName("cat") val category: String,
    @SerializedName("civilPubSt") val civilPubSt: Int,
    @SerializedName("comingTitle") val comingTitle: String,
    @SerializedName("desc") val description: String,
    @SerializedName("dir") val director: String,
    @SerializedName("dur") val duration: Int,
    @SerializedName("effectShowNum") val effectShowNum: Int,
    @SerializedName("followst") val followst: Int,
    @SerializedName("fra") val franchise: String?, // 可空
    @SerializedName("frt") val franchiseReleaseTime: String?, // 可空
    @SerializedName("globalReleased") val globalReleased: Boolean,
    @SerializedName("haspromotionTag") val hasPromotionTag: Boolean,
    @SerializedName("headLineShow") val headLineShow: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("isRevival") val isRevival: Boolean,
    @SerializedName("late") val late: Boolean,
    @SerializedName("localPubSt") val localPubSt: Int,
    @SerializedName("mark") val mark: Boolean,
    @SerializedName("mk") val mk: Double,
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("pn") val pn: Int,
    @SerializedName("preSale") val preSale: Int,
    @SerializedName("preShow") val preShow: Boolean,
    @SerializedName("proScore") val professionalScore: Double,
    @SerializedName("proScoreNum") val professionalScoreCount: Int,
    @SerializedName("pubDate") val publishDate: Long,
    @SerializedName("pubDesc") val publishDescription: String,
    @SerializedName("pubShowNum") val pubShowNum: Int,
    @SerializedName("recentShowDate") val recentShowDate: Long,
    @SerializedName("rt") val releaseTime: String,
    @SerializedName("sc") val score: Double,
    @SerializedName("scm") val scoreComment: String,
    @SerializedName("scoreLabel") val scoreLabel: String,
    @SerializedName("showCinemaNum") val showCinemaNum: Int,
    @SerializedName("showInfo") val showInfo: String,
    @SerializedName("showStateButton") val showStateButton: ShowStateButton,
    @SerializedName("showTimeInfo") val showTimeInfo: String,
    @SerializedName("showst") val showStatus: Int,
    @SerializedName("snum") val snum: Int,
    @SerializedName("src") val source: String,
    @SerializedName("star") val starring: String,
    @SerializedName("totalShowNum") val totalShowNum: Int,
    @SerializedName("ver") val version: String,
    @SerializedName("videoId") val videoId: Int,
    @SerializedName("videoName") val videoName: String,
    @SerializedName("videourl") val videoUrl: String,
    @SerializedName("vnum") val vnum: Int,
    @SerializedName("vodPlay") val vodPlay: Boolean,
    @SerializedName("wish") val wish: Int,
    @SerializedName("wishst") val wishStatus: Int
)

// 购票按钮状态
data class ShowStateButton(
    @SerializedName("color") val color: String,
    @SerializedName("content") val content: String,
    @SerializedName("onlyPreShow") val onlyPreShow: Boolean
)

// STID映射信息
data class StidInfo(
    @SerializedName("movieId") val movieId: Int,
    @SerializedName("stid") val stid: String
)