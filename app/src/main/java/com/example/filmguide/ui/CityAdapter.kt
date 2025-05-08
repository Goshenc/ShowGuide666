package com.example.filmguide.ui
// CityAdapter.kt
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.logic.network.city.City

class CityAdapter : ListAdapter<City, CityAdapter.CityVH>(DiffCallback()) {

    class CityVH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvName: TextView = itemView.findViewById(android.R.id.text1)
        fun bind(city: City) {
            tvName.text = "${city.name} (${city.pinyin})"
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<City>() {
        override fun areItemsTheSame(old: City, new: City) = old.id == new.id
        override fun areContentsTheSame(old: City, new: City) = old == new
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CityVH {
        val view = LayoutInflater.from(parent.context)
            .inflate(android.R.layout.simple_list_item_1, parent, false)
        return CityVH(view)
    }

    override fun onBindViewHolder(holder: CityVH, position: Int) {
        holder.bind(getItem(position))
    }
}
