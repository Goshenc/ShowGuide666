package com.example.filmguide.ui

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.logic.network.moviedetail.DetailMovie
import android.widget.ImageView
import android.widget.TextView
import android.widget.VideoView
import com.example.filmguide.R


class DetailMovieAdapter (private val movie : DetailMovie)  :
    RecyclerView.Adapter<DetailMovieAdapter.MovieViewHolder>() {

    class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvVideoImage: ImageView = itemView.findViewById(R.id.tvVideoImage)
        val ivPlayButton: ImageView = itemView.findViewById(R.id.ivPlayButton)
        val tvVideo: VideoView = itemView.findViewById(R.id.tvVideo)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDirector: TextView = itemView.findViewById(R.id.tvDirector)
        val tvStarring: TextView = itemView.findViewById(R.id.tvStarring)
        val tvComingDate: TextView = itemView.findViewById(R.id.tvComingDate)
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvIntroduce: TextView = itemView.findViewById(R.id.tvIntroduce)
        val tvWishCount: TextView = itemView.findViewById(R.id.tvWishCount)
        val tvVideoText: TextView = itemView.findViewById(R.id.tvVideoText)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_movie_detail, parent, false)

        return MovieViewHolder(view)
    }

    override fun getItemCount(): Int = 1

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movie
        val context = holder.itemView.context

        Glide.with(context)
            .load(movie.imageUrl)
            .into(holder.ivPoster)

        Glide.with(context)
            .load(movie.videoImg)
            .into(holder.tvVideoImage)

        holder.tvVideoText.visibility = View.GONE

        if (movie.videourl == null || movie.videourl == ""){
            holder.tvVideoText.visibility = View.VISIBLE
            holder.ivPlayButton.visibility = View.GONE
        }

        holder.tvName.text = movie.name
        holder.tvCategory.text = "类型：${movie.cat}"
        if (movie.dir == ""){
            holder.tvDirector.text = "导演：暂无信息"
        } else {
            holder.tvDirector.text = "导演：${movie.dir}"
        }
        if (movie.star == ""){
            holder.tvStarring.text = "主演：暂无信息"
        } else {
            holder.tvStarring.text = "主演：${movie.star}"
        }
        holder.tvComingDate.text = "上映时间：${movie.pubDesc}"
        holder.tvScore.text = "评分：${movie.scoreLabel}"
        holder.tvIntroduce.text = movie.dra
        holder.tvWishCount.text = "期待人数：${movie.wish}"

        if (!movie.videourl.isNullOrEmpty()) {
            holder.tvVideo.setVideoURI(Uri.parse(movie.videourl))
            holder.tvVideo.setOnPreparedListener { mediaPlayer ->
                holder.tvVideo.visibility = View.VISIBLE
                holder.tvVideo.isClickable = true

                holder.ivPlayButton.setOnClickListener {
                    holder.ivPlayButton.visibility = View.GONE
                    holder.tvVideoImage.visibility = View.GONE
                    holder.tvVideo.start()
                }

                holder.tvVideo.setOnClickListener {
                    holder.ivPlayButton.visibility = View.VISIBLE
                    holder.tvVideo.pause()
                }
            }
        }
    }
}