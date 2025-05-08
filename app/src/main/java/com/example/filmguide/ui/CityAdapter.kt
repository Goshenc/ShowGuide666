package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.logic.network.city.City

class CityAdapter(
    private val onItemClick: (City) -> Unit
) : ListAdapter<City, CityAdapter.CityVH>(CityVH.DiffCallback()) {

    class CityVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(city: City, onItemClick: (City) -> Unit) {
            tvName.text = city.name
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
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return CityVH(view)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(getItem(position), onItemClick)
    }
}
