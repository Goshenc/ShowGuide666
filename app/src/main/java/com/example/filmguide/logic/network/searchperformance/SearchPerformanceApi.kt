package com.example.filmguide.logic.network.searchperformance

import com.example.filmguide.logic.network.searchperformance.SearchPerformance

import retrofit2.http.GET
import retrofit2.http.Query

import retrofit2.http.*

interface SearchPerformanceApi {
    /**
     * 搜索演出信息（适配分号分隔的参数格式）
     * @param fixedParam 路径中的固定参数（默认值 "0"）
     * @param st st 参数（默认值 0）
     * @param pageNo 页码（默认值 1）
     * @param pageSize 每页数量（默认值 20）
     * @param keyword 搜索关键词（必填）
     */
    @GET("maoyansh/myshow/ajax/search/{fixedParam}")
    suspend fun searchPerformances(
        @Path("fixedParam") fixedParam: String = "0",
        @Query("st") st: Int = 0,
        @Query("p") pageNo: Int = 1,
        @Query("s") pageSize: Int = 20,
        @Query("k") keyword: String
    ): SearchPerformance
}
