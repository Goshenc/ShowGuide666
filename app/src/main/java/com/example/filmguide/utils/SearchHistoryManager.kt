package com.example.filmguide.utils

import android.content.Context
import android.content.SharedPreferences
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

/**
 * 搜索历史记录管理器
 * 管理用户的搜索历史，支持保存、获取、删除搜索记录
 */
class SearchHistoryManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("search_history", Context.MODE_PRIVATE)
    private val gson = Gson()
    private val maxHistorySize = 20 // 最大保存20条搜索记录
    
    /**
     * 搜索历史记录数据类
     */
    data class SearchHistoryItem(
        val keyword: String,
        val timestamp: Long,
        val searchType: String = "all" // all, movie, performance
    )
    
    /**
     * 保存搜索记录
     */
    fun saveSearchHistory(keyword: String, searchType: String = "all") {
        if (keyword.trim().isEmpty()) return
        
        val historyList = getSearchHistory().toMutableList()
        
        // 移除重复的关键词
        historyList.removeAll { it.keyword == keyword }
        
        // 添加新的搜索记录到列表开头
        val newItem = SearchHistoryItem(
            keyword = keyword.trim(),
            timestamp = System.currentTimeMillis(),
            searchType = searchType
        )
        historyList.add(0, newItem)
        
        // 限制历史记录数量
        if (historyList.size > maxHistorySize) {
            historyList.removeAt(historyList.size - 1)
        }
        
        // 保存到SharedPreferences
        val json = gson.toJson(historyList)
        prefs.edit().putString("search_history", json).apply()
    }
    
    /**
     * 获取搜索历史记录
     */
    fun getSearchHistory(): List<SearchHistoryItem> {
        val json = prefs.getString("search_history", null) ?: return emptyList()
        return try {
            val type = object : TypeToken<List<SearchHistoryItem>>() {}.type
            gson.fromJson(json, type) ?: emptyList()
        } catch (e: Exception) {
            emptyList()
        }
    }
    
    /**
     * 根据类型获取搜索历史
     */
    fun getSearchHistoryByType(searchType: String): List<SearchHistoryItem> {
        return getSearchHistory().filter { it.searchType == searchType }
    }
    
    /**
     * 删除单条搜索记录
     */
    fun deleteSearchHistory(keyword: String) {
        val historyList = getSearchHistory().toMutableList()
        historyList.removeAll { it.keyword == keyword }
        
        val json = gson.toJson(historyList)
        prefs.edit().putString("search_history", json).apply()
    }
    
    /**
     * 清空所有搜索历史
     */
    fun clearAllHistory() {
        prefs.edit().remove("search_history").apply()
    }
    
    /**
     * 获取热门搜索关键词（基于搜索频率）
     */
    fun getPopularKeywords(limit: Int = 10): List<String> {
        val historyList = getSearchHistory()
        val keywordCount = mutableMapOf<String, Int>()
        
        // 统计关键词出现次数
        historyList.forEach { item ->
            keywordCount[item.keyword] = keywordCount.getOrDefault(item.keyword, 0) + 1
        }
        
        // 按出现次数排序，返回前N个
        return keywordCount.entries
            .sortedByDescending { it.value }
            .take(limit)
            .map { it.key }
    }
    
    /**
     * 搜索历史统计
     */
    fun getHistoryStats(): Map<String, Any> {
        val historyList = getSearchHistory()
        val totalSearches = historyList.size
        val uniqueKeywords = historyList.map { it.keyword }.distinct().size
        val movieSearches = historyList.count { it.searchType == "movie" }
        val performanceSearches = historyList.count { it.searchType == "performance" }
        
        return mapOf(
            "totalSearches" to totalSearches,
            "uniqueKeywords" to uniqueKeywords,
            "movieSearches" to movieSearches,
            "performanceSearches" to performanceSearches
        )
    }
}
