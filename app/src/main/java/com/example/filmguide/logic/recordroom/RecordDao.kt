package com.example.filmguide.logic.recordroom

import androidx.lifecycle.LiveData
import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.Query
@Dao
interface RecordDao {
    @Insert
    suspend fun insertRecord(recordEntity: RecordEntity)
    //SELECT * FROM records
//查询数据库中 recordss 表的所有列。
//
//WHERE title LIKE :query OR article LIKE :query
//指定条件：
//
//title LIKE :query：表示记录的标题中包含与 query 参数匹配的字符串。
//OR article LIKE :query：或日记的正文（article）中包含与 query 参数匹配的字符串。
//LIKE 操作符：允许使用 SQL 通配符（如 %），通常需要在调用方法时传入类似 %关键字% 的字符串，表示模糊搜索。
    @Query("SELECT * FROM records WHERE title LIKE :query OR article LIKE :query")//:query：占位符，表示一个动态变量，通常由程序传入
    fun searchRecords(query: String): LiveData<List<RecordEntity>>//livedata

    @Delete
    suspend fun deleteRecord(recordEntity: RecordEntity)



    @Query("SELECT * FROM records")
    fun getRecords(): LiveData<List<RecordEntity>>//livedata

    @Query("SELECT * FROM records WHERE id = :recordId")
    suspend fun getRecordById(recordId: Int): RecordEntity
}