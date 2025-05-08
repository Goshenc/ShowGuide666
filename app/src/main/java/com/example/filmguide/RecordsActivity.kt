package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.widget.addTextChangedListener
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityRecordsBinding
import com.example.filmguide.logic.recordroom.RecordEntity
import com.example.filmguide.ui.RecordAdapter
import com.example.filmguide.ui.RecordViewModel
import com.example.filmguide.utils.ToastUtil

class RecordsActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRecordsBinding
    private lateinit var diaryAdapter: RecordAdapter
    private val viewModel: RecordViewModel by viewModels()
    private val addDiaryLauncher = registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {}
//很好的启动器，lambda里花括号中的部分是当启动的 Activity 返回结果时会执行的回调代码块.

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityRecordsBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setupRecyclerView()
        setupObservers()
        setupListeners()
    }

    private fun setupRecyclerView() {
        diaryAdapter = RecordAdapter(this, emptyList()) // 适配器初始为空
        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = diaryAdapter
    }

    private fun setupObservers() {
        // 监听数据库变化，更新 UI
        viewModel.allDiaries.observe(this) { diarieshahaha ->   //在这段代码中，diarieshahaha 是你在 observe 方法的 lambda 表达式中自定义的参数名。
            // 也就是说，它并不是在其他地方“预先定义”的，而是在这段代码中你选择用 diarieshahaha 来接收 LiveData 更新时传递过来的数据。
            diaryAdapter.updateData(diarieshahaha)
        }
    }

    private fun setupListeners() {
        // 搜索日记
        binding.searchBox.addTextChangedListener { text ->
            viewModel.searchDiaries(text.toString()).observe(this) { diaries ->
                diaryAdapter.updateData(diaries)
            }
        }

        // 添加日记
        binding.addDiaryButton.setOnClickListener {
            val intent = Intent(this, CreateRecordActivity::class.java)
            addDiaryLauncher.launch(intent)
        }

        // 删除日记
        binding.deleteDiaryButton.setOnClickListener { deleteDiary() }
    }

    private fun deleteDiary() {
        val diaries = diaryAdapter.getCurrentDiaries()
        if (diaries.isEmpty()) {
            ToastUtil.show(this, "当前没有日记可删除", R.drawable.icon)
            return
        }

        val diaryTitles = diaries.map { it.title }.toTypedArray()
        var selectedIndex = 0

        AlertDialog.Builder(this)
            .setTitle("选择要删除的日记")
            .setSingleChoiceItems(diaryTitles, 0) { _, which ->
                selectedIndex = which
            }
            .setPositiveButton("删除") { _, _ ->
                val diaryToDelete: RecordEntity = diaries[selectedIndex]
                viewModel.deleteDiary(diaryToDelete) // 通过 ViewModel 删除
            }
            .setNegativeButton("取消", null)
            .show()
    }
}
