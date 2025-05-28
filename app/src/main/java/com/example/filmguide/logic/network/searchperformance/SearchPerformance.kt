package com.example.filmguide.logic.network.searchperformance

import com.google.gson.annotations.SerializedName
import java.io.Serializable

data class SearchPerformance(
    val code: Int,
    val msg: String,
    val data: PerformanceData,
    val paging: Paging,
    @SerializedName("attrMaps") val attrMaps: AttrMaps,
    val success: Boolean
) : Serializable {

    fun getAllCelebrities(): List<Celebrity> = data.celebrityBasicDTOList ?: emptyList()

    fun getEnhancedPerformancesWithAllCelebrities(): List<EnhancedPerformance> {
        val allCelebrities = data.celebrityBasicDTOList.orEmpty()
        return data.performanceList.map { performance ->
            EnhancedPerformance(performance, allCelebrities)
        }
    }
}


data class PerformanceData(
    @SerializedName("celebrityBasicDTOList") val celebrityBasicDTOList: List<Celebrity>? = null ,
    @SerializedName("performanceVOList") val performanceList: List<Performance>
) : Serializable


data class Celebrity(
    @SerializedName("id") val id: Int,
    @SerializedName("celebrityId") val celebrityId: Long,
    @SerializedName("celebrityName") val celebrityName: String,
    @SerializedName("headUrl") val headUrl: String,
    @SerializedName("aliasName") val aliasName: String,
    @SerializedName("categoryIds") val categoryIds: List<Int>? = null,
    @SerializedName("type") val type: Int? = null,
    @SerializedName("status") val status: Int? = null,
    @SerializedName("backgroundUrl") val backgroundUrl: String? = null,

) : Serializable


data class Performance(
    @SerializedName("performanceId") val id: Long,
    val name: String,
    @SerializedName("shopName") val venue: String,
    @SerializedName("posterUrl") val posterUrl: String,
    @SerializedName("showTimeRange") val timeRange: String,
    @SerializedName("priceRange") val priceRange: String,
    @SerializedName("cityName") val city: String,
    @SerializedName("lowestPrice") val lowestPrice: Double,
    @SerializedName("stockOut") val isSoldOut: Boolean,
    @SerializedName("shareLink") val detailLink: String,
    @SerializedName("celebrityRelationVOS") val celebrityRelations: List<CelebrityRelation> = emptyList()
) : Serializable


data class CelebrityRelation(
    @SerializedName("celebrityId") val celebrityId: Long
) : Serializable


data class EnhancedPerformance(
    val performance: Performance,
    val celebrities: List<Celebrity>
) : Serializable {
    companion object {
        fun from(
            performance: Performance,
            celebrityMap: Map<Long, Celebrity>
        ): EnhancedPerformance {
            val celebrityIds = performance.celebrityRelations.map { it.celebrityId }
            val relatedCelebrities = celebrityIds.mapNotNull { celebrityMap[it] }
            return EnhancedPerformance(performance, relatedCelebrities)
        }
    }
}

// 分页信息类
data class Paging(
    val pageNo: Int,
    val pageSize: Int,
    val totalHits: Int,
    val hasMore: Boolean
) : Serializable

// 属性映射类
data class AttrMaps(
    @SerializedName("serverTime") val serverTime: Long
) : Serializable