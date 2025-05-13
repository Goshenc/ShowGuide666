package com.example.filmguide

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.view.ViewGroup
import android.view.WindowManager
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import com.example.filmguide.databinding.ActivityHomeBinding
import com.example.filmguide.logic.network.city.City
import com.google.android.material.tabs.TabLayoutMediator

class HomeActivity : AppCompatActivity() {
    lateinit var binding:ActivityHomeBinding
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        binding=ActivityHomeBinding.inflate(layoutInflater)
        setContentView(binding.root)

        if (PrefsManager.isFirstSelection(this)) {
            startActivity(Intent(this, CityActivity::class.java))
            finish()
            return
        }


        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomRow) { view, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            val display = (this@HomeActivity.getSystemService(Context.WINDOW_SERVICE) as WindowManager).currentWindowMetrics
            val screenHeight = display.bounds.height()

            // 判断是否存在物理导航键
            val hasPhysicalNav = (systemBars.bottom > 0 &&
                    systemBars.bottom != ViewCompat.getRootWindowInsets(view)?.getInsets(WindowInsetsCompat.Type.displayCutout())?.bottom)

            if (hasPhysicalNav) {
                // 物理导航键设备：添加底部内边距
                view.updatePadding(bottom = systemBars.bottom)
            } else {
                // 手势导航设备：不添加内边距（或自定义手势条高度）
                view.updatePadding(bottom = 0)
            }
            insets
        }
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomRow) { view, insets ->
            // 只取导航栏的高度
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom

            // 通过修改 margin 把整个 View 往上推
            (view.layoutParams as ViewGroup.MarginLayoutParams).apply {
                bottomMargin = navBarInset
            }.also { view.layoutParams = it }

            // 方法 B：或者更直接地用 translationY
            // view.translationY = -navBarInset.toFloat()

            // 一定要返回 insets
            insets
        }

        //把底部手势栏背景颜色改成白色
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)
// 底部手势栏图标也要变黑
        WindowInsetsControllerCompat(window, window.decorView).isAppearanceLightNavigationBars = true

// 关闭系统默认 fitsSystemWindows，否则 Insets 不会回调给你的 View
        WindowCompat.setDecorFitsSystemWindows(window, false)

binding.navCreate.setOnClickListener(){
    val intent= Intent(this,CreateRecordActivity::class.java)
    startActivity(intent)
}

binding.navDiary.setOnClickListener(){
    val intent=Intent(this,RecordsActivity::class.java)
    startActivity(intent)
}
binding.imgLocation.setOnClickListener(){
    val intent=Intent(this,CityActivity::class.java)
    startActivity(intent)
}
        binding.navClock.setOnClickListener(){
            val intent=Intent(this,ReminderActivity::class.java)
            startActivity(intent)
        }
        // Application 或者 MainActivity 中
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "default",
                "提醒通知",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "提醒时间到达时的通知"
            }
            val nm = getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            nm.createNotificationChannel(channel)
        }

        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }


        val cityId = PrefsManager.getCityId(this)
        val cityName = PrefsManager.getCityName(this)

        binding.viewPager.adapter = HomeViewPagerAdapter(this, cityId, cityName)

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "正在热映"
                1 -> tab.text = "即将上映"
                2 -> tab.text = "明星"
            }
        }.attach()

    }//onCreate end
}