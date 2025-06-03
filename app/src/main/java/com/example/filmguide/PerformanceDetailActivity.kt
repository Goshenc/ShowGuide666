package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityPerformanceDetailBinding
import com.example.filmguide.logic.network.allperformance.AllPerformanceResponse
import com.example.filmguide.logic.network.performancedetail.PerformanceDetailClient
import com.example.filmguide.logic.network.performancedetail.PerformanceDetailData
import com.example.filmguide.logic.network.searchperformance.Celebrity
import com.example.filmguide.logic.network.searchperformance.EnhancedPerformance
import com.example.filmguide.logic.network.searchperformance.Performance
import com.example.filmguide.logic.network.searchperformance.PerformanceData
import com.example.filmguide.ui.DetailPerformanceAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.launch

class PerformanceDetailActivity : AppCompatActivity() {

    private lateinit var binding: ActivityPerformanceDetailBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPerformanceDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 设置窗口Insets处理
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 获取传递的演出数据
        val enhancedPerformance = intent.getSerializableExtra("key_data") as? EnhancedPerformance
        val celebrityList = intent.getSerializableExtra("key_celebrity") as? List<Celebrity>
        val performance = intent.getSerializableExtra("performance") as? AllPerformanceResponse.PerformanceData

        if (enhancedPerformance != null && celebrityList != null) {
            lifecycleScope.launch {
                try {
                    val response = PerformanceDetailClient.performanceDetailApi.getPerformanceDetail(enhancedPerformance.performance.id)
                    val performanceData = response.data
                    updateUI(enhancedPerformance, celebrityList, performanceData, lifecycleScope)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else if (performance != null) {
            lifecycleScope.launch {
                try {
                    val response = PerformanceDetailClient.performanceDetailApi.getPerformanceDetail(performance.performanceId)
                    val performanceData = response.data
                    updateUI(performanceData, performance, lifecycleScope)
                } catch (e: Exception) {
                    e.printStackTrace()
                }
            }
        } else {
            Toast.makeText(this, "数据获取失败", Toast.LENGTH_SHORT).show()
            finish()
        }
    }

    private fun updateUI(
        enhancedPerformance: EnhancedPerformance,
        celebrityList: List<Celebrity>,
        performanceData: PerformanceDetailData?,
        scope: CoroutineScope // 接收协程作用域
    ) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PerformanceDetailActivity)
            // 传递 scope 到 Adapter
            adapter = DetailPerformanceAdapter(
                this@PerformanceDetailActivity,
                enhancedPerformance,
                celebrityList,
                performanceData,
                null,
                scope
            )
        }
    }

    private fun updateUI(
        performanceData: PerformanceDetailData?,
        performance: AllPerformanceResponse.PerformanceData,
        scope: CoroutineScope // 接收协程作用域
    ) {
        binding.recyclerView.apply {
            layoutManager = LinearLayoutManager(this@PerformanceDetailActivity)
            // 传递 scope 到 Adapter
            adapter = DetailPerformanceAdapter(
                this@PerformanceDetailActivity,
                null,
                null,
                performanceData,
                performance,
                scope
            )
        }
    }
}