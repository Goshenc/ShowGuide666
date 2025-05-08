package com.example.filmguide.logic.network.city

// City.kt
import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class CityResponse(
    @SerializedName("data") val data: CityData
)

data class CityData(
    @SerializedName("cts") val cts: List<City>
)

data class City(
    @SerializedName("id")   val id: Int,
    @SerializedName("nm")   val name: String,
    @SerializedName("py")   val pinyin: String
): Serializable


