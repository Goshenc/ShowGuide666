package com.example.filmguide.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.databinding.ActivityMovieDetailBinding
import com.example.filmguide.logic.network.moviedetail.MovieDetailClient
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch
import kotlin.math.log


class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieId = intent.getIntExtra("movieId", -1)
        Log.d("zxy", "Video URL: " + movieId)
        if (movieId == -1) {
            ToastUtil.show(this, "无效的电影 ID", R.drawable.icon)
            finish()
            return
        }
        lifecycleScope.launch {
            try {
                val response = MovieDetailClient.movieDetailApi.getMovieDetail(movieId)
                val movie = response.data.detailMovie
                showMovieDetail(movie)
                Log.d("zxy", "Video URL: " + movie.toString())
                Log.d("MovieDetailActivity", "Video URL: ${movie.videourl}")
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@MovieDetailActivity, "加载失败：${e.message}", R.drawable.icon)
            }

        }
    }
    private fun showMovieDetail(movie: com.example.filmguide.logic.network.moviedetail.DetailMovie) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@MovieDetailActivity)
            adapter = DetailMovieAdapter(movie)
        }
    }

}