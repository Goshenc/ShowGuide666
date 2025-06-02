package com.example.filmguide.logic.network.expectedmovies

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExpectedMoviesResponse(
    @SerializedName("coming") val comingMovies: List<ExpectedMovie>,
    @SerializedName("movieIds") val movieIds: List<Int>
)

data class ExpectedMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("haspromotionTag") val hasPromotionTag: Boolean,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("version") val version: String,
    @SerializedName("nm") val name: String,
    @SerializedName("preShow") val preShow: Boolean,
    @SerializedName("sc") val sc: Int,
    @SerializedName("globalReleased") val globalReleased: Boolean,
    @SerializedName("wish") val wishCount: Int,
    @SerializedName("star") val star: String,
    @SerializedName("rt") val releaseDate: String,
    @SerializedName("showInfo") val showInfo: String?,
    @SerializedName("showst") val showStatus: Int,
    @SerializedName("wishst") val wishStatus: Int,
    @SerializedName("comingTitle") val comingDate: String,
    @SerializedName("showStateButton") val showStateButton: ShowStateButton?
): Serializable

data class ShowStateButton(
    @SerializedName("color") val color: String,
    @SerializedName("content") val content: String,
    @SerializedName("onlyPreShow") val onlyPreShow: Boolean,
    @SerializedName("shadowColor") val shadowColor: String
)