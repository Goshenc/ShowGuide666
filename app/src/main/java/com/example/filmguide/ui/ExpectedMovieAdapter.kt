package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.network.expectedmovies.ExpectedMovie
import android.widget.ImageView
import android.widget.TextView

class ExpectedMovieAdapter : RecyclerView.Adapter<ExpectedMovieAdapter.ViewHolder>() {
    private val movieList = mutableListOf<ExpectedMovie>()
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(list: List<ExpectedMovie>) {
        movieList.clear()
        movieList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvWishCount: TextView = itemView.findViewById(R.id.tvWishCount)
        val tvComingDate: TextView = itemView.findViewById(R.id.tvComingDate)

        fun bind(movie: ExpectedMovie) {
            tvName.text = movie.name
            tvWishCount.text = "期待人数：${movie.wishCount}"
            tvComingDate.text = "上映时间：${movie.comingDate}"
            Glide.with(ivPoster.context)
                .load(movie.imageUrl)
                .into(ivPoster)
        }

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(movieList[position].id)
                }
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expected_movie, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = movieList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position])
    }
}