package com.example.filmguide

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.adapter.WishlistAdapter
import com.example.filmguide.databinding.ActivityWishlistBinding
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import kotlinx.coroutines.launch

/**
 * 心愿单Activity
 */
class WishlistActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityWishlistBinding
    private lateinit var adapter: WishlistAdapter
    private lateinit var database: RecordDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityWishlistBinding.inflate(layoutInflater)
        setContentView(binding.root)
        
        // 初始化数据库
        database = RecordDatabase.getInstance(this)
        
        initViews()
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
}
