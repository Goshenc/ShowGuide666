package com.example.filmguide.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.recordroom.RecordEntity
import java.text.SimpleDateFormat
import java.util.*

/**
 * 心愿单适配器
 */
class WishlistAdapter(
    private val onItemClick: (RecordEntity) -> Unit,
    private val onRemoveClick: (RecordEntity) -> Unit
) : RecyclerView.Adapter<WishlistAdapter.WishlistViewHolder>() {
    
    private var items = mutableListOf<RecordEntity>()
    
    fun updateItems(newItems: List<RecordEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): WishlistViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_wishlist, parent, false)
        return WishlistViewHolder(view)
    }
    
    override fun onBindViewHolder(holder: WishlistViewHolder, position: Int) {
        holder.bind(items[position])
    }
    
    override fun getItemCount(): Int = items.size
    
    inner class WishlistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val posterImage: ImageView = itemView.findViewById(R.id.posterImage)
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val typeText: TextView = itemView.findViewById(R.id.typeText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val removeButton: TextView = itemView.findViewById(R.id.removeButton)
        
        fun bind(item: RecordEntity) {
            // 设置海报
            if (item.posterUrl.isNotEmpty()) {
                Glide.with(itemView.context)
                    .load(item.posterUrl)
                    .placeholder(R.drawable.placeholder_poster)
                    .error(R.drawable.placeholder_poster)
                    .into(posterImage)
            } else {
                posterImage.setImageResource(R.drawable.placeholder_poster)
            }
            
            // 设置标题
            titleText.text = item.title
            
            // 设置类型
            typeText.text = when (item.type) {
                "movie" -> "电影"
                "concert" -> "演出"
                "exhibition" -> "展览"
                "activity" -> "活动"
                else -> "其他"
            }
            
            // 设置日期
            dateText.text = item.date
            
            // 设置地点
            locationText.text = item.location ?: "地点待定"
            
            // 设置点击事件
            itemView.setOnClickListener {
                onItemClick(item)
            }
            
            // 设置移除按钮
            removeButton.setOnClickListener {
                onRemoveClick(item)
            }
        }
    }
}
