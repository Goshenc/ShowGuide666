package com.example.filmguide

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.adapter.WishlistAdapter
import com.example.filmguide.databinding.ActivityWishlistBinding
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import com.example.filmguide.logic.network.weather.RetrofitBuilder
import com.example.filmguide.logic.network.weather.WeatherService
import com.example.filmguide.utils.Utils_Date_Location
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlinx.coroutines.Dispatchers

/**
 * 心愿单Activity
 */
class WishlistActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWishlistBinding
    private lateinit var adapter: WishlistAdapter
    private lateinit var database: RecordDatabase
    private lateinit var locationUtils: Utils_Date_Location.LocationHelper
    private var currentLocation: String? = null
    private val apiKey = "670ca929136a456992608cd2e794df24"
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 设置状态栏透明
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
        WindowCompat.setDecorFitsSystemWindows(window, false)
        
        // 处理系统栏insets，避免内容被遮挡
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        
        // 初始化数据库
        database = RecordDatabase.getInstance(this)
        locationUtils = Utils_Date_Location.LocationHelper(this)
        
        initViews()
        getLocation()
        loadWishlist()
    }
    
    private fun initViews() {
        // 设置返回按钮
        binding.toolbar.setNavigationOnClickListener {
            finish()
        }
        
        // 设置清空心愿单按钮
        binding.clearButton.setOnClickListener {
            clearWishlist()
        }
        
        // 设置RecyclerView
        adapter = WishlistAdapter(
            onItemClick = { item ->
                // 查看详情
                Toast.makeText(this, "查看详情：${item.title}", Toast.LENGTH_SHORT).show()
            },
            onRemoveClick = { item ->
                // 从心愿单移除
                removeFromWishlist(item)
            }
        )
        
        binding.wishlistRecyclerView.layoutManager = LinearLayoutManager(this)
        binding.wishlistRecyclerView.adapter = adapter
    }
    
    private fun loadWishlist() {
        lifecycleScope.launch {
            try {
                Log.d("WishlistActivity", "开始加载心愿单数据")
                val wishlistItems = database.recordDao().getWishlistItems()
                Log.d("WishlistActivity", "心愿单数据数量: ${wishlistItems.size}")
                adapter.updateItems(wishlistItems)
                
                if (wishlistItems.isEmpty()) {
                    Log.d("WishlistActivity", "心愿单为空，显示空状态")
                    showEmptyState()
                } else {
                    Log.d("WishlistActivity", "心愿单有数据，隐藏空状态")
                    hideEmptyState()
                }
            } catch (e: Exception) {
                Log.e("WishlistActivity", "加载心愿单失败: ${e.message}", e)
                Toast.makeText(this@WishlistActivity, "加载心愿单失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun removeFromWishlist(item: RecordEntity) {
        lifecycleScope.launch {
            try {
                database.recordDao().updateWishlistStatus(item.id, false)
                loadWishlist()
                Toast.makeText(this@WishlistActivity, "已从心愿单移除", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@WishlistActivity, "移除失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun clearWishlist() {
        lifecycleScope.launch {
            try {
                database.recordDao().clearWishlist()
                loadWishlist()
                Toast.makeText(this@WishlistActivity, "心愿单已清空", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Toast.makeText(this@WishlistActivity, "清空失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showEmptyState() {
        binding.emptyState.visibility = View.VISIBLE
        binding.wishlistRecyclerView.visibility = View.GONE
        binding.clearButton.visibility = View.GONE
    }
    
    private fun hideEmptyState() {
        binding.emptyState.visibility = View.GONE
        binding.wishlistRecyclerView.visibility = View.VISIBLE
        binding.clearButton.visibility = View.VISIBLE
    }
    
    private fun getLocation() {
        locationUtils.getLocation { location ->
            if (location != null) {
                val (lat, lng) = location.latitude to location.longitude
                Log.d("WishlistActivity", "获取到经纬度: $lng, $lat")
                // 调用API获取城市名称
                lifecycleScope.launch { 
                    getCityNameSuspend("$lng,$lat") 
                }
            } else {
                currentLocation = "位置获取失败"
                Log.d("WishlistActivity", "无法获取位置")
                adapter.setCurrentLocation(currentLocation)
            }
        }
    }
    
    private suspend fun getCityNameSuspend(location: String) {
        try {
            val service = RetrofitBuilder.getCityInstance.create(WeatherService::class.java)
            val resp = withContext(Dispatchers.IO) { service.getCity(apiKey, location) }
            if (resp.isSuccessful && resp.body()?.code == "200") {
                resp.body()?.location?.firstOrNull()?.let { loc ->
                    withContext(Dispatchers.Main) {
                        currentLocation = loc.name
                        Log.d("WishlistActivity", "获取到城市名称: $currentLocation")
                        adapter.setCurrentLocation(currentLocation)
                    }
                } ?: run {
                    withContext(Dispatchers.Main) {
                        currentLocation = "城市获取失败"
                        adapter.setCurrentLocation(currentLocation)
                    }
                }
            } else {
                withContext(Dispatchers.Main) {
                    currentLocation = "城市获取失败"
                    adapter.setCurrentLocation(currentLocation)
                }
            }
        } catch (e: Exception) {
            withContext(Dispatchers.Main) {
                currentLocation = "网络请求失败"
                adapter.setCurrentLocation(currentLocation)
            }
        }
    }
}
