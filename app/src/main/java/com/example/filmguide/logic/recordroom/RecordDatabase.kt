package com.example.filmguide.logic.recordroom

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase

@Database(entities = [RecordEntity::class], version = 3, exportSchema = false)
abstract class RecordDatabase : RoomDatabase() {
    abstract fun recordDao(): RecordDao

    companion object {
        @Volatile
        private var instance: RecordDatabase? = null

        fun getInstance(context: Context): RecordDatabase {
            return instance ?: synchronized(this) {
                instance ?: buildDatabase(context).also { instance = it }
            }
        }

        private fun buildDatabase(context: Context): RecordDatabase {
            return Room.databaseBuilder(
                context.applicationContext,
                RecordDatabase::class.java,
                "records database"
            )
                .fallbackToDestructiveMigration() // 避免数据库版本变更崩溃
                .addCallback(object : RoomDatabase.Callback() {//这里可使用onCreate、onOpen的，只是没必要
                override fun onCreate(db: SupportSQLiteDatabase) {
                    super.onCreate(db)
                    // 这里可以初始化数据，例如插入默认日记，懒得
                }
                })
                .build()
        }
    }
}
