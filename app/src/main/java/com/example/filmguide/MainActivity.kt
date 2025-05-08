package com.example.filmguide

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.filmguide.databinding.ActivityMainBinding
import com.example.filmguide.ui.AuthViewModel
import com.example.filmguide.ui.AuthViewModelFactory
import com.example.filmguide.utils.ToastUtil
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class MainActivity : AppCompatActivity() {
    private lateinit var binding: ActivityMainBinding
    private val viewModel: AuthViewModel by viewModels { AuthViewModelFactory() }
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

        // 1. 预填账号和密码（保留上次登录信息，但不跳转）
        val savedEmail = prefs.getString("saved_email", "")
        val savedPwd = prefs.getString("saved_pwd", "")
        if (!savedEmail.isNullOrEmpty() && !savedPwd.isNullOrEmpty()) {
            binding.accountEditText.setText(savedEmail)
            binding.passwordEditText.setText(savedPwd)
        }

        // 2. 点击登录
        binding.loginBottom.setOnClickListener {
            val user = binding.accountEditText.text.toString().trim()
            val pwd = binding.passwordEditText.text.toString()
            if (user.isEmpty() || pwd.isEmpty()) {
                ToastUtil.show(this, "账号和密码不能为空", R.drawable.icon)
                return@setOnClickListener
            }
            viewModel.login(user, pwd)
        }

        // 3. 观察登录结果
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
                // 登录成功，保存凭证
                prefs.edit()
                    .putString("saved_email", binding.accountEditText.text.toString())
                    .putString("saved_pwd", binding.passwordEditText.text.toString())
                    .apply()

                ToastUtil.show(this, "登录成功", R.drawable.icon)
                startActivity(Intent(this, HomeActivity::class.java))
                finish()
            } else {
                ToastUtil.show(this, "账号或密码错误", R.drawable.icon)
            }
        }

        binding.registerImage.setOnClickListener(){
            val intent=Intent(this,RegisterActivity::class.java)
            startActivity(intent)
        }
        binding.test.setOnClickListener {
            Log.d("MainActivity",">> test clicked")
            Toast.makeText(this,"test 点击", Toast.LENGTH_SHORT).show()
            startActivity(Intent(this, HomeActivity::class.java))
        }



        binding.testcity.setOnClickListener(){
            val intent=Intent(this,CityActivity::class.java)
            startActivity(intent)
        }

    }//onCreate end


}
