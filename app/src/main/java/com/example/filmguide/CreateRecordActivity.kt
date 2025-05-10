package com.example.filmguide

import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.EditText
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowInsetsControllerCompat
import androidx.core.view.updatePadding
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.example.filmguide.databinding.ActivityCreateRecordBinding
import com.example.filmguide.logic.model.CityItem
import com.example.filmguide.logic.model.WeatherItem
import com.example.filmguide.logic.network.weather.RetrofitBuilder
import com.example.filmguide.logic.network.weather.WeatherService
import com.example.filmguide.logic.recordroom.RecordDatabase
import com.example.filmguide.logic.recordroom.RecordEntity
import com.example.filmguide.utils.ToastUtil
import com.example.filmguide.utils.Utils_Date_Location
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import retrofit2.Response
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class CreateRecordActivity : AppCompatActivity() {
    private lateinit var locationUtils: Utils_Date_Location.LocationHelper
    private lateinit var binding: ActivityCreateRecordBinding
    private lateinit var diaryDatabase: RecordDatabase
    private val PERMISSION_REQUEST_CODE = 1
    private val CAMERA_REQUEST_CODE = 2
    private var selectedLocalImageUri: Uri? = null
    private var networkImageLink: String? = null
    private var currentPhotoUri: Uri? = null
    private val apiKey = "670ca929136a456992608cd2e794df24"

    private fun hasPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        // 只给状态栏留 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusInset = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(statusInset.left, statusInset.top, statusInset.right, 0)
            insets
        }

        // 关闭框架自动 fitsSystemWindows
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // bottomRow 单独处理导航栏 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomRow) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navBarInset)
            insets
        }

        // 导航栏白底黑字
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true




        diaryDatabase = RecordDatabase.getInstance(this)
        locationUtils = Utils_Date_Location.LocationHelper(this)


        binding.navDiary.setOnClickListener { selectLocalImage() }

        binding.navClock.setOnClickListener { openCamera() }

        binding.navCreate.setOnClickListener { showUrlInputDialog() }

        binding.navManage.setOnClickListener { saveDiary() }

        binding.navHome.setOnClickListener { getLocation()
            if (binding.weatherTextView.text!=null)
        ToastUtil.show(this,"刷新成功!",R.drawable.icon)}

        val permissionsToRequest = mutableListOf<String>()
        if (!hasPermission(android.Manifest.permission.ACCESS_FINE_LOCATION)) {
            permissionsToRequest.add(android.Manifest.permission.ACCESS_FINE_LOCATION)
        }
        if (!hasPermission(android.Manifest.permission.READ_EXTERNAL_STORAGE)) {
            permissionsToRequest.add(android.Manifest.permission.READ_EXTERNAL_STORAGE)
        }
        if (!hasPermission(android.Manifest.permission.CAMERA)) {
            permissionsToRequest.add(android.Manifest.permission.CAMERA)
        }
        if (permissionsToRequest.isNotEmpty()) {
            ActivityCompat.requestPermissions(
                this,
                permissionsToRequest.toTypedArray(),
                PERMISSION_REQUEST_CODE
            )
        } else {
            getLocation()
        }
    }

    private fun selectLocalImage() {
        val intent = Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI)
        startActivityForResult(intent, 1)
    }

    private fun openCamera() {

        if (!hasPermission(android.Manifest.permission.CAMERA)) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                PERMISSION_REQUEST_CODE
            )
            return
        }
        //创建一个 Intent 对象，并指定动作为 MediaStore.ACTION_IMAGE_CAPTURE，即启动相机拍照。
        val cameraIntent = Intent(MediaStore.ACTION_IMAGE_CAPTURE)
        if (cameraIntent.resolveActivity(packageManager) != null) {
            // 创建用于保存照片的文件
            val photoFile = createImageFile()
            photoFile?.also {
                currentPhotoUri = FileProvider.getUriForFile(this, "$packageName.provider", it)
                cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
            }
        } else {

            ToastUtil.show(this,"没有可用的相机应用",R.drawable.icon)
        }
    }

    // 创建用于存储拍照图片的临时文件
    private fun createImageFile(): File? {
        return try {
            val timeStamp = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault()).format(Date())
            val imageFileName = "JPEG_${timeStamp}_"
            val storageDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            File.createTempFile(imageFileName, ".jpg", storageDir)
        } catch (ex: Exception) {
            Log.e("AddDiaryActivity", "创建图片文件失败", ex)
            null
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when {
            requestCode == 1 && resultCode == RESULT_OK && data != null -> {
                selectedLocalImageUri = data.data//当用户选择一张图片后，图库应用会自动将该图片的 URI 填充到返回的 Intent 的 data 属性中，也就是 data.data
                Glide.with(this)
                    .load(selectedLocalImageUri)
                    .into(binding.selectedImageView)
                binding.selectedImageView.visibility = View.VISIBLE
            }
            requestCode == CAMERA_REQUEST_CODE && resultCode == RESULT_OK -> {
                // 拍照成功，使用 currentPhotoUri 保存的图片
                selectedLocalImageUri = currentPhotoUri
                Glide.with(this)
                    .load(selectedLocalImageUri)
                    .into(binding.selectedImageView)
                binding.selectedImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun showUrlInputDialog() {
        val input = EditText(this)//使用 EditText(this) 创建了一个输入框，让用户在弹出对话框中输入文本。
        input.hint = "请输入有效的图片链接"
        AlertDialog.Builder(this)
            .setTitle("输入图片URL")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotBlank()) {
                    networkImageLink= url
                    Glide.with(this)
                        .load(url)

                        .into(binding.selectedImageView)
                    binding.selectedImageView.visibility = View.VISIBLE
                } else {
                    showToast("请输入有效的图片链接")
                    binding.selectedImageView.visibility = View.GONE
                }
            }
            .setNegativeButton("取消") { dialog, _ ->
                dialog.cancel()
            }
            .show()
    }

    private fun getLocation() {
        locationUtils.getLocation { location ->//这里的location是由 Utils_Date_Location.LocationHelper.getLocation 方法传递的 Location 对象。
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("AddDiaryActivity", "获取到的经纬度: 纬度 $latitude, 经度 $longitude")
                binding.longtitudeandlatitudeTextView.text="经纬度:($latitude,$longitude)"
                val loc = String.format("%.2f,%.2f", location.longitude, location.latitude)
                // 使用协程调用 suspend 函数获取城市信息和天气
                lifecycleScope.launch {
                    getCityIdSuspend(loc)
                }
            } else {
                runOnUiThread {

                    ToastUtil.show(this,"无法获取当前位置",R.drawable.icon)
                }
            }
        }
    }

    // suspend 函数用于获取城市信息
    private suspend fun getCityIdSuspend(cityName: String) {
        try {
            val weatherService = RetrofitBuilder.getCityInstance.create(WeatherService::class.java)
            val response: Response<CityItem> = withContext(Dispatchers.IO) {////response.body() 获取 服务器返回的 JSON 数据，并转换成 CityItem 对象
                weatherService.getCity(apiKey, cityName)
            }
            if (response.isSuccessful && response.body()?.code == "200") {
                val cityLocation = response.body()?.location?.firstOrNull()
                if (cityLocation != null) {
                    // 更新 UI 必须在主线程
                    withContext(Dispatchers.Main) {
                        binding.locationTextView.text = cityLocation.name
                    }
                    // 调用 suspend 函数获取天气信息
                    getWeatherInfoSuspend(cityLocation.id)
                } else {
                    showToast("获取城市 ID 失败")
                }
            } else {
                Log.e("AddDiaryActivity", "获取城市 ID 失败: ${response.message()}, code: ${response.code()}, error: ${response.errorBody()?.string()}")
                showToast("获取城市 ID 失败")
            }
        } catch (e: Exception) {
            Log.e("AddDiaryActivity", "获取城市 ID 网络请求失败: ${e.message}", e)
            showToast("获取城市 ID 网络请求失败")
        }
    }

    // suspend 函数用于获取天气信息
    private suspend fun getWeatherInfoSuspend(cityId: String) {
        try {
            val weatherService = RetrofitBuilder.getWeatherInstance.create(WeatherService::class.java)
            val response: Response<WeatherItem> = withContext(Dispatchers.IO) {
                weatherService.getWeather(apiKey, cityId)
            }
            if (response.isSuccessful && response.body()?.code == "200") {//response.body() 获取 服务器返回的 JSON 数据，并转换成 WeatherItem 对象
                val today = Utils_Date_Location.formatDate(Calendar.getInstance().time)//使用 Calendar.getInstance().time 获取当前时间。
                val todayWeather = response.body()?.daily?.firstOrNull { it.fxDate == today }
                if (todayWeather != null) {
                    withContext(Dispatchers.Main) {
                        binding.weatherTextView.text = todayWeather.textDay
                    }
                } else {
                    showToast("获取天气信息失败")
                }
            } else {
                Log.e("AddDiaryActivity", "获取天气信息失败: ${response.message()}, code: ${response.code()}, error: ${response.errorBody()?.string()}")
                showToast("获取天气信息失败")
            }
        } catch (e: Exception) {
            Log.e("AddDiaryActivity", "获取天气信息网络请求失败: ${e.message}", e)
            showToast("获取天气信息网络请求失败")
        }
    }


    private fun showToast(message: String) {
        runOnUiThread {

            ToastUtil.show(this,message,R.drawable.icon)
             }
    }

    private fun saveDiary() {
        val title = binding.titleEditText.text.toString()
        val article = binding.articleEditText.text.toString()
        val localImagePath = selectedLocalImageUri?.toString()
        val date = Utils_Date_Location.formatDate(Calendar.getInstance().time)
        val weather = binding.weatherTextView.text.toString()
        val location = binding.locationTextView.text.toString()

        val diaryEntity = RecordEntity(
            title = title,
            article = article,
            localImagePath = localImagePath,
            networkImageLink = networkImageLink,
            date = date,
            weather = weather,
            location = location
        )

        lifecycleScope.launch(Dispatchers.IO) {//在 IO线程 中启动一个协程，因为数据库操作是耗时任务，不适合在主线程中执行。
            diaryDatabase.recordDao().insertRecord(diaryEntity)
            withContext(Dispatchers.Main) {

                ToastUtil.show(this@CreateRecordActivity,"保存成功",R.drawable.icon)
                val intent = Intent("SAVED")
                sendBroadcast(intent)
                finish()
            }
        }
    }
}

