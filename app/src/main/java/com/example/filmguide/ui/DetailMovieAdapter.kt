package com.example.filmguide.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.IsBuyDialogFragment
import com.example.filmguide.R
import com.example.filmguide.logic.AppDatabase
import com.example.filmguide.logic.network.moviedetail.DetailMovie
import com.example.filmguide.logic.network.moviedetail.convertDetailMovieToMovieEntity
import com.example.filmguide.utils.ToastUtil
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.ui.PlayerView

class DetailMovieAdapter(
    private val context: Context,
    private val movie: DetailMovie,
    private val coroutineScope: CoroutineScope
) : RecyclerView.Adapter<DetailMovieAdapter.MovieViewHolder>() {

    private val ITEM_HEAD = 0
    private val ITEM_SCORE = 1
    private val ITEM_INTRODUCE = 2
    private val ITEM_VIDEO = 3
    private val ITEM_BUY = 4

    // Base ViewHolder
    open class MovieViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView)

    // 头部 Holder
    class HeadViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val ivPoster: ImageView = itemView.findViewById(R.id.ivPoster)
        val tvName: TextView = itemView.findViewById(R.id.tvName)
        val tvCategory: TextView = itemView.findViewById(R.id.tvCategory)
        val tvDirector: TextView = itemView.findViewById(R.id.tvDirector)
        val tvStarring: TextView = itemView.findViewById(R.id.tvStarring)
        val tvComingDate: TextView = itemView.findViewById(R.id.tvComingDate)
    }

    // 评分 Holder
    class ScoreViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val tvScore: TextView = itemView.findViewById(R.id.tvScore)
        val tvWishCount: TextView = itemView.findViewById(R.id.tvWishCount)
    }

    // 简介 Holder
    class IntroduceViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val tvIntroduce: TextView = itemView.findViewById(R.id.tvIntroduce)
    }

    // 视频 Holder：使用 PlayerView，并单独管理 ExoPlayer
    class VideoViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val tvVideoText: TextView = itemView.findViewById(R.id.tvVideoText)
        val tvVideoImage: ImageView = itemView.findViewById(R.id.tvVideoImage)
        val playerView: PlayerView = itemView.findViewById(R.id.tvVideo)

        // 每个 Holder 单独维护一个 ExoPlayer
        var exoPlayer: ExoPlayer? = null

        // 记录是否已对 playerView 注册过 detach Listener
        private var attachedListenerRegistered = false

        fun initPlayer(context: Context) {
            exoPlayer?.release()
            exoPlayer = ExoPlayer.Builder(context).build().also { player ->
                playerView.player = player
            }
            // 确保只注册一次 Listener
            if (!attachedListenerRegistered) {
                attachedListenerRegistered = true

                // 当 PlayerView 从窗口分离时（Activity/Fragment 退出或页面切换），立刻释放播放器
                playerView.addOnAttachStateChangeListener(object : View.OnAttachStateChangeListener {
                    override fun onViewAttachedToWindow(v: View) {
                        // 不需要处理
                    }

                    override fun onViewDetachedFromWindow(v: View) {
                        exoPlayer?.pause()
                        exoPlayer?.release()
                        exoPlayer = null
                        playerView.player = null
                    }
                })
            }
        }

        fun releasePlayer() {
            playerView.player = null
            exoPlayer?.release()
            exoPlayer = null
        }
    }

    // 购票 Holder
    class BuyViewHolder(itemView: View) : MovieViewHolder(itemView) {
        val buyButton: TextView = itemView.findViewById(R.id.buyButton)
    }

    override fun getItemViewType(position: Int): Int {
        return when (position) {
            0 -> ITEM_HEAD
            2 -> ITEM_SCORE
            3 -> ITEM_INTRODUCE
            1 -> ITEM_BUY
            else -> ITEM_VIDEO
        }
    }

    override fun getItemCount(): Int = 5

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MovieViewHolder {
        return when (viewType) {
            ITEM_HEAD -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detail_head, parent, false)
                HeadViewHolder(view)
            }
            ITEM_SCORE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detail_score, parent, false)
                ScoreViewHolder(view)
            }
            ITEM_INTRODUCE -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detail_introduce, parent, false)
                IntroduceViewHolder(view)
            }
            ITEM_BUY -> {
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detail_buy, parent, false)
                BuyViewHolder(view)
            }
            else -> {
                // ITEM_VIDEO
                val view = LayoutInflater.from(parent.context)
                    .inflate(R.layout.item_movie_detail_video, parent, false)
                VideoViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: MovieViewHolder, position: Int) {
        when (holder) {

            is HeadViewHolder -> {
                Glide.with(context).load(movie.imageUrl).into(holder.ivPoster)
                holder.tvName.text = movie.name
                holder.tvCategory.text = "类型：${movie.cat}"
                holder.tvDirector.text =
                    if (movie.dir.isEmpty()) "导演：暂无信息" else "导演：${movie.dir}"
                holder.tvStarring.text =
                    if (movie.star.isEmpty()) "主演：暂无信息" else "主演：${movie.star}"
                holder.tvComingDate.text = "上映时间：${movie.pubDesc}"
            }

            is ScoreViewHolder -> {
                holder.tvScore.text = "评分：${movie.scoreLabel}"
                holder.tvWishCount.text = "期待人数：${movie.wish}"
            }

            is IntroduceViewHolder -> {
                holder.tvIntroduce.text = "简介：${movie.dra}"
            }

            is BuyViewHolder -> {
                holder.buyButton.setOnClickListener {
                    val fragmentManager =
                        (context as androidx.fragment.app.FragmentActivity).supportFragmentManager
                    val dialogFragment = IsBuyDialogFragment()
                    dialogFragment.setOnConfirmButtonClickListener(
                        object : IsBuyDialogFragment.OnConfirmButtonClickListener {
                            override fun onConfirmClick() {
                                coroutineScope.launch(Dispatchers.IO) {
                                    val movieEntity = convertDetailMovieToMovieEntity(movie)
                                    val movieDao = AppDatabase.getInstance(context).movieDao()
                                    movieDao.insert(movieEntity)
                                    withContext(Dispatchers.Main) {
                                        ToastUtil.show(
                                            context,
                                            "已自动添加到管理票务页面",
                                            R.drawable.icon
                                        )
                                    }
                                }
                            }
                        }
                    )
                    dialogFragment.show(fragmentManager, "CustomDialogFragment")
                    openUrl("https://www.maoyan.com/films/${movie.id}")
                }
            }

            is VideoViewHolder -> {
                // === Step1：UI 初始状态：封面可见，播放器隐藏，“暂无资源”隐藏 ===
                Glide.with(context).load(movie.videoImg).into(holder.tvVideoImage)
                holder.tvVideoText.visibility = View.GONE
                holder.tvVideoImage.visibility = View.VISIBLE
                holder.playerView.visibility = View.INVISIBLE

                // 有效 videourl 时，自动播放；否则显示“暂无资源”
                if (!movie.videourl.isNullOrEmpty()) {
                    holder.initPlayer(context)

                    // 构造 MediaItem 并马上播放
                    val mediaItem = MediaItem.fromUri(Uri.parse(movie.videourl))
                    holder.exoPlayer?.apply {
                        setMediaItem(mediaItem)
                        prepare()
                        playWhenReady = true
                    }

                    // 监听“实际开始播放”事件，一旦播放，就隐藏封面并把 PlayerView 设为可见
                    holder.exoPlayer?.addListener(object : Player.Listener {
                        override fun onIsPlayingChanged(isPlaying: Boolean) {
                            if (isPlaying) {
                                holder.tvVideoImage.visibility = View.GONE
                                holder.playerView.visibility = View.VISIBLE
                            }
                        }
                    })

                    // （可选）点击视频可暂停/恢复
                    holder.playerView.setOnClickListener {
                        holder.exoPlayer?.let { player ->
                            if (player.isPlaying) player.pause()
                            else {
                                holder.tvVideoImage.visibility = View.GONE
                                holder.playerView.visibility = View.VISIBLE
                                player.play()
                            }
                        }
                    }
                } else {
                    holder.tvVideoText.visibility = View.VISIBLE
                }
            }
        }
    }

    /** 保险起见：回收时也释放播放器 **/
    override fun onViewRecycled(holder: MovieViewHolder) {
        super.onViewRecycled(holder)
        if (holder is VideoViewHolder) {
            holder.releasePlayer()
        }
    }

    private fun openUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(url))
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        } else {
            Toast.makeText(context, "无法打开链接", Toast.LENGTH_SHORT).show()
        }
    }
}
