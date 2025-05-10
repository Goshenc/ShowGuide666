package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.example.filmguide.databinding.ActivityRecordDetailBinding
import com.example.filmguide.ui.RecordDetailViewModel

class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private val viewModel: RecordDetailViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val sysBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(sysBars.left, sysBars.top, sysBars.right, sysBars.bottom)
            insets
        }

        val diaryId = intent.getIntExtra("diaryId", -1)
        if (diaryId == -1) {
            finish()
            return
        }
        viewModel.loadDiaryDetails(diaryId)

        viewModel.diaryEntity.observe(this) { diary ->
            if (diary == null) return@observe

            binding.diaryTitle.text = diary.title
            binding.diaryArticle.text = diary.article
            binding.diaryDate.text = diary.date
            binding.diaryPlace.text = diary.location
            binding.diaryWeather.text = diary.weather
            binding.ratingBar.rating = diary.rating

            // 图片展示：保留展示逻辑
            binding.diaryImage.visibility = View.GONE
            diary.localImagePath?.takeIf { it.isNotBlank() }?.let { path ->
                binding.diaryImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(path)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.diaryImage)
            } ?: diary.networkImageLink?.takeIf { it.isNotBlank() }?.let { url ->
                binding.diaryImage.visibility = View.VISIBLE
                Glide.with(this)
                    .load(url)
                    .transition(DrawableTransitionOptions.withCrossFade())
                    .into(binding.diaryImage)
            }

            // 分享按钮点击：仅分享文字，不包括图片Uri
            binding.imgShare.setOnClickListener {
                shareRecord(
                    title = diary.title,
                    date = diary.date,
                    location = diary.location,
                    rating = diary.rating,
                    article = diary.article
                )
            }
        }
    }

    /**
     * 分享逻辑：仅文字分享，不携带图片流
     */
    private fun shareRecord(
        title: String,
        date: String,
        location: String,
        rating: Float,
        article: String
    ) {
        val shareText = buildString {
            append("【观演记】").append(title).append("\n")
            append("📅 时间: ").append(date).append("\n")
            append("📍 地点: ").append(location).append("\n")
            append("⭐️ 评分: ").append(rating).append("\n\n")
            append(article).append("\n\n")
            append("—— 来自 FilmGuide App")
        }

        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            putExtra(Intent.EXTRA_TEXT, shareText)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(shareIntent, "分享到"))
    }
}