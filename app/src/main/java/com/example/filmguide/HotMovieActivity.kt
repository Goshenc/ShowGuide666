package com.example.filmguide

import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.databinding.ActivityHotMovieBinding
import com.example.filmguide.logic.network.hotmovie.HotMovie
import com.example.filmguide.logic.network.hotmovie.HotMovieClient
import com.example.filmguide.ui.HotMovieAdapter
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.launch

class HotMovieActivity : AppCompatActivity() {

    private lateinit var binding: ActivityHotMovieBinding
    private val adapter = HotMovieAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHotMovieBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }



        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val cityId = intent.getIntExtra("cityId", -1)
        val cityName : String = intent.getStringExtra("cityName").toString()
        Log.d("zxy",cityName);
        if (cityId == -1) {
            ToastUtil.show(this, "无效城市 ID",R.drawable.icon)
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // 使用 HotMovieApi 获取热映电影
                val response = HotMovieClient.hotMoviesApi.getHotMovies(cityId, cityName)
                val movieList = response.data?.data?.hotMovies ?: emptyList()
                adapter.submitList(movieList)
                Log.d("zxy",response.toString())
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@HotMovieActivity, "加载失败：${e.message}",R.drawable.icon)
            }
        }
    }
}
