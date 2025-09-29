package com.example.filmguide.ui

import android.content.Context
import android.content.Intent
import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.example.filmguide.RecordDetailActivity
import com.example.filmguide.databinding.RecordItemBinding
import com.example.filmguide.logic.recordroom.RecordEntity

class RecordAdapter(
    private val context: Context,
    private var diaries: List<RecordEntity>
) : RecyclerView.Adapter<RecordAdapter.DiaryViewHolder>() {

    inner class DiaryViewHolder(val binding: RecordItemBinding) : RecyclerView.ViewHolder(binding.root)

    override fun getItemCount(): Int = diaries.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): DiaryViewHolder {
        val binding = RecordItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return DiaryViewHolder(binding)
    }

    override fun onBindViewHolder(holder: DiaryViewHolder, position: Int) {
        val diary = diaries[position]
        holder.binding.apply {
            diaryTitle.text = diary.title
            diaryArticle.text = diary.article
            diaryDate.text = diary.date
            diaryLocation.text = diary.location
            diaryWeather.text = diary.weather

            // 处理图片加载
            val imageUrl = diary.localImagePath?.takeIf { it.isNotBlank() } ?: diary.networkImageLink
            
            // 显示或隐藏图片容器
            diaryImage.visibility = if (imageUrl.isNullOrEmpty()) View.GONE else View.VISIBLE
            
            if (!imageUrl.isNullOrEmpty()) {
                // 获取CardView内的ImageView
                val imageView = diaryImage.getChildAt(0) as? android.widget.ImageView
                imageView?.let { imgView ->
                    Glide.with(context)
                        .load(Uri.parse(imageUrl))
                        .into(imgView)
                }
            }

            // 点击事件
            root.setOnClickListener {
                val intent = Intent(context, RecordDetailActivity::class.java).apply {
                    putExtra("diaryId", diary.id)
                }
                context.startActivity(intent)
            }
        }
    }


    // 使用 DiffUtil 优化数据更新
    fun updateData(newDiaries: List<RecordEntity>) {
        val diffCallback = DiaryDiffCallback(diaries, newDiaries)//DiaryDiffCallback 是一个自定义的 DiffUtil.Callback 类。
        //它的作用是比较 diaries 和 newDiaries，找出不同之处（新增、删除、修改的项目）
        val diffResult = DiffUtil.calculateDiff(diffCallback)//DiffUtil.Callback 是干嘛的？
        //DiffUtil 是 Android 提供的智能计算列表差异的工具。
        //它能找出新旧列表之间的变化，而不是直接刷新整个 RecyclerView，提高性能。
        diaries = newDiaries
        diffResult.dispatchUpdatesTo(this) // 这一步通知 RecyclerView 只更新有变化的部分。，提高性能
    }

    // DiffUtil 计算数据变化，提高 RecyclerView 刷新效率
    class DiaryDiffCallback(
        private val oldList: List<RecordEntity>,
        private val newList: List<RecordEntity>
    ) : DiffUtil.Callback() {

        override fun getOldListSize(): Int = oldList.size
        override fun getNewListSize(): Int = newList.size

        override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition].id == newList[newItemPosition].id
        }

        override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean {
            return oldList[oldItemPosition] == newList[newItemPosition]
        }
    }

    fun getCurrentDiaries(): List<RecordEntity> = diaries
}
