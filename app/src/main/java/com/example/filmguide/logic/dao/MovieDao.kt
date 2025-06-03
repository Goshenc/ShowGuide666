package com.example.filmguide.logic.dao

import androidx.room.*
import com.example.filmguide.logic.network.moviedetail.MovieEntity


@Dao
interface MovieDao {
    @Query("SELECT * FROM movies")
    fun getAll(): List<MovieEntity>

    @Query("SELECT * FROM movies WHERE id = :movieId LIMIT 1")
    fun findById(movieId: Int): MovieEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insertAll(movies: List<MovieEntity>)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    fun insert(movie: MovieEntity)

    @Delete
    fun delete(movie: MovieEntity)

    @Query("DELETE FROM movies WHERE id = :id")
    suspend fun deleteMovieByDbId(id: Int): Int
}



