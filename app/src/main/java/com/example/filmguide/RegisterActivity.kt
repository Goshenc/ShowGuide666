package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import com.example.filmguide.databinding.ActivityRegisterBinding
import com.example.filmguide.ui.AuthViewModel
import com.example.filmguide.ui.AuthViewModelFactory
import com.example.filmguide.utils.ToastUtil

class RegisterActivity : AppCompatActivity() {
    private lateinit var binding: ActivityRegisterBinding
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory(this) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding = ActivityRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // 处理系统栏 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 注册按钮点击
        binding.registerBottom.setOnClickListener {
            val email = binding.accountEditText.text.toString().trim()
            val pwd = binding.passwordEditText.text.toString()
            val confirm = binding.confirmEditText.text.toString()

            // 非空校验
            if (email.isEmpty() || pwd.isEmpty() || confirm.isEmpty()) {
                showToast("账号、密码和确认密码均不能为空")
                return@setOnClickListener
            }

            // 密码一致性校验
            if (pwd != confirm) {
                showToast("两次密码不一致")
                return@setOnClickListener
            }

            // 调用注册逻辑
            viewModel.register(email, pwd)
        }

        // 观察注册结果
        viewModel.regResult.observe(this, Observer { success ->
            if (success) {
                showToast("注册成功，跳转登录")
                intent = Intent(this, MainActivity::class.java)
                startActivity(intent)
            } else {
                showToast("该邮箱已注册或注册失败")
            }
        })
    }

    private fun showToast(msg: String) {
        ToastUtil.show(this, msg,R.drawable.icon)    }
}
