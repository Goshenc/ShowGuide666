package com.example.filmguide.logic.network.hotmovie

import com.google.gson.annotations.SerializedName

/**
 * 最外层包装类，匹配 JSON 根节点（包含"data"字段）
 */
data class HotMoviesRoot(
    @SerializedName("data") val data: HotMoviesData
)

/**
 * 热映电影数据模型 - 映射JSON中"data"字段的内容
 */
data class HotMoviesData(
    @SerializedName("abbreviation") val abbreviation: String,
    @SerializedName("chiefBonus") val chiefBonus: Map<String, List<ChiefInfo>>,
    @SerializedName("coming") val comingMovies: List<Any>, // 建议根据实际结构定义具体类
    @SerializedName("hot") val hotMovies: List<HotMovie>,
    @SerializedName("movieIds") val movieIds: List<Int>,
    @SerializedName("schemaUrl") val schemaUrl: String,
    @SerializedName("stid") val stid: String,
    @SerializedName("stids") val stids: List<StidInfo>,
    @SerializedName("total") val total: Int
)

/**
 * 主演信息
 */
data class ChiefInfo(
    @SerializedName("chiefAvatarUrl") val avatarUrl: String,
    @SerializedName("chiefName") val name: String
)

/**
 * 热映电影详细信息（含新增字段）
 */
data class HotMovie(
    @SerializedName("bingeWatch") val bingeWatch: Int,
    @SerializedName("cat") val category: String,
    @SerializedName("civilPubSt") val civilPubSt: Int,
    @SerializedName("comingTitle") val comingTitle: String,
    @SerializedName("desc") val description: String?,
    @SerializedName("dir") val director: String,
    @SerializedName("dur") val duration: Int,
    @SerializedName("effectShowNum") val effectShowNum: Int,
    @SerializedName("followst") val followStatus: Int,
    @SerializedName("fra") val foreignReleaseArea: String?,
    @SerializedName("frt") val foreignReleaseTime: String?,
    @SerializedName("globalReleased") val globalReleased: Boolean,
    @SerializedName("haspromotionTag") val hasPromotionTag: Boolean,
    @SerializedName("headLineShow") val headLineShow: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("isRevival") val isRevival: Boolean,
    @SerializedName("late") val late: Boolean,
    @SerializedName("localPubSt") val localPubSt: Int,
    @SerializedName("mark") val mark: Boolean,
    @SerializedName("mk") val mkScore: Double,
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("pn") val pn: Int,
    @SerializedName("preSale") val preSale: Int,
    @SerializedName("preShow") val preShow: Boolean,
    @SerializedName("proScore") val professionalScore: Double,
    @SerializedName("proScoreNum") val professionalScoreNum: Int,
    @SerializedName("pubDate") val pubDate: Long,
    @SerializedName("pubDesc") val pubDescription: String,
    @SerializedName("pubShowNum") val pubShowNum: Int,
    @SerializedName("recentShowDate") val recentShowDate: Long,
    @SerializedName("recentShowNum") val recentShowNum: Int,
    @SerializedName("rt") val releaseTime: String,
    @SerializedName("sc") val score: Double,
    @SerializedName("scm") val scoreComment: String,
    @SerializedName("scoreLabel") val scoreLabel: String,
    @SerializedName("showCinemaNum") val showCinemaNum: Int,
    @SerializedName("showInfo") val showInfo: String,
    @SerializedName("showNum") val showNum: Int,
    @SerializedName("showStateButton") val showStateButton: ShowStateButton,
    @SerializedName("showTimeInfo") val showTimeInfo: String,
    @SerializedName("showst") val showStatus: Int,
    @SerializedName("snum") val snum: Int,
    @SerializedName("src") val source: String,
    @SerializedName("star") val stars: String,
    @SerializedName("totalShowNum") val totalShowNum: Int,
    @SerializedName("ver") val version: String,
    @SerializedName("videoId") val videoId: Int,
    @SerializedName("videoName") val videoName: String,
    @SerializedName("videourl") val videoUrl: String,
    @SerializedName("vnum") val videoNum: Int,
    @SerializedName("vodPlay") val vodPlay: Boolean,
    @SerializedName("wish") val wish: Int,
    @SerializedName("wishst") val wishStatus: Int,

    // 新增字段
    @SerializedName("ftime") val ftime: String? = null,
    @SerializedName("rrt") val releaseReRunTimes: List<String>? = null
)

/**
 * 购票按钮状态
 */
data class ShowStateButton(
    @SerializedName("color") val color: String,
    @SerializedName("content") val content: String,
    @SerializedName("onlyPreShow") val onlyPreShow: Boolean
)

/**
 * STID映射信息
 */
data class StidInfo(
    @SerializedName("movieId") val movieId: Int,
    @SerializedName("stid") val stid: String
)