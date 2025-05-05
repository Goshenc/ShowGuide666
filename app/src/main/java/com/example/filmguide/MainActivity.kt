package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.filmguide.databinding.ActivityMainBinding
import com.example.filmguide.ui.AuthViewModel
import com.example.filmguide.ui.AuthViewModelFactory
import com.example.filmguide.utils.ToastUtil

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(this) }
    private val prefs by lazy {
        val masterKey = MasterKey.Builder(this)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            this,
            "session_prefs",
            masterKey,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理系统栏 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 1. 预填账号和密码
        val savedEmail = prefs.getString("saved_email", "")
        val savedPwd = prefs.getString("saved_pwd", "")
        if (!savedEmail.isNullOrEmpty() && !savedPwd.isNullOrEmpty()) {
            binding.accountEditText.setText(savedEmail)
            binding.passwordEditText.setText(savedPwd)
        }

        // 2. 跳转到注册页面
        binding.registerImage.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        // 3. 登录按钮点击事件
        binding.loginBottom.setOnClickListener {
            val email = binding.accountEditText.text.toString().trim()
            val pwd = binding.passwordEditText.text.toString()
            // 非空校验
            if (email.isEmpty() || pwd.isEmpty()) {
                ToastUtil.show(this, "账号和密码不能为空", R.drawable.icon)
                return@setOnClickListener
            }
            // 调用 ViewModel 登录
            viewModel.login(email, pwd)
        }

        // 4. 观察登录结果
        viewModel.loginResult.observe(this) { user ->
            if (user != null) {
                // 登录成功后保存账号密码
                prefs.edit()
                    .putString("saved_email", binding.accountEditText.text.toString())
                    .putString("saved_pwd", binding.passwordEditText.text.toString())
                    .apply()
                ToastUtil.show(this, "登录成功，欢迎 ${user.account}", R.drawable.icon)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                ToastUtil.show(this, "账号或密码错误", R.drawable.icon)
            }
        }
    }
}
