package com.example.filmguide.logic.network.moviedetail

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MovieDetailResponse(
    @SerializedName("data") val data: MovieDetailData,
    @SerializedName("cookie") val cookie: CookieInfo,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

data class MovieDetailData(
    @SerializedName("detailMovie") val detailMovie: DetailMovie
)

data class DetailMovie(
    @SerializedName("availableEpisodes") val availableEpisodes: Int,
    @SerializedName("awardUrl") val awardUrl: String,
    @SerializedName("backgroundColor") val backgroundColor: String,
    @SerializedName("bingeWatch") val bingeWatch: Int,
    @SerializedName("bingeWatchst") val bingeWatchStatus: Int,
    @SerializedName("cat") val categories: String,
    @SerializedName("comScorePersona") val communityScorePersona: Boolean,
    @SerializedName("commented") val isCommented: Boolean,
    @SerializedName("dir") val director: String,
    @SerializedName("distributions") val scoreDistributions: List<ScoreDistribution>,
    @SerializedName("dra") val description: String,
    @SerializedName("dur") val duration: Int,
    @SerializedName("egg") val hasEgg: Boolean,
    @SerializedName("enm") val englishName: String,
    @SerializedName("episodeDur") val episodeDuration: Int,
    @SerializedName("episodes") val episodes: Int,
    @SerializedName("fra") val franchise: String,
    @SerializedName("frt") val franchiseReleaseTime: String,
    @SerializedName("globalReleased") val isGlobalReleased: Boolean,
    @SerializedName("guideToWish") val guideToWish: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("latestEpisode") val latestEpisode: Int,
    @SerializedName("modcsSt") val modcsStatus: Boolean,
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("multiPub") val isMultiPub: Boolean,
    @SerializedName("musicName") val musicName: String,
    @SerializedName("musicNum") val musicCount: Int,
    @SerializedName("musicStar") val musicStars: String,
    @SerializedName("nm") val name: String,
    @SerializedName("onSale") val isOnSale: Boolean,
    @SerializedName("onlinePlay") val isOnlinePlay: Boolean,
    @SerializedName("orderSt") val orderStatus: Int,
    @SerializedName("oriLang") val originalLanguage: String,
    @SerializedName("photos") val photos: List<String>,
    @SerializedName("pn") val pn: Int,
    @SerializedName("preScorePersona") val preScorePersona: Boolean,
    @SerializedName("proScore") val professionalScore: Double,
    @SerializedName("proScoreNum") val professionalScoreCount: Int,
    @SerializedName("pubDate") val publishDate: Long,
    @SerializedName("pubDesc") val publishDescription: String,
    @SerializedName("rt") val releaseDate: String,
    @SerializedName("sc") val score: Double,
    @SerializedName("scm") val scoreComment: String,
    @SerializedName("scoreLabel") val scoreLabel: String,
    @SerializedName("shareInfo") val shareInfo: ShareInfo,
    @SerializedName("showst") val showStatus: Int,
    @SerializedName("snum") val scoreNumber: Int,
    @SerializedName("src") val source: String,
    @SerializedName("star") val starring: String,
    @SerializedName("trailerStyle") val trailerStyle: Int,
    @SerializedName("type") val type: Int,
    @SerializedName("typeDesc") val typeDescription: String,
    @SerializedName("vd") val videoUrl: String,
    @SerializedName("ver") val versions: String,
    @SerializedName("videoImg") val videoImageUrl: String,
    @SerializedName("videoName") val videoName: String,
    @SerializedName("videourl") val videoPlayUrl: String,
    @SerializedName("viewedSt") val viewedStatus: Int,
    @SerializedName("vnum") val videoCount: Int,
    @SerializedName("vodFreeSt") val vodFreeStatus: Int,
    @SerializedName("vodPlay") val isVodPlay: Boolean,
    @SerializedName("vodSt") val vodStatus: Int,
    @SerializedName("watched") val watchedCount: Int,
    @SerializedName("wish") val wishCount: Int,
    @SerializedName("wishst") val wishStatus: Int,
    @SerializedName("version") val version: String
): Serializable

data class ScoreDistribution(
    @SerializedName("movieScoreLevel") val scoreLevel: String,
    @SerializedName("proportion") val proportion: String
): Serializable

data class ShareInfo(
    @SerializedName("channel") val channel: Int,
    @SerializedName("content") val content: String,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String
): Serializable

data class CookieInfo(
    @SerializedName("from") val from: String,
    @SerializedName("uuid_n_v") val uuidNV: String,
    @SerializedName("iuuid") val iuuid: String,
    @SerializedName("h5guardOpen") val h5guardOpen: String,
    @SerializedName("webp") val webp: String,
    @SerializedName("ci") val cityInfo: String,
    @SerializedName("featrues") val features: String
): Serializable