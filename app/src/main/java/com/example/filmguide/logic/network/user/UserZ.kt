package com.example.filmguide.logic.network.user




data class UserZ(
    val id: Int? = null,
    val username: String,
    val password: String,
    val email: String,
    val createdAt: String? = null      // ISO 字符串形式，后端生成
)
