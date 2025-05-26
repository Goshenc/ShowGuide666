package com.example.filmguide.logic.network.moviedetail

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class MovieDetailResponse(
    @SerializedName("data") val data: MovieDetailData,
    @SerializedName("cookie") val cookie: CookieData,
    @SerializedName("code") val code: Int,
    @SerializedName("message") val message: String
)

data class MovieDetailData(
    @SerializedName("detailMovie") val detailMovie: DetailMovie
)

data class DetailMovie(
    @SerializedName("availableEpisodes") val availableEpisodes: Int = 0,
    @SerializedName("awardUrl") val awardUrl: String = "",
    @SerializedName("backgroundColor") val backgroundColor: String = "",
    @SerializedName("bingeWatch") val bingeWatch: Int = 0,
    @SerializedName("bingeWatchst") val bingeWatchst: Int = 0,
    @SerializedName("cat") val cat: String = "",
    @SerializedName("comScorePersona") val comScorePersona: Boolean = false,
    @SerializedName("commented") val commented: Boolean = false,
    @SerializedName("dir") val dir: String = "",
    @SerializedName("distributions") val distributions: List<Distribution> = emptyList(),
    @SerializedName("dra") val dra: String = "",
    @SerializedName("dur") val dur: Int = 0,
    @SerializedName("egg") val egg: Boolean = false,
    @SerializedName("enm") val enm: String = "",
    @SerializedName("episodeDur") val episodeDur: Int = 0,
    @SerializedName("episodes") val episodes: Int = 0,
    @SerializedName("globalReleased") val globalReleased: Boolean = false,
    @SerializedName("guideToWish") val guideToWish: Boolean = false,
    @SerializedName("id") val id: Int = 0,
    @SerializedName("img") val imageUrl: String = "",
    @SerializedName("latestEpisode") val latestEpisode: Int = 0,
    @SerializedName("modcsSt") val modcsSt: Boolean = false,
    @SerializedName("movieExtraVO") val movieExtraVO: MovieExtraVO? = null,
    @SerializedName("movieType") val movieType: Int = 0,
    @SerializedName("multiPub") val multiPub: Boolean = false,
    @SerializedName("musicNum") val musicNum: Int = 0,
    @SerializedName("nm") val name: String = "",
    @SerializedName("onSale") val onSale: Boolean = false,
    @SerializedName("onlinePlay") val onlinePlay: Boolean = false,
    @SerializedName("orderSt") val orderSt: Int = 0,
    @SerializedName("oriLang") val oriLang: String = "",
    @SerializedName("photos") val photos: List<String> = emptyList(),
    @SerializedName("pn") val pn: Int = 0,
    @SerializedName("preScorePersona") val preScorePersona: Boolean = false,
    @SerializedName("proScore") val proScore: Int = 0,
    @SerializedName("proScoreNum") val proScoreNum: Int = 0,
    @SerializedName("pubDate") val pubDate: Long = 0,
    @SerializedName("pubDesc") val pubDesc: String = "",
    @SerializedName("rt") val rt: String = "",
    @SerializedName("sc") val sc: Double = 0.0, // 注意：第二个数据中sc为0，第一个为9.6，改为Double
    @SerializedName("scm") val scm: String = "",
    @SerializedName("scoreLabel") val scoreLabel: String = "",
    @SerializedName("shareInfo") val shareInfo: ShareInfo? = null,
    @SerializedName("shortComment") val shortComment: ShortComment? = null,
    @SerializedName("showst") val showst: Int = 0,
    @SerializedName("snum") val snum: Int = 0,
    @SerializedName("src") val src: String = "",
    @SerializedName("star") val star: String = "",
    @SerializedName("trailerStyle") val trailerStyle: Int = 0,
    @SerializedName("type") val type: Int = 0,
    @SerializedName("typeDesc") val typeDesc: String = "",
    @SerializedName("vd") val vd: String = "",
    @SerializedName("ver") val ver: String = "",
    @SerializedName("videoImg") val videoImg: String = "",
    @SerializedName("videoName") val videoName: String = "",
    @SerializedName("videourl") val videourl: String = "",
    @SerializedName("viewedSt") val viewedSt: Int = 0,
    @SerializedName("vnum") val vnum: Int = 0,
    @SerializedName("vodFreeSt") val vodFreeSt: Int = 0,
    @SerializedName("vodPlay") val vodPlay: Boolean = false,
    @SerializedName("vodSt") val vodSt: Int = 0,
    @SerializedName("watched") val watched: Int = 0,
    @SerializedName("wish") val wish: Int = 0,
    @SerializedName("wishst") val wishst: Int = 0,
    @SerializedName("version") val version: String = "",
    @SerializedName("shootingCty") val shootingCty: String? = null
) : Serializable

data class Distribution(
    @SerializedName("movieScoreLevel") val movieScoreLevel: String = "",
    @SerializedName("proportion") val proportion: String = ""
)

data class MovieExtraVO(
    @SerializedName("envelope") val envelope: Envelope? = null
)

data class Envelope(
    @SerializedName("buttonContent") val buttonContent: String = "",
    @SerializedName("schemaUrl") val schemaUrl: String = ""
)

data class ShareInfo(
    @SerializedName("channel") val channel: Int = 0,
    @SerializedName("content") val content: String = "",
    @SerializedName("img") val img: String = "",
    @SerializedName("title") val title: String = "",
    @SerializedName("url") val url: String = ""
)

data class ShortComment(
    @SerializedName("content") val content: String = "",
    @SerializedName("location") val location: Int = 0
)

data class CookieData(
    @SerializedName("from") val from: String = "",
    @SerializedName("uuid_n_v") val uuidNV: String = "",
    @SerializedName("iuuid") val iuuid: String = "",
    @SerializedName("h5guardOpen") val h5guardOpen: String = "",
    @SerializedName("webp") val webp: String = "",
    @SerializedName("ci") val ci: String = "",
    @SerializedName("featrues") val featrues: String = ""
)