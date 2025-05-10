package com.example.filmguide.logic.network.expectedmovies

// ExpectedMovies.kt
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class ExpectedMoviesResponse(
    @SerializedName("data") val data: ExpectedMoviesData
)

data class ExpectedMoviesData(
    @SerializedName("success") val success: Boolean,
    @SerializedName("coming") val comingMovies: List<ExpectedMovie>
)

data class ExpectedMovie(
    @SerializedName("id") val id: Int,
    @SerializedName("img") val imageUrl: String,
    @SerializedName("wish") val wishCount: Int,
    @SerializedName("wishst") val wishStatus: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("comingTitle") val comingDate: String
): Serializable