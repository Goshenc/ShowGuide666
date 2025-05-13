package com.example.filmguide.ui

import android.media.MediaMetadataRetriever
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.databinding.ActivityMovieDetailBinding
import com.example.filmguide.logic.network.moviedetail.MovieDetailClient
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch


class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val movieId = intent.getIntExtra("movieId", -1)
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
                Log.d("MovieDetailActivity", "Video URL: ${movie.videourl}")
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@MovieDetailActivity, "加载失败：${e.message}", R.drawable.icon)
            }

        }
    }
    private fun showMovieDetail(movie: com.example.filmguide.logic.network.moviedetail.DetailMovie) {
        Glide.with(this)
            .load(movie.imageUrl)
            .into(binding.ivPoster)
        Glide.with(this)
            .load(movie.videoImg)
            .into(binding.tvVideoImage)

        binding.tvName.text = movie.name
        binding.tvCategory.text = "类型：${movie.cat}"
        binding.tvDirector.text = "导演：${movie.dir}"
        binding.tvStarring.text = "主演：${movie.star}"
        binding.tvComingDate.text = "上映时间：${movie.pubDesc}"
        binding.tvSource.text = "放映地：${movie.src}"
        binding.tvScore.text = "评分：${movie.scoreLabel}"
        binding.tvIntroduce.text = movie.dra
        binding.tvWishCount.text = "期待人数：${movie.wish}"


        if (!movie.videourl.isNullOrEmpty()) {
            binding.tvVideo.setVideoURI(Uri.parse(movie.videourl))
            binding.tvVideo.setOnPreparedListener { mediaPlayer ->
                binding.tvVideo.isClickable = true
                binding.ivPlayButton.setOnClickListener {
                    binding.ivPlayButton.visibility = View.GONE
                    binding.tvVideoImage.visibility = View.GONE
                    binding.tvVideo.start()
                }
                binding.tvVideo.setOnClickListener {
                    binding.ivPlayButton.visibility = View.VISIBLE
                    binding.tvVideo.pause()
                }
            }

        }

    }



}