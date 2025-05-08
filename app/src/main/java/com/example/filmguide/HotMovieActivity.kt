package com.example.filmguide

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
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

        val recyclerView = binding.recyclerView
        recyclerView.layoutManager = LinearLayoutManager(this)
        recyclerView.adapter = adapter

        val cityId = intent.getIntExtra("cityId", -1)
        if (cityId == -1) {
            ToastUtil.show(this, "无效城市 ID",R.drawable.icon)
            finish()
            return
        }

        lifecycleScope.launch {
            try {
                // 使用 HotMovieApi 获取热映电影
                val response = HotMovieClient.hotMovieApi.getHotMovies(cityId)
                val movieList = response.outerData.innerData.hot
                adapter.submitList(movieList)
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@HotMovieActivity, "加载失败：${e.message}",R.drawable.icon)
            }
        }
    }
}
