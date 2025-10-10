package com.example.filmguide

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.KeyEvent
import android.view.inputmethod.EditorInfo
import android.widget.TextView
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
            // 空关键词时不关闭Activity，而是显示空的搜索界面
            Log.d("zxy","空关键")
            setupEmptySearch()
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

        try {
            binding.searchButton.setOnClickListener{
                Log.d("zxy","搜索按钮被点击")
                performSearch()
            }
            Log.d("zxy","搜索按钮点击事件设置成功")
        } catch (e: Exception) {
            Log.e("zxy","设置搜索按钮点击事件失败: ${e.message}")
        }
        
        // 设置键盘确定键事件
        try {
            binding.searchBox.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    Log.d("zxy","键盘确定键被按下")
                    performSearch()
                    true
                } else {
                    false
                }
            }
            Log.d("zxy","键盘确定键事件设置成功")
        } catch (e: Exception) {
            Log.e("zxy","设置键盘确定键事件失败: ${e.message}")
        }

    }
    
    /**
     * 执行搜索功能
     */
    private fun performSearch() {
        val currentKeyword = binding.searchBox.text.toString().trim()
        Log.d("zxy","执行搜索，关键词: $currentKeyword")
        
        if (currentKeyword.isNotEmpty()) {
            val adapter = binding.viewPager.adapter as? SearchViewPagerAdapter
            if (adapter != null) {
                Log.d("zxy","调用updateKeyword")
                adapter.updateKeyword(currentKeyword)
            } else {
                Log.e("zxy","adapter为null")
            }
        } else {
            Log.d("zxy","关键词为空，不执行搜索")
        }
    }
    
    /**
     * 设置空搜索界面
     */
    private fun setupEmptySearch() {
        val cityId = intent.getIntExtra("cityId", -1)
        if (cityId == -1) {
            finish()
            Log.d("zxy","无效ID")
            return
        }
        
        // 设置空的搜索界面，用户可以输入搜索内容
        binding.searchBox.setText("")
        binding.viewPager.adapter = SearchViewPagerAdapter(this, cityId, "")
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

        try {
            binding.searchButton.setOnClickListener{
                Log.d("zxy","空搜索界面-搜索按钮被点击")
                performSearch()
            }
            Log.d("zxy","空搜索界面-搜索按钮点击事件设置成功")
        } catch (e: Exception) {
            Log.e("zxy","空搜索界面-设置搜索按钮点击事件失败: ${e.message}")
        }
        
        // 设置键盘确定键事件
        try {
            binding.searchBox.setOnEditorActionListener { _, actionId, event ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH || 
                    actionId == EditorInfo.IME_ACTION_DONE ||
                    (event != null && event.keyCode == KeyEvent.KEYCODE_ENTER && event.action == KeyEvent.ACTION_DOWN)) {
                    Log.d("zxy","空搜索界面-键盘确定键被按下")
                    performSearch()
                    true
                } else {
                    false
                }
            }
            Log.d("zxy","空搜索界面-键盘确定键事件设置成功")
        } catch (e: Exception) {
            Log.e("zxy","空搜索界面-设置键盘确定键事件失败: ${e.message}")
        }
    }
}