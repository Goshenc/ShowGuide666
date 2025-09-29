package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.R
import com.example.filmguide.logic.network.city.City

class CityAdapter(
    private val onItemClick: (City) -> Unit
) : ListAdapter<City, CityAdapter.CityVH>(CityVH.DiffCallback()) {

    class CityVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvCityName: TextView = itemView.findViewById(R.id.tvCityName)
        private val tvPinyin: TextView = itemView.findViewById(R.id.tvPinyin)
        
        fun bind(city: City, onItemClick: (City) -> Unit) {
            tvCityName.text = city.name
            tvPinyin.text = city.pinyin ?: ""
            itemView.setOnClickListener {
                onItemClick(city)
            }
        }

        class DiffCallback : DiffUtil.ItemCallback<City>() {
            override fun areItemsTheSame(oldItem: City, newItem: City) = oldItem.id == newItem.id
            override fun areContentsTheSame(oldItem: City, newItem: City) = oldItem == newItem
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_city, parent, false)
        return CityVH(view)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}
