package com.example.filmguide

// CityActivity.kt
import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityCityBinding
import com.example.filmguide.logic.network.city.City
import com.example.filmguide.logic.network.city.CityClient
import com.example.filmguide.ui.CityAdapter
import com.example.filmguide.utils.ToastUtil

import kotlinx.coroutines.launch
import kotlin.math.log

class CityActivity : AppCompatActivity() {
    lateinit var binding: ActivityCityBinding
    private val adapter = CityAdapter { city ->
        PrefsManager.saveCityInfo(this, city.id, city.name)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private var allCityList = mutableListOf<City>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 直接在 Activity 的协程作用域里发请求
        lifecycleScope.launch {
            try {
                val response = CityClient.cityApi.getCities()
                val cityList = response.cts
                allCityList.addAll(cityList) // 保存所有城市数据
                adapter.submitList(cityList)
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@CityActivity, "加载失败：${e.message}", R.drawable.icon)
            }
        }

        // 为搜索 EditText 添加文本变化监听
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { keyword ->
                    val filteredList = allCityList.filter { city ->
                        city.name.contains(keyword, ignoreCase = true)
                    }
                    adapter.submitList(filteredList)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }
}
