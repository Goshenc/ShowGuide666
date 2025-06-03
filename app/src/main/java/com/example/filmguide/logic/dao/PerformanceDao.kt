package com.example.filmguide.logic.dao

import androidx.room.*
import com.example.filmguide.logic.network.performancedetail.PerformanceEntity

@Dao
interface PerformanceDao {
    // 插入单个演出
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformance(performance: PerformanceEntity): Long

    // 插入多个演出
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertPerformances(performances: List<PerformanceEntity>): List<Long>

    // 根据ID查询演出
    @Query("SELECT * FROM performances WHERE performanceId = :id")
    suspend fun getPerformanceById(id: Long): PerformanceEntity?

    // 查询所有演出
    @Query("SELECT * FROM performances ORDER BY name ASC")
    suspend fun getAllPerformances(): List<PerformanceEntity>

    // 根据名称模糊查询演出
    @Query("SELECT * FROM performances WHERE name LIKE '%' || :name || '%'")
    suspend fun searchPerformancesByName(name: String): List<PerformanceEntity>

    // 更新演出信息
    @Update
    suspend fun updatePerformance(performance: PerformanceEntity): Int

    // 根据ID删除演出
    @Query("DELETE FROM performances WHERE performanceId = :id")
    suspend fun deletePerformanceById(id: Long): Int

    // 删除所有演出
    @Query("DELETE FROM performances")
    suspend fun deleteAllPerformances(): Int

    // 查询是否存在某个演出
    @Query("SELECT EXISTS(SELECT * FROM performances WHERE performanceId = :id)")
    suspend fun existsPerformance(id: Long): Boolean
}