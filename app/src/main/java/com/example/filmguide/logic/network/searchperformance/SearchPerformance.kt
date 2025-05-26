package com.example.filmguide.logic.network.searchperformance

import com.google.gson.annotations.SerializedName

data class SearchPerformance(
    val code: Int,
    val msg: String,
    val data: PerformanceData,
    val paging: Paging,
    @SerializedName("attrMaps") val attrMaps: AttrMaps,
    val success: Boolean
) {
    // 增强方法：关联明星信息到演出
    fun getEnhancedPerformances(): List<EnhancedPerformance> {
        val celebrityMap = data.celebrityList?.associateBy { it.celebrityId } ?: emptyMap()
        return data.performanceList.map { performance ->
            EnhancedPerformance.from(performance, celebrityMap)
        }
    }
}

data class PerformanceData(
    @SerializedName("celebrityBasicDTOList") val celebrityList: List<Celebrity>? = null,
    @SerializedName("performanceVOList") val performanceList: List<Performance>
)

data class Celebrity(
    val id: Int,
    @SerializedName("celebrityId") val celebrityId: Long,
    @SerializedName("celebrityName") val name: String,
    @SerializedName("headUrl") val avatarUrl: String,
    @SerializedName("aliasName") val alias: String
)

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
    // 原始API可能没有的字段，用于关联明星
    @SerializedName("celebrityIds") val celebrityIds: List<Long>? = null
)

// 新增：增强版的演出数据类（包含明星信息）
data class EnhancedPerformance(
    val performance: Performance,
    val celebrities: List<Celebrity>
) {
    companion object {
        fun from(performance: Performance, celebrityMap: Map<Long, Celebrity>): EnhancedPerformance {
            val relatedCelebrities = performance.celebrityIds?.mapNotNull {
                celebrityMap[it]
            } ?: emptyList()
            return EnhancedPerformance(performance, relatedCelebrities)
        }
    }
}

data class Paging(
    val pageNo: Int,
    val pageSize: Int,
    val totalHits: Int,
    val hasMore: Boolean
)

data class AttrMaps(
    @SerializedName("serverTime") val serverTime: Long
)