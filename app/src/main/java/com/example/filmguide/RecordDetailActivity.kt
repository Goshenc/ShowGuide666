package com.example.filmguide

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.View
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
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }






        val diaryId = intent.getIntExtra("diaryId", -1)
        if (diaryId != -1) {
            viewModel.loadDiaryDetails(diaryId)
        } else {
            finish()
        }

        // 观察 LiveData 并更新 UI
        viewModel.diaryEntity.observe(this) { diary ->
            diary?.let {
                binding.diaryTitle.text   = it.title
                binding.diaryArticle.text = it.article
                binding.diaryDate.text    = it.date
                binding.diaryPlace.text   = it.location
                binding.diaryWeather.text = it.weather
                binding.ratingBar.apply {
                    rating = it.rating

                }
                Log.d("DiaryDetailActivity", "加载日记 ID: $diaryId")
                Log.d("DiaryDetailActivity", "本地图片路径: ${it.localImagePath}")
                Log.d("DiaryDetailActivity", "网络图片路径: ${it.networkImageLink}")

                // 先隐藏，下面有图片时再显示
                binding.diaryImage.visibility = View.GONE

                // 优先尝试本地 Uri
                it.localImagePath?.takeIf { path -> path.isNotBlank() }?.let { localPath ->
                    val localUri = Uri.parse(localPath)
                    binding.diaryImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(localUri)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.diaryImage)
                    return@observe
                }

                // 再尝试网络 URL
                it.networkImageLink?.takeIf { url -> url.isNotBlank() }?.let { url ->
                    binding.diaryImage.visibility = View.VISIBLE
                    Glide.with(this)
                        .load(url)
                        .transition(DrawableTransitionOptions.withCrossFade())
                        .into(binding.diaryImage)
                }
            }
        }
    }
}
