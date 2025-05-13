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
    @SerializedName("availableEpisodes") val availableEpisodes: Int,
    @SerializedName("awardUrl") val awardUrl: String,
    @SerializedName("backgroundColor") val backgroundColor: String,
    @SerializedName("bingeWatch") val bingeWatch: Int,
    @SerializedName("bingeWatchst") val bingeWatchst: Int,
    @SerializedName("cat") val cat: String,
    @SerializedName("comScorePersona") val comScorePersona: Boolean,
    @SerializedName("commented") val commented: Boolean,
    @SerializedName("dir") val dir: String,
    @SerializedName("distributions") val distributions: List<Distribution>,
    @SerializedName("dra") val dra: String,
    @SerializedName("dur") val dur: Int,
    @SerializedName("egg") val egg: Boolean,
    @SerializedName("enm") val enm: String,
    @SerializedName("episodeDur") val episodeDur: Int,
    @SerializedName("episodes") val episodes: Int,
    @SerializedName("globalReleased") val globalReleased: Boolean,
    @SerializedName("guideToWish") val guideToWish: Boolean,
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("latestEpisode") val latestEpisode: Int,
    @SerializedName("modcsSt") val modcsSt: Boolean,
    @SerializedName("movieExtraVO") val movieExtraVO: MovieExtraVO,
    @SerializedName("movieType") val movieType: Int,
    @SerializedName("multiPub") val multiPub: Boolean,
    @SerializedName("musicNum") val musicNum: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("onSale") val onSale: Boolean,
    @SerializedName("onlinePlay") val onlinePlay: Boolean,
    @SerializedName("orderSt") val orderSt: Int,
    @SerializedName("oriLang") val oriLang: String,
    @SerializedName("photos") val photos: List<String>,
    @SerializedName("pn") val pn: Int,
    @SerializedName("preScorePersona") val preScorePersona: Boolean,
    @SerializedName("proScore") val proScore: Int,
    @SerializedName("proScoreNum") val proScoreNum: Int,
    @SerializedName("pubDate") val pubDate: Long,
    @SerializedName("pubDesc") val pubDesc: String,
    @SerializedName("rt") val rt: String,
    @SerializedName("sc") val sc: Int,
    @SerializedName("scm") val scm: String,
    @SerializedName("scoreLabel") val scoreLabel: String,
    @SerializedName("shareInfo") val shareInfo: ShareInfo,
    @SerializedName("shortComment") val shortComment: ShortComment,
    @SerializedName("showst") val showst: Int,
    @SerializedName("snum") val snum: Int,
    @SerializedName("src") val src: String,
    @SerializedName("star") val star: String,
    @SerializedName("trailerStyle") val trailerStyle: Int,
    @SerializedName("type") val type: Int,
    @SerializedName("typeDesc") val typeDesc: String,
    @SerializedName("vd") val vd: String,
    @SerializedName("ver") val ver: String,
    @SerializedName("videoImg") val videoImg: String,
    @SerializedName("videoName") val videoName: String,
    @SerializedName("videourl") val videourl: String,
    @SerializedName("viewedSt") val viewedSt: Int,
    @SerializedName("vnum") val vnum: Int,
    @SerializedName("vodFreeSt") val vodFreeSt: Int,
    @SerializedName("vodPlay") val vodPlay: Boolean,
    @SerializedName("vodSt") val vodSt: Int,
    @SerializedName("watched") val watched: Int,
    @SerializedName("wish") val wish: Int,
    @SerializedName("wishst") val wishst: Int,
    @SerializedName("version") val version: String
) : Serializable

data class Distribution(
    @SerializedName("movieScoreLevel") val movieScoreLevel: String,
    @SerializedName("proportion") val proportion: String
)

data class MovieExtraVO(
    @SerializedName("envelope") val envelope: Envelope
)

data class Envelope(
    @SerializedName("buttonContent") val buttonContent: String,
    @SerializedName("schemaUrl") val schemaUrl: String
)

data class ShareInfo(
    @SerializedName("channel") val channel: Int,
    @SerializedName("content") val content: String,
    @SerializedName("img") val img: String,
    @SerializedName("title") val title: String,
    @SerializedName("url") val url: String
)

data class ShortComment(
    @SerializedName("content") val content: String,
    @SerializedName("location") val location: Int
)

data class CookieData(
    @SerializedName("from") val from: String,
    @SerializedName("uuid_n_v") val uuidNV: String,
    @SerializedName("iuuid") val iuuid: String,
    @SerializedName("h5guardOpen") val h5guardOpen: String,
    @SerializedName("webp") val webp: String,
    @SerializedName("ci") val ci: String,
    @SerializedName("featrues") val featrues: String
)