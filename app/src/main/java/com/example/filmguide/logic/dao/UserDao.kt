package com.example.filmguide.logic.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.filmguide.logic.model.User
import kotlinx.coroutines.flow.Flow

@Dao
interface UserDao {
    /** 注册：插入新用户，若 account 冲突则抛异常 */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: User)

    /** 登录：根据 account+password 查找用户 */
    @Query("SELECT * FROM users WHERE account = :account AND password = :pwd LIMIT 1")
    suspend fun findUser(account: String, pwd: String): User?

    @Query("SELECT * FROM users")
    fun getAllUsers(): Flow<List<User>>
}
