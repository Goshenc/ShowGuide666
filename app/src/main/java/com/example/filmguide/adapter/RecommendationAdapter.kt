package com.example.filmguide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.R
import com.example.filmguide.recommendation.RecommendationEngine

/**
 * 推荐列表适配器
 */
class RecommendationAdapter(
    private val onItemClick: (RecommendationEngine.Recommendation) -> Unit,
    private val onAddToWishlist: (RecommendationEngine.Recommendation) -> Unit
) : RecyclerView.Adapter<RecommendationAdapter.RecommendationViewHolder>() {
    
    private var recommendations = mutableListOf<RecommendationEngine.Recommendation>()
    
    fun updateRecommendations(newRecommendations: List<RecommendationEngine.Recommendation>) {
        recommendations.clear()
        recommendations.addAll(newRecommendations)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_recommendation, parent, false)
        return RecommendationViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(recommendations[position])
    }
    
    override fun getItemCount(): Int = recommendations.size
    
    inner class RecommendationViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val typeIcon: TextView = itemView.findViewById(R.id.typeIcon)
        private val recommendationTitle: TextView = itemView.findViewById(R.id.recommendationTitle)
        private val recommendationType: TextView = itemView.findViewById(R.id.recommendationType)
        private val confidenceScore: TextView = itemView.findViewById(R.id.confidenceScore)
        private val recommendationReason: TextView = itemView.findViewById(R.id.recommendationReason)
        private val viewDetailsButton: TextView = itemView.findViewById(R.id.viewDetailsButton)
        private val addToWishlistButton: TextView = itemView.findViewById(R.id.addToWishlistButton)
        
        fun bind(recommendation: RecommendationEngine.Recommendation) {
            // 设置类型图标
            typeIcon.text = when (recommendation.type) {
                RecommendationEngine.RecommendationType.MOVIE -> "🎬"
                RecommendationEngine.RecommendationType.CONCERT -> "🎵"
                RecommendationEngine.RecommendationType.EXHIBITION -> "🎨"
                RecommendationEngine.RecommendationType.ACTIVITY -> "🎪"
            }
            
            // 设置标题和类型
            recommendationTitle.text = recommendation.title
            recommendationType.text = when (recommendation.type) {
                RecommendationEngine.RecommendationType.MOVIE -> "电影推荐"
                RecommendationEngine.RecommendationType.CONCERT -> "演出推荐"
                RecommendationEngine.RecommendationType.EXHIBITION -> "展览推荐"
                RecommendationEngine.RecommendationType.ACTIVITY -> "活动推荐"
            }
            
            // 设置置信度
            confidenceScore.text = "${(recommendation.confidence * 100).toInt()}%"
            
            // 设置推荐理由
            recommendationReason.text = recommendation.reason
            
            // 设置按钮点击事件
            viewDetailsButton.setOnClickListener {
                onItemClick(recommendation)
            }
            
            addToWishlistButton.setOnClickListener {
                // 添加到心愿单
                onAddToWishlist(recommendation)
                addToWishlistButton.text = "已添加"
                addToWishlistButton.isEnabled = false
            }
            
            // 设置置信度颜色
            val confidence = recommendation.confidence
            when {
                confidence >= 0.8f -> confidenceScore.setTextColor(itemView.context.getColor(R.color.DodgerBlue))
                confidence >= 0.6f -> confidenceScore.setTextColor(itemView.context.getColor(android.R.color.holo_orange_light))
                else -> confidenceScore.setTextColor(itemView.context.getColor(R.color.text_secondary))
            }
        }
    }
}
