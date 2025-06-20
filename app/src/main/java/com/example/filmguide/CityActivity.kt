package com.example.filmguide

// CityActivity.kt
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.databinding.ActivityCityBinding
import com.example.filmguide.logic.network.city.City
import com.example.filmguide.logic.network.city.CityClient
import com.example.filmguide.ui.CityAdapter
import com.example.filmguide.utils.ToastUtil

import kotlinx.coroutines.launch
import kotlin.math.log

class CityActivity : AppCompatActivity() {
    lateinit var binding: ActivityCityBinding
    private val adapter = CityAdapter { city ->
        PrefsManager.saveCityInfo(this, city.id, city.name)
        val intent = Intent(this, HomeActivity::class.java)
        startActivity(intent)
        finish()
    }

    private var allCityList = mutableListOf<City>()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etPlaceName: EditText = findViewById(R.id.search_edit_text)
        val btnOpenMaps: View = findViewById(R.id.img_location)
        btnOpenMaps.setOnClickListener {
            val placeName = etPlaceName.text.toString().trim()
            if (placeName.isEmpty()) {
                ToastUtil.show(this,"请输入地名",R.drawable.icon)
                return@setOnClickListener
            }
            showMapChooser(this, placeName)
        }




        binding.recyclerView.layoutManager = LinearLayoutManager(this)
        binding.recyclerView.adapter = adapter

        // 直接在 Activity 的协程作用域里发请求
        lifecycleScope.launch {
            try {
                val response = CityClient.cityApi.getCities()
                val cityList = response.cts
                allCityList.clear()
                allCityList.addAll(cityList)

                intent.getStringExtra("EXTRA_PLACE_NAME")?.let { place ->
                    // 填充输入并筛选
                    binding.searchEditText.setText(place)
                    val filtered = cityList.filter { it.name.contains(place, ignoreCase = true) }
                    adapter.submitList(filtered)
                    binding.recyclerView.scrollToPosition(0)
                } ?: run {
                    // 无传入关键字，直接展示所有城市
                    adapter.submitList(cityList)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                ToastUtil.show(this@CityActivity, "加载失败：${e.message}", R.drawable.icon)
            }
        }

        // 为搜索 EditText 添加文本变化监听
        binding.searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                s?.let { keyword ->
                    val filteredList = allCityList.filter { city ->
                        city.name.contains(keyword, ignoreCase = true)
                    }
                    adapter.submitList(filteredList)
                }
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }







    private fun showMapChooser(context: Context, placeName: String) {
        val options = mutableListOf<Pair<String, Intent>>()
        getBaiduMapIntent(context, placeName)?.let {
            options.add("百度地图" to it)
        }
        getAmapIntent(context, placeName)?.let {
            options.add("高德地图" to it)
        }
        getGoogleMapIntent(context, placeName)?.let {
            options.add("Google 地图" to it)
        }
        getGoogleEarthIntent(context, placeName)?.let {
            options.add("Google Earth" to it)
        }

        if (options.isEmpty()) {
            ToastUtil.show(context,"未检测到已安装的地图应用",R.drawable.icon)
            return
        }
        // 如果只有一个应用，直接启动
        if (options.size == 1) {
            try {
                context.startActivity(options[0].second)
            } catch (e: Exception) {
                ToastUtil.show(context,"无法启动${options[0].first}",R.drawable.icon)
            }
            return
        }
        val names = options.map { it.first }.toTypedArray()
        AlertDialog.Builder(context)
            .setTitle("请选择地图应用")
            .setItems(names) { _, which ->
                val intent = options[which].second
                try {
                    context.startActivity(intent)
                } catch (e: Exception) {
                    ToastUtil.show(context,"无法启动${options[which].first}",R.drawable.icon)
                }
            }
            .show()
    }

    private fun isIntentAvailable(context: Context, intent: Intent): Boolean {
        val resolveInfoList = context.packageManager.queryIntentActivities(intent, PackageManager.MATCH_DEFAULT_ONLY)
        return resolveInfoList.isNotEmpty()
    }

    private fun isPackageInstalled(context: Context, packageName: String): Boolean {
        return try {
            context.packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES)
            true
        } catch (e: PackageManager.NameNotFoundException) {
            false
        }
    }

    // 构造百度地图查询 Intent
    private fun getBaiduMapIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.baidu.BaiduMap")) return null
        // 使用百度地图搜索地名
        val uri = Uri.parse(
            "baidumap://map/place/search?query=${Uri.encode(query)}&src=${Uri.encode(context.packageName)}"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.baidu.BaiduMap")
        return if (isIntentAvailable(context, intent)) intent else null
    }

    // 构造高德地图查询 Intent
    private fun getAmapIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.autonavi.minimap")) return null
        // 高德地图关键字搜索
        val uri = Uri.parse(
            "androidamap://keyword?sourceApplication=${Uri.encode(context.packageName)}&keyword=${Uri.encode(query)}&dev=0"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.autonavi.minimap")
        return if (isIntentAvailable(context, intent)) intent else null
    }

    // 构造 Google 地图查询 Intent
    private fun getGoogleMapIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.google.android.apps.maps")) return null
        // 使用 geo URI 搜索地名
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        return if (isIntentAvailable(context, intent)) intent else null
    }

    // 构造 Google Earth 查询 Intent（尝试使用 geo URI）
    private fun getGoogleEarthIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.google.earth")) return null
        // Google Earth 对 geo URI 支持不一定完全，但尝试搜索地名
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.earth")
        return if (isIntentAvailable(context, intent)) intent else null
    }


}
