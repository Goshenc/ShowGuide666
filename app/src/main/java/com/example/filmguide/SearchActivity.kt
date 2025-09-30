package com.example.filmguide

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.viewpager2.widget.ViewPager2
import com.example.filmguide.databinding.ActivitySearchBinding
import com.example.filmguide.ui.SearchViewPagerAdapter
import com.google.android.material.tabs.TabLayoutMediator

class SearchActivity : AppCompatActivity() {

    lateinit var binding: ActivitySearchBinding
    private lateinit var keyword: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivitySearchBinding.inflate(layoutInflater)
        setContentView(binding.root)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 1. 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT

        // 2. 把状态栏内文字和图标切成浅色，以便在渐变背景上能看清
        val insetsController = WindowInsetsControllerCompat(window, window.decorView)
        insetsController.isAppearanceLightStatusBars = false
        getIntentData()

    }

    private fun getIntentData() {
        keyword = intent.getStringExtra("keyword") ?: ""
        if (keyword.isEmpty()) {
            finish()
            Log.d("zxy","空关键")
            return
        }

        val cityId = intent.getIntExtra("cityId",-1)
        if (cityId == -1) {
            finish()
            Log.d("zxy","无效ID")
            return
        }

        binding.searchBox.setText(keyword)
        binding.viewPager.adapter = SearchViewPagerAdapter(this, cityId, keyword)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)

            }
        })

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "影视"
                1 -> tab.text = "演出"
            }
        }.attach()

        binding.searchButton.setOnClickListener{
            Log.d("zxy","点击成功")
            val currentKeyword = binding.searchBox.text.toString().trim()
            val adapter = binding.viewPager.adapter as? SearchViewPagerAdapter
            (binding.viewPager.adapter as SearchViewPagerAdapter).updateKeyword(currentKeyword)
        }

    }
}