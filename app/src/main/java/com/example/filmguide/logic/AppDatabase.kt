package com.example.filmguide.logic

import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import android.content.Context
import androidx.room.TypeConverters
import com.example.filmguide.logic.dao.MovieDao
import com.example.filmguide.logic.dao.PerformanceDao
import com.example.filmguide.logic.dao.UserDao
import com.example.filmguide.logic.model.User
import com.example.filmguide.logic.network.moviedetail.MovieEntity
import com.example.filmguide.logic.network.performancedetail.PerformanceEntity


@Database(
    entities = [User::class, MovieEntity::class, PerformanceEntity::class],
    version = 5,
    exportSchema = false
)

abstract class AppDatabase : RoomDatabase() {
    abstract fun userDao(): UserDao
    abstract fun movieDao(): MovieDao
    abstract fun performanceDao(): PerformanceDao // 新增 DAO

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase =
            INSTANCE ?: synchronized(this) {
                INSTANCE ?: Room.databaseBuilder(
                    context.applicationContext,
                    AppDatabase::class.java,
                    "app_database.db"
                )

                    .fallbackToDestructiveMigration() // 开发阶段使用
                    .build().also { INSTANCE = it }
            }
    }
}