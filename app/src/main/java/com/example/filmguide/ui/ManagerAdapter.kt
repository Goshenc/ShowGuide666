package com.example.filmguide.ui


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.button.MaterialButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.R
import com.example.filmguide.logic.dao.PerformanceDao
import com.example.filmguide.logic.dao.MovieDao // 假设电影相关Dao
import com.example.filmguide.logic.network.performancedetail.PerformanceEntity
import com.example.filmguide.logic.network.moviedetail.MovieEntity // 假设电影实体类
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

class ManagerAdapter(
    private val performanceDao: PerformanceDao,
    private val movieDao: MovieDao,
    private val context: Context,
    private val textView: TextView,
    private val onItemClick: (Any) -> Unit
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    private val performanceItems = mutableListOf<PerformanceEntity>()
    private val movieItems = mutableListOf<MovieEntity>()
    private val allItems = mutableListOf<Any>()

    init {
        loadData()
    }

    private fun loadData() {
        textView.visibility = View.GONE
        performanceItems.clear()
        movieItems.clear()
        allItems.clear()
        CoroutineScope(Dispatchers.IO).launch {
            performanceItems.addAll(performanceDao.getAllPerformances())
            movieItems.addAll(movieDao.getAll())
            allItems.addAll(performanceItems)
            allItems.addAll(movieItems)
            CoroutineScope(Dispatchers.Main).launch {
                notifyDataSetChanged()
            }
            if (allItems.isEmpty()){
                textView.visibility = View.VISIBLE
            } else {
                textView.visibility = View.GONE
            }
        }
    }

    override fun getItemViewType(position: Int): Int {
        return if (allItems[position] is PerformanceEntity) {
            0
        } else {
            1
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            0 -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_performance, parent, false)
                PerformanceViewHolder(view)
            }
            else -> {
                val view = LayoutInflater.from(parent.context).inflate(R.layout.item_manage_movie, parent, false)
                MovieViewHolder(view)
            }
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is PerformanceViewHolder -> {
                val item = allItems[position] as PerformanceEntity
                holder.bind(item)
                holder.deleteButton.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        performanceDao.deletePerformanceById(item.performanceId)
                        performanceItems.remove(item)
                        allItems.remove(item)
                        CoroutineScope(Dispatchers.Main).launch {
                            notifyDataSetChanged()
                            loadData()
                        }
                    }
                }
                holder.viewedButton.setOnClickListener {
                    onItemClick(item)

                }
            }
            is MovieViewHolder -> {
                val item = allItems[position] as MovieEntity
                holder.bind(item)
                holder.deleteButton.setOnClickListener {
                    CoroutineScope(Dispatchers.IO).launch {
                        movieDao.deleteMovieByDbId(item.id)
                        movieItems.remove(item)
                        allItems.remove(item)
                        CoroutineScope(Dispatchers.Main).launch {
                            notifyDataSetChanged()
                            loadData()
                        }
                    }
                }
                holder.viewedButton.setOnClickListener {
                    onItemClick(item)

                }
            }
        }
    }

    override fun getItemCount(): Int {
        return allItems.size
    }

    inner class PerformanceViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvName)
        val imageView: ImageView = view.findViewById(R.id.ivPoster)
        val deleteButton: MaterialButton = view.findViewById(R.id.delete_button)
        val viewedButton: MaterialButton = view.findViewById(R.id.viewed_button)

        fun bind(performance: PerformanceEntity) {
            nameTextView.text = performance.name
            Glide.with(context)
                .load(performance.posterUrl)
                .into(imageView)
        }
    }

    inner class MovieViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val nameTextView: TextView = view.findViewById(R.id.tvName)
        val imageView: ImageView = view.findViewById(R.id.ivPoster)
        val deleteButton: MaterialButton = view.findViewById(R.id.delete_button)
        val viewedButton: MaterialButton = view.findViewById(R.id.viewed_button)

        fun bind(movie: MovieEntity) {
            nameTextView.text = movie.name
            Glide.with(context)
                .load(movie.imageUrl)
                .into(imageView)
        }
    }
}