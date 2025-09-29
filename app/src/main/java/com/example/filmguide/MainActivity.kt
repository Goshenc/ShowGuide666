package com.example.filmguide

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.ComponentName
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Log
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey
import com.example.filmguide.databinding.ActivityMainBinding
import com.example.filmguide.ui.AuthViewModel
import com.example.filmguide.ui.AuthViewModelFactory
import com.example.filmguide.utils.ToastUtil

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




        // 请求自启动设置（仅首次打开）
        val appPrefs = getSharedPreferences("app_prefs", MODE_PRIVATE)
        if (!appPrefs.getBoolean("asked_autostart", false)) {
            showAutoStartDialog()
            appPrefs.edit().putBoolean("asked_autostart", true).apply()
        }

        // 预填账号和密码
        val savedEmail = prefs.getString("saved_email", "")
        val savedPwd = prefs.getString("saved_pwd", "")
        if (!savedEmail.isNullOrEmpty() && !savedPwd.isNullOrEmpty()) {
            binding.accountEditText.setText(savedEmail)
            binding.passwordEditText.setText(savedPwd)
        }

        // 点击登录
        binding.loginBottom.setOnClickListener {
            val user = binding.accountEditText.text.toString().trim()
            val pwd = binding.passwordEditText.text.toString()
            if (user.isEmpty() || pwd.isEmpty()) {
                ToastUtil.show(this, "账号和密码不能为空", R.drawable.icon)
                return@setOnClickListener
            }
            viewModel.login(user, pwd)
        }

        // 观察登录结果
        viewModel.loginSuccess.observe(this) { success ->
            if (success) {
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

        binding.registerText.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }

        binding.visitorLoginBottom.setOnClickListener {
            Log.d("MainActivity", ">> test clicked")
            ToastUtil.show(this, "游客登录", R.drawable.icon)
            startActivity(Intent(this, HomeActivity::class.java))
        }


        // 创建通知渠道
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "默认提醒"
            val descriptionText = "用于提醒的通知渠道"
            val importance = NotificationManager.IMPORTANCE_DEFAULT
            val channel = NotificationChannel("default", name, importance).apply {
                description = descriptionText
            }
            val notificationManager: NotificationManager =
                getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    /** 弹出自启动设置引导对话框 */
    private fun showAutoStartDialog() {
        AlertDialog.Builder(this)
            .setTitle("开启自启动")
            .setMessage("为了保证提醒能在后台准时触发，请允许应用自启动。")
            .setPositiveButton("去设置") { _, _ -> openAutoStartSettings() }
            .setNegativeButton("取消", null)
            .show()
    }

    /** 跳转到厂商自启动管理页 */
    private fun openAutoStartSettings() {
        val manufacturer = Build.MANUFACTURER.lowercase()
        try {
            val intent = when {
                manufacturer.contains("xiaomi") -> Intent().apply {
                    component = ComponentName(
                        "com.miui.securitycenter",
                        "com.miui.permcenter.autostart.AutoStartManagementActivity"
                    )
                }
                manufacturer.contains("oppo") -> Intent().apply {
                    component = ComponentName(
                        "com.coloros.safecenter",
                        "com.coloros.safecenter.permission.startup.StartupAppListActivity"
                    )
                }
                manufacturer.contains("vivo") -> Intent().apply {
                    component = ComponentName(
                        "com.iqoo.secure",
                        "com.iqoo.secure.ui.phoneoptimize.BgStartUpManager"
                    )
                }
                manufacturer.contains("huawei") || manufacturer.contains("honor") -> Intent().apply {
                    component = ComponentName(
                        "com.huawei.systemmanager",
                        "com.huawei.systemmanager.startupmgr.ui.StartupNormalAppListActivity"
                    )
                }
                else -> Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS).apply {
                    data = Uri.parse("package:$packageName")
                }
            }
            startActivity(intent)
        } catch (e: Exception) {
            startActivity(
                Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                    .apply { data = Uri.parse("package:$packageName") }
            )
        }
    }
}
