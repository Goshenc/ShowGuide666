package com.example.filmguide

// CityActivity.kt
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityCityBinding
import com.example.filmguide.logic.network.city.ApiClient
import com.example.filmguide.logic.network.city.City
import com.example.filmguide.ui.CityAdapter

import kotlinx.coroutines.launch

class CityActivity : AppCompatActivity() {
    lateinit var binding:ActivityCityBinding

    private val adapter = CityAdapter()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding=ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 直接在 Activity 的协程作用域里发请求
        lifecycleScope.launch {
            try {
                // Retrofit + Gson 已经把 JSON 转成 CityResponse
                val response = ApiClient.cityApi.getCities()
                // 拿到真正的 List<City>
                val cityList = response.data.cts
                // 切回主线程，提交给 RecyclerView
                adapter.submitList(cityList)
            } catch (e: Exception) {
                e.printStackTrace()
                Toast.makeText(this@CityActivity, "加载失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
}
