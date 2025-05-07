package com.example.filmguide

import android.os.Bundle
import android.util.Log
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.bumptech.glide.Glide
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions
import com.bumptech.glide.request.RequestOptions
import com.example.filmguide.databinding.ActivityRecordDetailBinding
import com.example.filmguide.ui.RecordDetailViewModel

class RecordDetailActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordDetailBinding
    private val viewModel: RecordDetailViewModel by viewModels() // 使用 ViewModel

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordDetailBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val diaryId = intent.getIntExtra("diaryId", -1)
        if (diaryId != -1) {
            viewModel.loadDiaryDetails(diaryId)
        } else {
            finish()
        }

        // 观察 LiveData 并更新 UI
        viewModel.diaryEntity.observe(this) { diary ->
            if (diary != null) {
                binding.diaryTitle.text = diary.title
                binding.diaryArticle.text = diary.article
                binding.diaryDate.text = diary.date
                binding.diaryPlace.text = diary.location
                binding.diaryWeather.text = diary.weather

                Log.d("DiaryDetailActivity", "加载日记 ID: $diaryId")
                Log.d("DiaryDetailActivity", "本地图片路径: ${diary.localImagePath}")
                Log.d("DiaryDetailActivity", "网络图片路径: ${diary.networkImageLink}")

                val imagePath = diary.localImagePath ?: diary.networkImageLink


                binding.diaryImage.visibility = android.view.View.VISIBLE

                if (!imagePath.isNullOrEmpty()) {
                    Glide.with(this)
                        .load(imagePath)

                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.diaryImage)
                }
            }
        }
    }
}
