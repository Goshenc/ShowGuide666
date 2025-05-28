package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import android.widget.ImageView
import android.widget.TextView
import com.example.filmguide.logic.network.allperformance.AllPerformanceResponse
import com.example.filmguide.logic.network.hotmovie.HotMovie

class AllPerformanceAdapter : RecyclerView.Adapter<AllPerformanceAdapter.ViewHolder>() {

    private val performanceList = mutableListOf<AllPerformanceResponse.PerformanceData>()
    private var onItemClickListener: ((AllPerformanceResponse.PerformanceData) -> Unit)? = null

    fun setOnItemClickListener(listener: (AllPerformanceResponse.PerformanceData) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(list: List<AllPerformanceResponse.PerformanceData>) {
        performanceList.clear()
        performanceList.addAll(list)
        notifyDataSetChanged()
    }


    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvVenue: TextView = itemView.findViewById(R.id.tvVenue)
        val tvPrice: TextView = itemView.findViewById(R.id.tvPrice)

        fun bind(performance : AllPerformanceResponse.PerformanceData) {
            tvName.text = performance.name
            tvVenue.text = performance.shopName
            tvPrice.text = "￥" + performance.priceRange
            Glide.with(ivPoster.context)
                .load(performance.posterUrl)
                .into(ivPoster)
        }

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(performanceList[position])
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_all_performance, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = performanceList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(performanceList[position])
    }
}