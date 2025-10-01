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
    private var currentLocation: String? = null
    
    fun updateItems(newItems: List<RecordEntity>) {
        items.clear()
        items.addAll(newItems)
        notifyDataSetChanged()
    }
    
    fun setCurrentLocation(location: String?) {
        currentLocation = location
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
        private val titleText: TextView = itemView.findViewById(R.id.titleText)
        private val dateText: TextView = itemView.findViewById(R.id.dateText)
        private val locationText: TextView = itemView.findViewById(R.id.locationText)
        private val removeButton: TextView = itemView.findViewById(R.id.removeButton)
        
        fun bind(item: RecordEntity) {
            // 设置标题
            titleText.text = item.title
            
            // 设置日期
            dateText.text = item.date
            
            // 设置地点 - 使用传入的位置信息
            locationText.text = currentLocation ?: (item.location ?: "地点待定")
            
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
