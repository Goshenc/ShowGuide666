package com.example.filmguide.logic

import android.content.Context
import android.database.sqlite.SQLiteConstraintException
import com.example.filmguide.logic.model.User

class AuthRepository(context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao()

    /** 注册：返回 true=成功，false=账号·已存在 */
    suspend fun register(account: String, pwd: String): Boolean {
        return try {
            val user = User(account, pwd)
            userDao.insert(user)
            true
        } catch (e: SQLiteConstraintException) {
            // 主键冲突（同邮箱已注册）
            false
        }
    }

    /** 登录：匹配成功返回 User，否则返回 null */
    suspend fun login(account: String, pwd: String): User? {
        return userDao.findUser(account, pwd)
    }
}
