package com.example.filmguide.logic.network.allperformance

import com.example.filmguide.logic.network.allperformance.AllPerformanceResponse
import retrofit2.http.GET
import retrofit2.http.QueryMap

interface AllPerformanceApi {
    /**
     * 获取演出列表
     * @param params 可选参数（如pageNo, pageSize等）
     */
    @GET("myshow/ajax/performances/1;st=0;p=1;s=20;tft=0;marketLevel=0")
    suspend fun getPerformances(@QueryMap params: Map<String, String> = emptyMap()): AllPerformanceResponse
}