package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.RadioButton
import android.widget.TextView
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
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }








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

        showBeautifulDeleteDialog(diaries)
    }
    
    private fun showBeautifulDeleteDialog(diaries: List<RecordEntity>) {
        val dialog = AlertDialog.Builder(this)
            .setView(R.layout.dialog_delete_record)
            .create()
        
        dialog.show()
        
        // 获取容器并添加记录选项
        val container = dialog.findViewById<LinearLayout>(R.id.recordsList)
        val btnDelete = dialog.findViewById<TextView>(R.id.btnDelete)
        var selectedIndex = 0
        
        // 创建记录选择项
        diaries.forEachIndexed { index, diary ->
            val optionView = createRecordOptionView(diary, index, dialog, container) { selectedIdx ->
                selectedIndex = selectedIdx
            }
            container?.addView(optionView)
        }
        
        // 设置删除按钮点击事件
        btnDelete?.setOnClickListener {
            if (selectedIndex >= 0 && selectedIndex < diaries.size) {
                val diaryToDelete = diaries[selectedIndex]
                dialog.dismiss()
                viewModel.deleteDiary(diaryToDelete)
                ToastUtil.show(this, "已删除：${diaryToDelete.title}", R.drawable.icon)
            } else {
                ToastUtil.show(this, "请先选择要删除的记录", R.drawable.icon)
            }
        }
        
        // 美化对话框样式
        dialog.window?.let { window ->
            window.setBackgroundDrawable(getDrawable(R.drawable.card_background))
            window.attributes?.let { attributes ->
                attributes.dimAmount = 0.3f
                window.attributes = attributes
            }
        }
    }
    
    /**
     * 创建记录选项视图（完全按照地图选项的样式）
     */
    private fun createRecordOptionView(diary: RecordEntity, index: Int, dialog: AlertDialog, container: LinearLayout?, onSelected: (Int) -> Unit): View {
        val view = LayoutInflater.from(this).inflate(R.layout.item_record_selection, null)
        
        val iconView = view.findViewById<ImageView>(R.id.recordIcon)
        val titleView = view.findViewById<TextView>(R.id.titleText)
        val dateView = view.findViewById<TextView>(R.id.dateText)
        val radioButton = view.findViewById<RadioButton>(R.id.radioButton)
        
        // 设置图标和文本
        iconView.setImageResource(R.drawable.ic_delete_red)
        titleView.text = diary.title
        dateView.text = diary.date
        radioButton.isChecked = index == 0
        
        // 设置点击事件 - 选择记录
        view.setOnClickListener {
            // 如果点击的是当前选中的项，则取消选中
            if (radioButton.isChecked) {
                // 取消所有选中状态
                for (i in 0 until (container?.childCount ?: 0)) {
                    val child = container?.getChildAt(i)
                    if (child is View) {
                        val rb = child.findViewById<RadioButton>(R.id.radioButton)
                        rb?.isChecked = false
                    }
                }
                onSelected(-1) // 传递-1表示取消选中
            } else {
                onSelected(index)
                // 更新所有单选按钮状态
                for (i in 0 until (container?.childCount ?: 0)) {
                    val child = container?.getChildAt(i)
                    if (child is View) {
                        val rb = child.findViewById<RadioButton>(R.id.radioButton)
                        rb?.isChecked = (i == index)
                    }
                }
            }
        }
        
        return view
    }
}
