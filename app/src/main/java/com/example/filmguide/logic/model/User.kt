package com.example.filmguide.logic.model

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "users")
data class User(
    @PrimaryKey val account: String,
    val password: String  // 为示例直接存明文，生产可改为哈希存储
)

