package com.example.filmguide

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityMovieDetailBinding
import com.example.filmguide.logic.network.moviedetail.MovieDetailClient
import com.example.filmguide.ui.DetailMovieAdapter
import kotlinx.coroutines.launch
import androidx.lifecycle.lifecycleScope

/**
 * 电影详情页面
 */
class MovieDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMovieDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMovieDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置状态栏透明
        window.statusBarColor = android.graphics.Color.TRANSPARENT
        window.navigationBarColor = android.graphics.Color.TRANSPARENT
        
        // 设置系统UI可见性
        window.decorView.systemUiVisibility = 
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_STABLE or
            android.view.View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN

        loadMovieData()
    }

    private fun loadMovieData() {
        // 从Intent获取电影数据
        val movieId = intent.getIntExtra("movieId", -1)
        
        if (movieId != -1) {
            // 通过movieId加载电影详情
            loadMovieDetailById(movieId)
        } else {
            Toast.makeText(this, "电影数据加载失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
    
    private fun loadMovieDetailById(movieId: Int) {
        lifecycleScope.launch {
            try {
                Log.d("MovieDetailActivity", "正在加载电影详情，movieId: $movieId")
                val response = MovieDetailClient.movieDetailApi.getMovieDetail(movieId)
                val detailMovie = response.detailMovie

                // 设置RecyclerView
                binding.recyclerView.layoutManager = LinearLayoutManager(this@MovieDetailActivity)
                val adapter = DetailMovieAdapter(this@MovieDetailActivity, detailMovie, lifecycleScope)
                binding.recyclerView.adapter = adapter

            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@MovieDetailActivity, "加载失败：${e.message}", Toast.LENGTH_SHORT).show()
                finish()
            }
        }
    }
}