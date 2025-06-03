package com.example.filmguide.logic.network.performancedetail


import androidx.room.*
import com.example.filmguide.logic.network.allperformance.AllPerformanceResponse
import com.example.filmguide.logic.network.searchperformance.Celebrity
import com.example.filmguide.logic.network.searchperformance.EnhancedPerformance
import com.example.filmguide.logic.network.searchperformance.Performance

import java.io.Serializable


@Entity(tableName = "performances")
data class PerformanceEntity(
    @PrimaryKey val performanceId: Long,
    val name: String,
    val venue: String,
    val posterUrl: String,
    val timeRange: String,
    val priceRange: String,
    val city: String,
    val lowestPrice: Double,
    val isSoldOut: Boolean,
    val detailLink: String?,
    val star: String?,
    val starHeadUrl: String?
) : Serializable

fun convertToPerformanceEntity(enhancedPerformance: EnhancedPerformance): PerformanceEntity {
    return PerformanceEntity(
        performanceId = enhancedPerformance.performance.id,
        name = enhancedPerformance.performance.name,
        venue = enhancedPerformance.performance.venue,
        posterUrl = enhancedPerformance.performance.posterUrl,
        timeRange = enhancedPerformance.performance.timeRange,
        priceRange = enhancedPerformance.performance.priceRange,
        city = enhancedPerformance.performance.city,
        lowestPrice = enhancedPerformance.performance.lowestPrice,
        isSoldOut = enhancedPerformance.performance.isSoldOut,
        detailLink = enhancedPerformance.performance.detailLink,
        star = enhancedPerformance.celebrities[0].celebrityName,
        starHeadUrl = enhancedPerformance.celebrities[0].headUrl
    )
}

fun convertToPerformanceEntity(performance: AllPerformanceResponse.PerformanceData): PerformanceEntity {
    return PerformanceEntity(
        performanceId = performance.performanceId,
        name = performance.name,
        venue = performance.shopName,
        posterUrl = performance.posterUrl,
        timeRange = performance.showTimeRange,
        priceRange = performance.priceRange,
        city = performance.cityName,
        lowestPrice = performance.lowestPrice,
        isSoldOut = performance.self,
        detailLink = null,
        star = null,
        starHeadUrl = null
    )
}

