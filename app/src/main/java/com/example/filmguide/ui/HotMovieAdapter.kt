package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.network.hotmovie.HotMovie
import android.widget.ImageView
import android.widget.TextView


class HotMovieAdapter : RecyclerView.Adapter<HotMovieAdapter.ViewHolder>() {
    private val movieList = mutableListOf<HotMovie>()
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(list: List<HotMovie>) {
        movieList.clear()
        movieList.addAll(list)
        notifyDataSetChanged()
    }

    inner class ViewHolder(itemView: android.view.View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvStar: TextView = itemView.findViewById(R.id.tvStar)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvComment: TextView = itemView.findViewById(R.id.tvComment)

        fun bind(movie: HotMovie) {
            tvName.text = movie.name
            tvStar.text = "主演：${movie.stars}"
            tvScore.text = "评分：${movie.score}"
            tvCategory.text = "类型：${movie.category}"
            tvComment.text = movie.scoreComment
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
            .inflate(R.layout.item_hot_movie, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = movieList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position])
    }
}
