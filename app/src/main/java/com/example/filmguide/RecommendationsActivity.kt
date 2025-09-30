package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.adapter.RecommendationAdapter
import com.example.filmguide.databinding.ActivityRecommendationsBinding
import com.example.filmguide.recommendation.RecommendationEngine
import com.example.filmguide.social.ShareManager
import com.example.filmguide.logic.recordroom.RecordEntity
import com.example.filmguide.logic.recordroom.RecordDatabase
import kotlinx.coroutines.launch

/**
 * 智能推荐页面
 * 展示基于用户偏好的个性化推荐
 */
class RecommendationsActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityRecommendationsBinding
    private lateinit var recommendationEngine: RecommendationEngine
    private lateinit var shareManager: ShareManager
    private lateinit var adapter: RecommendationAdapter
    private lateinit var database: RecordDatabase
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRecommendationsBinding.inflate(layoutInflater)
        // 需要在 setContentView 之前或紧挨着调用
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        
        initViews()
        initData()
        
        // 检查是否有用户选择的类型
        val selectedGenres = intent.getStringArrayListExtra("selectedGenres")
        if (selectedGenres != null && selectedGenres.isNotEmpty()) {
            loadRecommendationsByUserSelection(selectedGenres)
        } else {
            loadRecommendations()
        }
    }
    
    private fun initViews() {
        // 返回按钮
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // 刷新按钮
        binding.refreshButton.setOnClickListener {
            loadRecommendations()
        }
        
        // 分享按钮
        binding.shareButton.setOnClickListener {
            showShareOptions()
        }
        
        // 心愿单按钮
        binding.wishlistButton.setOnClickListener {
            // 跳转到心愿单页面
            val intent = Intent(this, WishlistActivity::class.java)
            startActivity(intent)
        }
        
        // 偏好设置按钮
        binding.preferencesButton.setOnClickListener {
            // 跳转到偏好选择页面
            val intent = Intent(this, PreferenceSelectionActivity::class.java)
            startActivity(intent)
        }
        
        // 设置RecyclerView
        binding.recommendationsRecyclerView.layoutManager = LinearLayoutManager(this)
        adapter = RecommendationAdapter(
            onItemClick = { recommendation ->
                onRecommendationClick(recommendation)
            },
            onAddToWishlist = { recommendation ->
                addToWishlist(recommendation)
            }
        )
        binding.recommendationsRecyclerView.adapter = adapter
        
        // 移除欢迎消息，避免Toast过多
    }
    
    private fun initData() {
        recommendationEngine = RecommendationEngine(this)
        shareManager = ShareManager(this)
        database = RecordDatabase.getInstance(this)
    }
    
    private fun loadRecommendations() {
        binding.refreshButton.visibility = View.GONE
        
        // 显示加载状态
        showLoadingState()
        
        lifecycleScope.launch {
            try {
                val recommendations = recommendationEngine.generateRecommendations()
                adapter.updateRecommendations(recommendations)
                
                if (recommendations.isEmpty()) {
                    showEmptyState()
                } else {
                    hideLoadingState()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationsActivity, "加载推荐失败：${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState()
            } finally {
                binding.refreshButton.visibility = View.VISIBLE
            }
        }
    }
    
    private fun onRecommendationClick(recommendation: RecommendationEngine.Recommendation) {
        when (recommendation.type) {
            RecommendationEngine.RecommendationType.MOVIE -> {
                // 跳转到电影详情页
                val intent = Intent(this, MovieDetailActivity::class.java)
                // 这里需要传递完整的MovieData，暂时用推荐数据创建
                val movieData = com.example.filmguide.network.MovieData(
                    id = "rec_${recommendation.title.hashCode()}",
                    title = recommendation.title,
                    genre = "推荐",
                    rating = recommendation.confidence * 10.0,
                    releaseDate = "2023",
                    director = "未知导演",
                    actors = emptyList(),
                    description = recommendation.reason,
                    posterUrl = "",
                    boxOffice = null
                )
                intent.putExtra("movieData", movieData)
                startActivity(intent)
            }
            RecommendationEngine.RecommendationType.CONCERT -> {
                // 跳转到演出详情页
                Toast.makeText(this, "查看演出详情：${recommendation.title}", Toast.LENGTH_SHORT).show()
            }
            RecommendationEngine.RecommendationType.EXHIBITION -> {
                // 跳转到展览详情页
                Toast.makeText(this, "查看展览详情：${recommendation.title}", Toast.LENGTH_SHORT).show()
            }
            RecommendationEngine.RecommendationType.ACTIVITY -> {
                // 跳转到活动详情页
                Toast.makeText(this, "查看活动详情：${recommendation.title}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun addToWishlist(recommendation: RecommendationEngine.Recommendation) {
        lifecycleScope.launch {
            try {
                Log.d("RecommendationsActivity", "开始添加到心愿单: ${recommendation.title}")
                val record = RecordEntity(
                    title = recommendation.title,
                    article = recommendation.reason,
                    localImagePath = null,
                    networkImageLink = null,
                    date = java.text.SimpleDateFormat("yyyy-MM-dd", java.util.Locale.getDefault()).format(java.util.Date()),
                    weather = "未知",
                    location = "未知",
                    rating = recommendation.confidence * 10f,
                    type = when (recommendation.type) {
                        RecommendationEngine.RecommendationType.MOVIE -> "movie"
                        RecommendationEngine.RecommendationType.CONCERT -> "concert"
                        RecommendationEngine.RecommendationType.EXHIBITION -> "exhibition"
                        RecommendationEngine.RecommendationType.ACTIVITY -> "activity"
                    },
                    posterUrl = "",
                    isInWishlist = true
                )
                
                Log.d("RecommendationsActivity", "准备插入记录到数据库")
                database.recordDao().insertRecord(record)
                Log.d("RecommendationsActivity", "记录插入成功")
                Toast.makeText(this@RecommendationsActivity, "已添加到心愿单", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("RecommendationsActivity", "添加到心愿单失败: ${e.message}", e)
                Toast.makeText(this@RecommendationsActivity, "添加失败：${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }
    
    private fun showShareOptions() {
        val shareOptions = shareManager.getShareOptions()
        val options = shareOptions.map { it.name }.toTypedArray()
        
        androidx.appcompat.app.AlertDialog.Builder(this)
            .setTitle("📤 分享到")
            .setItems(options) { _, which ->
                when (which) {
                    0 -> shareManager.shareToWeChat(getLatestRecord())
                    1 -> shareManager.shareToMoments(getLatestRecord())
                    2 -> shareManager.shareToWeibo(getLatestRecord())
                    3 -> shareManager.shareImageToSocial(getLatestRecord())
                    4 -> shareManager.shareImageToSocial(getLatestRecord())
                }
            }
            .show()
    }
    
    /**
     * 基于用户选择的类型加载推荐
     */
    private fun loadRecommendationsByUserSelection(selectedGenres: List<String>) {
        binding.refreshButton.visibility = View.GONE
        
        // 显示加载状态
        showLoadingState()
        
        lifecycleScope.launch {
            try {
                val recommendations = recommendationEngine.generateRecommendationsByGenres(selectedGenres)
                adapter.updateRecommendations(recommendations)
                
                if (recommendations.isEmpty()) {
                    showEmptyState()
                } else {
                    hideLoadingState()
                }
            } catch (e: Exception) {
                Toast.makeText(this@RecommendationsActivity, "加载推荐失败：${e.message}", Toast.LENGTH_SHORT).show()
                showEmptyState()
            } finally {
                binding.refreshButton.visibility = View.VISIBLE
            }
        }
    }
    
    private fun showLoadingState() {
        // 显示加载状态
        binding.recommendationsRecyclerView.visibility = View.GONE
        // 这里可以添加一个加载指示器
    }
    
    private fun hideLoadingState() {
        // 隐藏加载状态
        binding.recommendationsRecyclerView.visibility = View.VISIBLE
    }
    
    private fun showEmptyState() {
        // 显示空状态提示
        Toast.makeText(this, "暂无推荐内容，请稍后再试", Toast.LENGTH_SHORT).show()
        // 显示空状态，不再重新加载
        hideLoadingState()
    }
    
    private fun getLatestRecord(): RecordEntity {
        // 获取最新的观影记录用于分享
        // 这里简化实现，实际应该从数据库获取
        return RecordEntity(
            id = 1,
            title = "示例电影",
            article = "这是一部很棒的电影！",
            localImagePath = null,
            networkImageLink = null,
            date = "2024-01-01",
            weather = "晴天",
            location = "示例影院",
            rating = 4.5f
        )
    }
}
