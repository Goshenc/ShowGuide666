package com.example.filmguide

import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.provider.MediaStore
import android.util.Log
import android.view.View
import android.widget.EditText
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.WindowCompat
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
import java.util.*

class CreateRecordActivity : AppCompatActivity() {
    companion object {
        private const val PICK_IMAGE_REQUEST_CODE = 1
        private const val CAMERA_REQUEST_CODE    = 2
    }

    private lateinit var locationUtils: Utils_Date_Location.LocationHelper
    private lateinit var binding: ActivityCreateRecordBinding
    private lateinit var diaryDatabase: RecordDatabase

    private var selectedLocalImageUri: Uri? = null
    private var networkImageLink: String?    = null
    private var currentPhotoUri: Uri?        = null

    private val apiKey = "670ca929136a456992608cd2e794df24"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCreateRecordBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // 状态栏 inset
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val statusInset = insets.getInsets(WindowInsetsCompat.Type.statusBars())
            v.setPadding(statusInset.left, statusInset.top, statusInset.right, 0)
            insets
        }
        WindowCompat.setDecorFitsSystemWindows(window, false)
        ViewCompat.setOnApplyWindowInsetsListener(binding.bottomRow) { view, insets ->
            val navBarInset = insets.getInsets(WindowInsetsCompat.Type.navigationBars()).bottom
            view.updatePadding(bottom = navBarInset)
            insets
        }
        window.navigationBarColor = ContextCompat.getColor(this, android.R.color.white)
        WindowInsetsControllerCompat(window, window.decorView)
            .isAppearanceLightNavigationBars = true

        val imageUrl = intent.getStringExtra("img")
        val title = intent.getStringExtra("name")
        imageUrl?.let { loadImageFromManage(it) }
        title?.let { loadTitleFromManage(it) }
        diaryDatabase = RecordDatabase.getInstance(this)
        locationUtils = Utils_Date_Location.LocationHelper(this)

        // 点击选择本地图片
        binding.navDiary.setOnClickListener { selectLocalImage() }
        // 点击拍照
        binding.navClock.setOnClickListener  { openCamera() }
        // 点击输入网络图 URL
        binding.navCreate.setOnClickListener { showUrlInputDialog() }
        // 点击保存
        binding.navManage.setOnClickListener { saveDiary() }
        // 刷新定位和天气
        binding.navHome.setOnClickListener {
            getLocation()
            ToastUtil.show(this, "刷新成功!", R.drawable.icon)
        }
        // 请求必要权限
        requestPermissionsIfNeeded()
    }

    private fun requestPermissionsIfNeeded() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            perms += android.Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.READ_EXTERNAL_STORAGE)
            != PackageManager.PERMISSION_GRANTED) {
            perms += android.Manifest.permission.READ_EXTERNAL_STORAGE
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            perms += android.Manifest.permission.CAMERA
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), PICK_IMAGE_REQUEST_CODE)
        } else {
            getLocation()
        }
    }

    /** 方案一：用 ACTION_OPEN_DOCUMENT + 持久权限 来选图库图片 */
    private fun selectLocalImage() {
        Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
            addCategory(Intent.CATEGORY_OPENABLE)
            type = "image/*"
            addFlags(
                Intent.FLAG_GRANT_READ_URI_PERMISSION or
                        Intent.FLAG_GRANT_PERSISTABLE_URI_PERMISSION
            )
        }.also { startActivityForResult(it, PICK_IMAGE_REQUEST_CODE) }
    }

    private fun openCamera() {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.CAMERA)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(android.Manifest.permission.CAMERA),
                CAMERA_REQUEST_CODE
            )
            return
        }
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { cameraIntent ->
            cameraIntent.resolveActivity(packageManager)?.let {
                createImageFile()?.also { file ->
                    currentPhotoUri = FileProvider.getUriForFile(
                        this, "$packageName.provider", file
                    )
                    cameraIntent.putExtra(MediaStore.EXTRA_OUTPUT, currentPhotoUri)
                    startActivityForResult(cameraIntent, CAMERA_REQUEST_CODE)
                }
            } ?: ToastUtil.show(this, "没有可用的相机应用", R.drawable.icon)
        }
    }

    private fun createImageFile(): File? = try {
        val ts = SimpleDateFormat("yyyyMMdd_HHmmss", Locale.getDefault())
            .format(Date())
        val name = "JPEG_${ts}_"
        val dir = getExternalFilesDir(Environment.DIRECTORY_PICTURES)
        File.createTempFile(name, ".jpg", dir)
    } catch (e: Exception) {
        Log.e("CreateRecord", "创建图片文件失败", e)
        null
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        when (requestCode) {
            PICK_IMAGE_REQUEST_CODE -> if (resultCode == RESULT_OK && data?.data != null) {
                val uri = data.data!!
                // 拿持久化读权限
                val takeFlags = data.flags and
                        (Intent.FLAG_GRANT_READ_URI_PERMISSION or Intent.FLAG_GRANT_WRITE_URI_PERMISSION)
                contentResolver.takePersistableUriPermission(uri, takeFlags)

                selectedLocalImageUri = uri
                Glide.with(this)
                    .load(uri)
                    .into(binding.selectedImageView)
                binding.selectedImageView.visibility = View.VISIBLE
            }

            CAMERA_REQUEST_CODE -> if (resultCode == RESULT_OK) {
                selectedLocalImageUri = currentPhotoUri
                Glide.with(this)
                    .load(currentPhotoUri)
                    .into(binding.selectedImageView)
                binding.selectedImageView.visibility = View.VISIBLE
            }
        }
    }

    private fun showUrlInputDialog() {
        val input = EditText(this).apply { 
            hint = "请输入有效的图片链接"
            background = getDrawable(R.drawable.underline_input_field)
            setPadding(0, 24, 0, 24)
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
            elevation = 0f
        }
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("🌐 输入图片URL")
            .setMessage("请输入网络图片的完整链接地址")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotBlank()) {
                    networkImageLink = url
                    Glide.with(this)
                        .load(url)
                        .into(binding.selectedImageView)
                    binding.selectedImageView.visibility = View.VISIBLE
                } else {
                    ToastUtil.show(this, "请输入有效的图片链接", R.drawable.icon)
                    binding.selectedImageView.visibility = View.GONE
                }
            }
            .setNegativeButton("取消", null)
            .create()
            
        dialog.show()
        
        // 美化对话框样式
        dialog.window?.let { window ->
            window.setBackgroundDrawable(getDrawable(R.drawable.glassmorphism_background))
            window.attributes?.let { attributes ->
                attributes.dimAmount = 0.3f
                window.attributes = attributes
            }
        }
    }

    private fun getLocation() {
        locationUtils.getLocation { location ->
            if (location != null) {
                val (lat, lng) = location.latitude to location.longitude
                binding.longtitudeandlatitudeTextView.text = "经纬度:($lng,$lat)"
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
                        binding.locationTextView.text = loc.name
                    }
                    getWeatherInfoSuspend(loc.id)
                } ?: showToast("获取城市 ID 失败")
            } else {
                showToast("获取城市 ID 失败")
            }
        } catch (e: Exception) {
            showToast("获取城市 ID 网络请求失败")
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
                    binding.weatherTextView.text = todayWeather?.textDay ?: "—"
                }
            } else {
                showToast("获取天气信息失败")
            }
        } catch (e: Exception) {
            showToast("获取天气信息网络请求失败")
        }
    }

    private fun saveDiary() {
        val title          = binding.titleEditText.text.toString().trim()
        if (title.isEmpty()) {
            // 标题不能为空
            ToastUtil.show(this, "标题不能为空", R.drawable.icon)
            return
        }
        val article        = binding.articleEditText.text.toString()
        val localPath      = selectedLocalImageUri?.toString()
        val date           = Utils_Date_Location.formatDate(Calendar.getInstance().time)
        val weather        = binding.weatherTextView.text.toString()
        val location       = binding.locationTextView.text.toString()
        val rating         = binding.rating.rating

        val entity = RecordEntity(
            title            = title,
            article          = article,
            localImagePath   = localPath,
            networkImageLink = networkImageLink,
            date             = date,
            weather          = weather,
            location         = location,
            rating           = rating
        )

        lifecycleScope.launch(Dispatchers.IO) {
            diaryDatabase.recordDao().insertRecord(entity)
            withContext(Dispatchers.Main) {
                ToastUtil.show(this@CreateRecordActivity, "保存成功", R.drawable.icon)
                sendBroadcast(Intent("SAVED"))
                finish()
            }
        }
    }

    private fun showToast(msg: String) {
        ToastUtil.show(this, msg, R.drawable.icon)
    }
    private fun loadImageFromManage(url: String) {
        val input = EditText(this).apply { 
            hint = "请输入有效的图片链接"
            background = getDrawable(R.drawable.underline_input_field)
            setPadding(0, 24, 0, 24)
            textSize = 16f
            setTextColor(getColor(R.color.text_primary))
            setHintTextColor(getColor(R.color.text_secondary))
            elevation = 0f
        }
        input.setText(url)
        
        val dialog = AlertDialog.Builder(this)
            .setTitle("🌐 输入图片URL")
            .setMessage("请输入网络图片的完整链接地址")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val url = input.text.toString().trim()
                if (url.isNotBlank()) {
                    networkImageLink = url
                    Glide.with(this)
                        .load(url)
                        .into(binding.selectedImageView)
                    binding.selectedImageView.visibility = View.VISIBLE
                } else {
                    ToastUtil.show(this, "请输入有效的图片链接", R.drawable.icon)
                    binding.selectedImageView.visibility = View.GONE
                }
            }
            .setNegativeButton("取消", null)
            .create()
            
        dialog.show()
        
        // 美化对话框样式
        dialog.window?.let { window ->
            window.setBackgroundDrawable(getDrawable(R.drawable.glassmorphism_background))
            window.attributes?.let { attributes ->
                attributes.dimAmount = 0.3f
                window.attributes = attributes
            }
        }
    }
    private fun loadTitleFromManage(title: String) {
        binding.titleEditText.setText(title + "观影日志")
    }
}
