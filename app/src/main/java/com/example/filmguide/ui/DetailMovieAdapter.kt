package com.example.filmguide.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.logic.network.moviedetail.DetailMovie
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import android.widget.VideoView
import androidx.core.content.ContentProviderCompat.requireContext
import com.example.filmguide.R
import com.example.filmguide.logic.AppDatabase

import com.example.filmguide.logic.network.moviedetail.MovieEntity
import com.example.filmguide.logic.network.moviedetail.convertDetailMovieToMovieEntity
import com.example.filmguide.ui.DetailPerformanceAdapter.PerformanceViewHolder
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DetailMovieAdapter (private val context: Context,private val movie : DetailMovie,private val coroutineScope: CoroutineScope )  :
    RecyclerView.Adapter<DetailMovieAdapter.MovieViewHolder>() {


        private val ITEM_HEAD = 0
        private val ITEM_SCORE = 1
        private val ITEM_INTRODUCE = 2
        private val ITEM_VIDEO = 3
        private val ITEM_BUY = 4

    open class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {

    }

    class HeadViewHolder(itemView: View) : MovieViewHolder(itemView){
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDirector: TextView = itemView.findViewById(R.id.tvDirector)
        val tvStarring: TextView = itemView.findViewById(R.id.tvStarring)
        val tvComingDate: TextView = itemView.findViewById(R.id.tvComingDate)
    }

    class ScoreViewHolder(itemView: View) : MovieViewHolder(itemView){
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvWishCount: TextView = itemView.findViewById(R.id.tvWishCount)
    }

    class IntroduceViewHolder(itemView: View) : MovieViewHolder(itemView){
        val tvIntroduce: TextView = itemView.findViewById(R.id.tvIntroduce)
    }

    class VideoViewHolder(itemView: View) : MovieViewHolder(itemView){
        val tvVideoText: TextView = itemView.findViewById(R.id.tvVideoText)
        val tvVideoImage: ImageView = itemView.findViewById(R.id.tvVideoImage)
        val ivPlayButton: ImageView = itemView.findViewById(R.id.ivPlayButton)
        val tvVideo: VideoView = itemView.findViewById(R.id.tvVideo)
    }
    class BuyViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val buyButton : TextView = itemView.findViewById(R.id.buyButton)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        if (viewType == ITEM_HEAD) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movie_detail_head, parent, false)
            return HeadViewHolder(view)
        } else if (viewType == ITEM_SCORE){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movie_detail_score, parent, false)
            return ScoreViewHolder(view)
        } else if (viewType == ITEM_INTRODUCE){
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movie_detail_introduce, parent, false)
            return IntroduceViewHolder(view)
        } else if (viewType == ITEM_BUY) {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movie_detail_buy, parent, false)
            return BuyViewHolder(view)
        } else {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_movie_detail_video, parent, false)
            return VideoViewHolder(view)
        }


    }

    override fun getItemViewType(position: Int): Int {
        if (position == 0){
            return ITEM_HEAD
        } else if (position == 2){
            return ITEM_SCORE
        } else if (position == 3){
            return ITEM_INTRODUCE
        } else if (position == 1){
            return ITEM_BUY
        } else {
            return ITEM_VIDEO
        }
    }

    override fun getItemCount(): Int = 5

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        val movie = movie
        val context = holder.itemView.context


        if (holder is HeadViewHolder){
            var newHolder : HeadViewHolder = holder as HeadViewHolder

            Glide.with(context)
                .load(movie.imageUrl)
                .into(newHolder.ivPoster)
            newHolder.tvName.text = movie.name
            newHolder.tvCategory.text = "类型：${movie.cat}"
            if (movie.dir == ""){
                newHolder.tvDirector.text = "导演：暂无信息"
            } else {
                newHolder.tvDirector.text = "导演：${movie.dir}"
            }
            if (movie.star == ""){
                newHolder.tvStarring.text = "主演：暂无信息"
            } else {
                newHolder.tvStarring.text = "主演：${movie.star}"
            }
            newHolder.tvComingDate.text = "上映时间：${movie.pubDesc}"
        } else if (holder is ScoreViewHolder) {
            var newHolder : ScoreViewHolder = holder as ScoreViewHolder
            newHolder.tvScore.text = "评分：${movie.scoreLabel}"
            newHolder.tvWishCount.text = "期待人数：${movie.wish}"
        } else if (holder is IntroduceViewHolder){
            var newHolder : IntroduceViewHolder = holder as IntroduceViewHolder
            newHolder.tvIntroduce.text = "简介：${movie.dra}"
        } else if (holder is BuyViewHolder){
            var newHolder : BuyViewHolder = holder as BuyViewHolder
            newHolder.buyButton.setOnClickListener{
                val fragmentManager = (context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                val dialogFragment = IsBuyDialogFragment()
                dialogFragment.setOnConfirmButtonClickListener(object : IsBuyDialogFragment.OnConfirmButtonClickListener {
                    override fun onConfirmClick() {
                        coroutineScope.launch(Dispatchers.IO) {
                            val movieEntity = convertDetailMovieToMovieEntity(movie)
                            val movieDao = AppDatabase.getInstance(context).movieDao()
                            movieDao.insert(movieEntity)

                            withContext(Dispatchers.Main) {
                                Toast.makeText(context, "已自动添加到管理", Toast.LENGTH_SHORT).show()
                            }
                        }
                    }
                })

                dialogFragment.show(fragmentManager, "CustomDialogFragment")
                openUrl("https://www.maoyan.com/films/" + movie.id)
            }
        } else {

            var newHolder : VideoViewHolder = holder as VideoViewHolder

            Glide.with(context)
                .load(movie.videoImg)
                .into(newHolder.tvVideoImage)

            newHolder.tvVideoImage.visibility = View.VISIBLE
            newHolder.tvVideoText.visibility = View.GONE


            if (!movie.videourl.isNullOrEmpty()) {

                newHolder.tvVideo.setVideoURI(Uri.parse(movie.videourl))
                newHolder.tvVideo.setOnPreparedListener { mediaPlayer ->
                    newHolder.tvVideo.visibility = View.VISIBLE
                    newHolder.tvVideo.isClickable = true

                    newHolder.ivPlayButton.setOnClickListener {
                        newHolder.ivPlayButton.visibility = View.GONE
                        newHolder.tvVideoImage.visibility = View.GONE
                        newHolder.tvVideo.start()
                    }

                    newHolder.tvVideo.setOnClickListener {
                        newHolder.ivPlayButton.visibility = View.VISIBLE
                        newHolder.tvVideo.pause()
                    }
                }
            } else {
                newHolder.tvVideoText.visibility = View.VISIBLE
                newHolder.ivPlayButton.visibility = View.GONE
            }
        }
    }
    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent) // 使用传递的 Context
        } else {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
}