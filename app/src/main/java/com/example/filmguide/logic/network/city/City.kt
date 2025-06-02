package com.example.filmguide.logic.network.city

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CityResponse(
    @SerializedName("cts") val cts: List<City>
)

data class City(
    @SerializedName("id") val id: Int,
    @SerializedName("nm") val name: String,
    @SerializedName("py") val pinyin: String
): Serializable