package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.network.searchmovies.Movie
import com.example.filmguide.logic.network.searchperformance.EnhancedPerformance

class SearchPerformanceAdapter : RecyclerView.Adapter<SearchPerformanceAdapter.ViewHolder>() {
    private val PerformanceList = mutableListOf<EnhancedPerformance>()

    fun submitList(list: List<EnhancedPerformance>) {
        PerformanceList.clear()
        PerformanceList.addAll(list)
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView){
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvStar: TextView = itemView.findViewById(R.id.tvStar)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)
        val tvVenue: TextView = itemView.findViewById(R.id.tvVenue)

        fun bind(enhancedPerformance: EnhancedPerformance) {
            tvName.text = enhancedPerformance.performance.name
            tvStar.text = "时间：" + enhancedPerformance.performance.timeRange
            tvPrice.text = "价格：" + enhancedPerformance.performance.priceRange.toString()
            tvVenue.text = "地点：" + enhancedPerformance.performance.venue.toString()
            Glide.with(ivPoster.context)
                .load(enhancedPerformance.performance.posterUrl)
                .into(ivPoster)


        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_performance, parent, false)
        return ViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int ) {
        holder.bind(PerformanceList[position])
    }

    override fun getItemCount(): Int {
        return PerformanceList.size
    }



}