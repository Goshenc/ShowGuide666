package com.example.filmguide

import android.app.NotificationChannel
import android.app.NotificationManager
import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log
import android.view.ViewGroup
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.lifecycle.lifecycleScope
import androidx.viewpager2.widget.ViewPager2
import com.example.filmguide.databinding.ActivityHomeBinding
import com.google.android.material.tabs.TabLayoutMediator
import com.example.filmguide.logic.network.weather.RetrofitBuilder
import com.example.filmguide.logic.network.weather.WeatherService
import com.example.filmguide.utils.ToastUtil
import com.example.filmguide.utils.Utils_Date_Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.Calendar

class HomeActivity : AppCompatActivity() {
    private var currentPagePosition = 0
    private val apiKey = "670ca929136a456992608cd2e794df24"
    private lateinit var locationUtils: Utils_Date_Location.LocationHelper
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

        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = true
            val cityId = PrefsManager.getCityId(this)

            currentPagePosition = binding.viewPager.currentItem
            Log.d("zxy", currentPagePosition.toString())

            val cityName = PrefsManager.getCityName(this)

            binding.viewPager.adapter = HomeViewPagerAdapter(this, cityId, cityName)

            TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
                when (position) {
                    0 -> tab.text = "正在热映"
                    1 -> tab.text = "即将上映"
                    2 -> tab.text = "明星"
                }
            }.attach()

            binding.viewPager.setCurrentItem(currentPagePosition,false)

            lifecycleScope.launch {
                kotlinx.coroutines.delay(1000)
                withContext(Dispatchers.Main) {
                    binding.swipeRefresh.isRefreshing = false
                    Log.d("zxy", currentPagePosition.toString())
                }
            }
        }


        val cityId = PrefsManager.getCityId(this)
        val cityName = PrefsManager.getCityName(this)

        binding.viewPager.adapter = HomeViewPagerAdapter(this, cityId, cityName)
        binding.viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                super.onPageSelected(position)
                currentPagePosition = position
            }
        })

        TabLayoutMediator(binding.tabLayout, binding.viewPager) { tab, position ->
            when (position) {
                0 -> tab.text = "正在热映"
                1 -> tab.text = "即将上映"
                2 -> tab.text = "明星"
            }
        }.attach()
        locationUtils = Utils_Date_Location.LocationHelper(this)
        getLocation()

        binding.search.setOnClickListener {
            val keyword = binding.searchBox.text.toString().trim()
            if (keyword.isNotEmpty()) {
                startActivity(Intent(this, SearchActivity::class.java).apply {
                    putExtra("keyword", keyword)
                    putExtra("cityId",cityId)
                })
            } else {
                ToastUtil.show(this, "请输入搜索内容", R.drawable.icon)
            }
        }

    }//onCreate end
    private fun getLocation() {
        locationUtils.getLocation { location ->
            if (location != null) {
                val (lat, lng) = location.latitude to location.longitude

                lifecycleScope.launch { getCityIdSuspend("$lng,$lat") }
            } else {
                ToastUtil.show(this, "无法获取当前位置", R.drawable.icon)
            }
        }
    }
    private suspend fun getCityIdSuspend(cityName: String) {
        try {
            val service = RetrofitBuilder.getCityInstance.create(WeatherService::class.java)
            val resp = withContext(Dispatchers.IO) { service.getCity(apiKey, cityName) }
            if (resp.isSuccessful && resp.body()?.code == "200") {
                resp.body()?.location?.firstOrNull()?.let { loc ->
                    withContext(Dispatchers.Main) {
                        binding.textLocation.text = loc.name
                    }
                    getWeatherInfoSuspend(loc.id)
                } ?: ToastUtil.show(this,"获取城市 ID 失败")
            } else {
                ToastUtil.show(this,"获取城市 ID 失败")
            }
        } catch (e: Exception) {
            ToastUtil.show(this,"获取城市 ID 网络请求失败")
        }
    }

    private suspend fun getWeatherInfoSuspend(cityId: String) {
        try {
            val service = RetrofitBuilder.getWeatherInstance.create(WeatherService::class.java)
            val resp = withContext(Dispatchers.IO) { service.getWeather(apiKey, cityId) }
            if (resp.isSuccessful && resp.body()?.code == "200") {
                val today = Utils_Date_Location.formatDate(Calendar.getInstance().time)
                val todayWeather = resp.body()?.daily?.firstOrNull { it.fxDate == today }
                withContext(Dispatchers.Main) {

                }
            } else {
                ToastUtil.show(this,"获取天气信息失败")
            }
        } catch (e: Exception) {
            ToastUtil.show(this,"获取天气信息网络请求失败")
        }
    }



}