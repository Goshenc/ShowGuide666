package com.example.filmguide.logic.network.performancedetail

import retrofit2.http.GET
import retrofit2.http.Path

interface PerformanceDetailApi {
    /**
     * 根据演出ID获取演出详情
     * @param performanceId 演出ID
     */
    @GET("maoyansh/myshow/ajax/v2/performance/{performanceId}") // 结合提供的请求地址
    suspend fun getPerformanceDetail(
        @Path("performanceId") performanceId: Long
    ): PerformanceDetailResponse
}