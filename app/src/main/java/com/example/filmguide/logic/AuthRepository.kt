package com.example.filmguide.logic

import android.content.Context
import com.example.filmguide.logic.model.User

class AuthRepository(context: Context) {
    private val userDao = AppDatabase.getInstance(context).userDao()

    /** 注册：返回 true=成功，false=账号·已存在 */
    suspend fun register(account: String, pwd: String): Boolean {
        return try {
            userDao.insert(User(account, pwd))
            true
        } catch (e: Exception) {
            false
        }
    }

    /** 登录：匹配成功返回 User，否则返回 null */
    suspend fun login(account: String, pwd: String): User? {
        return userDao.findUser(account, pwd)
    }
}