/* //enqueue回调方法，需要恢复weatherService的代码再使用,对比书上P454
    // 修改后的获取城市信息方法（不再是 suspend 函数）
    private fun getCityId(cityName: String) {
        val weatherService = RetrofitBuilder.getCityInstance.create(WeatherService::class.java)
        weatherService.getCity(apiKey, cityName).enqueue(object : Callback<CityItem> {
            override fun onResponse(call: Call<CityItem>, response: Response<CityItem>) {
                if (response.isSuccessful && response.body()?.code == "200") {
                    val cityLocation = response.body()?.location?.firstOrNull()
                    if (cityLocation != null) {
                        // 更新 UI 必须在主线程
                        runOnUiThread { binding.locationTextView.text = cityLocation.name }
                        // 请求天气信息
                        getWeatherInfo(cityLocation.id)
                    } else {
                        showToast("获取城市 ID 失败")
                    }
                } else {
                    Log.e(
                        "AddDiaryActivity",
                        "获取城市 ID 失败: ${response.message()}, code: ${response.code()}, error: ${response.errorBody()?.string()}"
                    )
                    showToast("获取城市 ID 失败")
                }
            }

            override fun onFailure(call: Call<CityItem>, t: Throwable) {
                Log.e("AddDiaryActivity", "获取城市 ID 网络请求失败: ${t.message}", t)
                showToast("获取城市 ID 网络请求失败")
            }
        })
    }

    // 修改后的获取天气信息方法
    private fun getWeatherInfo(cityId: String) {
        val weatherService = RetrofitBuilder.getWeatherInstance.create(WeatherService::class.java)
        weatherService.getWeather(apiKey, cityId).enqueue(object : Callback<WeatherItem> {
            override fun onResponse(call: Call<WeatherItem>, response: Response<WeatherItem>) {
                if (response.isSuccessful && response.body()?.code == "200") {
                    val today = Utils.formatDate(Calendar.getInstance().time)
                    val todayWeather = response.body()?.daily?.firstOrNull { it.fxDate == today }
                    if (todayWeather != null) {
                        runOnUiThread { binding.weatherTextView.text = todayWeather.textDay }
                    } else {
                        showToast("获取天气信息失败")
                    }
                } else {
                    Log.e(
                        "AddDiaryActivity",
                        "获取天气信息失败: ${response.message()}, code: ${response.code()}, error: ${response.errorBody()?.string()}"
                    )
                    showToast("获取天气信息失败")
                }
            }

            override fun onFailure(call: Call<WeatherItem>, t: Throwable) {
                Log.e("AddDiaryActivity", "获取天气信息网络请求失败: ${t.message}", t)
                showToast("获取天气信息网络请求失败")
            }
        })
    }

    // 修改 getLocation 方法，去掉协程调用，直接调用新的 getCityId 方法
    private fun getLocation() {
        locationUtils.getLocation { location ->
            if (location != null) {
                val latitude = location.latitude
                val longitude = location.longitude
                Log.d("AddDiaryActivity", "获取到的经纬度: 纬度 $latitude, 经度 $longitude")
                val loc = String.format("%.2f,%.2f", location.longitude, location.latitude)
                runOnUiThread { binding.locationTextView.text = loc }
                // 直接调用非 suspend 的 getCityId 方法
                getCityId(loc)
            } else {
                runOnUiThread {
                    Toast.makeText(this, "无法获取当前位置", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }
*/