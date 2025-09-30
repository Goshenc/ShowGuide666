package com.example.filmguide.logic.recordroom

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "records")
data class RecordEntity(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val title: String,
    val article: String,
    val localImagePath: String?,
    val networkImageLink: String?,
    val date: String,
    val weather: String,
    val location: String,
    val rating: Float,
    val type: String = "movie",
    val posterUrl: String = "",
    val isInWishlist: Boolean = false
    )
