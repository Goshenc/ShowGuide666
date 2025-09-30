package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.WindowCompat
import com.example.filmguide.databinding.ActivityPreferenceSelectionBinding

/**
 * 用户偏好选择页面
 * 让用户选择喜欢的电影类型
 */
class PreferenceSelectionActivity : AppCompatActivity() {
    
    private lateinit var binding: ActivityPreferenceSelectionBinding
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityPreferenceSelectionBinding.inflate(layoutInflater)
        // 需要在 setContentView 之前或紧挨着调用
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContentView(binding.root)
        
        initViews()
    }
    
    private fun initViews() {
        // 返回按钮
        binding.backButton.setOnClickListener {
            finish()
        }
        
        // 确定按钮
        binding.confirmButton.setOnClickListener {
            val selectedGenres = getSelectedGenres()
            if (selectedGenres.isEmpty()) {
                Toast.makeText(this, "请至少选择一种电影类型", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            
            // 跳转到推荐页面，传递选择的类型
            val intent = Intent(this, RecommendationsActivity::class.java).apply {
                putStringArrayListExtra("selectedGenres", ArrayList(selectedGenres))
            }
            startActivity(intent)
            finish()
        }
    }
    
    /**
     * 获取用户选择的电影类型
     */
    private fun getSelectedGenres(): List<String> {
        val selectedGenres = mutableListOf<String>()
        
        if (binding.actionCheckbox.isChecked) {
            selectedGenres.add("动作")
        }
        if (binding.comedyCheckbox.isChecked) {
            selectedGenres.add("喜剧")
        }
        if (binding.romanceCheckbox.isChecked) {
            selectedGenres.add("爱情")
        }
        if (binding.scifiCheckbox.isChecked) {
            selectedGenres.add("科幻")
        }
        if (binding.horrorCheckbox.isChecked) {
            selectedGenres.add("恐怖")
        }
        if (binding.animationCheckbox.isChecked) {
            selectedGenres.add("动画")
        }
        if (binding.mysteryCheckbox.isChecked) {
            selectedGenres.add("悬疑")
        }
        if (binding.biographyCheckbox.isChecked) {
            selectedGenres.add("传记")
        }
        
        return selectedGenres
    }
}
