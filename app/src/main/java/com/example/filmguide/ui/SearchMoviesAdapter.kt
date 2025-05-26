package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.network.searchmovies.Movie
import android.widget.ImageView
import android.widget.TextView


class SearchMoviesAdapter : RecyclerView.Adapter<SearchMoviesAdapter.ViewHolder>() {
    private val movieList = mutableListOf<Movie>()
    private var onItemClickListener: ((Int) -> Unit)? = null

    fun setOnItemClickListener(listener: (Int) -> Unit) {
        onItemClickListener = listener
    }

    fun submitList(list: List<Movie>) {
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

        fun bind(movie: Movie) {
            tvName.text = movie.name
            if (movie.starring == null){
                tvStar.text = "主演： 暂无信息"
            } else {
                tvStar.text = "主演：${movie.starring}"
            }
            tvScore.text = "评分：${movie.score}"
            tvCategory.text = "类型：${movie.category}"
            Glide.with(ivPoster.context)
                .load(movie.imageUrl)
                .into(ivPoster)
        }

        init {
            itemView.setOnClickListener {
                val position = adapterPosition
                if (position != RecyclerView.NO_POSITION) {
                    onItemClickListener?.invoke(movieList[position].movieId)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val itemView = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_search_movie, parent, false)
        return ViewHolder(itemView)
    }

    override fun getItemCount(): Int = movieList.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(movieList[position])
    }


}