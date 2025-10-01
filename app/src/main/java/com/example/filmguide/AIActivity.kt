package com.example.filmguide

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.provider.MediaStore
import android.speech.RecognizerIntent
import android.util.Base64
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.example.filmguide.ai.AIChatService
import com.example.filmguide.databinding.ActivityAiactivityBinding
import com.example.filmguide.logic.model.ChatMessage
import com.example.filmguide.ui.ChatAdapter
import com.example.filmguide.logic.network.weather.RetrofitBuilder
import com.example.filmguide.logic.network.weather.WeatherService
import com.example.filmguide.utils.PrefsManager
import com.example.filmguide.utils.Utils_Date_Location
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*
import java.util.Calendar

class AIActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiactivityBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var aiChatService: AIChatService
    private val conversationHistory = mutableListOf<AIChatService.ChatMessage>()
    private var isAiResponding = false
    private var isDeepThinkingEnabled = false
    private var isWebSearchEnabled = true
    private var selectedImageUri: Uri? = null
    private val apiKey = "670ca929136a456992608cd2e794df24"
    private lateinit var locationUtils: Utils_Date_Location.LocationHelper
    private var currentCityName: String = ""
    private var currentWeatherInfo: String = ""

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
        private const val REQ_SPEECH = 101
        private const val PICK_IMAGE_REQUEST_CODE = 1
    }

    // 图片选择器
    private val imagePickerLauncher = registerForActivityResult(
        ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        uri?.let {
            selectedImageUri = it
            android.util.Log.d("AIActivity", "图片已选择: $it")
            Toast.makeText(this, "图片已选择，可以发送给AI分析", Toast.LENGTH_SHORT).show()
        } ?: run {
            android.util.Log.d("AIActivity", "图片选择被取消")
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        
        // 隐藏悬浮按钮
        hideFloatingIcon()

        // 初始化AI聊天服务
        aiChatService = AIChatService()

        // 测试AI连接
        CoroutineScope(Dispatchers.Main).launch {
            val isConnected = aiChatService.testConnection()
            if (isConnected) {
                android.util.Log.d("AIActivity", "AI服务连接正常")
            } else {
                android.util.Log.e("AIActivity", "AI服务连接失败")
                Toast.makeText(this@AIActivity, "AI服务连接失败，请检查网络", Toast.LENGTH_LONG).show()
            }
        }

        // 一进来显示欢迎语
        val welcomeMessage = ChatMessage(
            "Hi，我是你的AI推荐官ShowGuide！无论你想看演唱会、话剧、音乐剧、电影，还是想找周末市集、艺术展览、亲子活动，" +
            "我都能根据你的喜好帮你推荐最合适的去处。只要告诉我你感兴趣的类型、预算和日期，我就能为你筛选最新最热门的玩乐资讯，" +
            "让你的休闲时光更精彩！快来和我聊聊你的需求吧~", 
            false
        )
        messages.add(welcomeMessage)
        
        // 延迟显示位置和天气信息
        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(2000) // 等待2秒让位置和天气信息获取完成
            if (currentCityName.isNotEmpty() && currentWeatherInfo.isNotEmpty()) {
                val locationMessage = ChatMessage(
                    "📍 当前位置：$currentCityName\n🌤️ 今日天气：$currentWeatherInfo\n\n我可以根据你的位置和天气情况为你推荐合适的活动！", 
                    false
                )
                messages.add(locationMessage)
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            }
        }

        adapter = ChatAdapter(messages)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMessages.adapter = adapter

        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        binding.voice.setOnClickListener {
            RecordDialogFragment { recognizedText ->
                binding.editTextMessage.setText(recognizedText)
            }.show(supportFragmentManager, "record")
        }

        // 深度思考按钮
        binding.deepThinkingButton.setOnClickListener {
            isDeepThinkingEnabled = !isDeepThinkingEnabled
            updateDeepThinkingButton()
        }

        // 联网搜索按钮
        binding.webSearchButton.setOnClickListener {
            isWebSearchEnabled = !isWebSearchEnabled
            updateWebSearchButton()
        }

        // 输入框点击事件（用于图片上传）
        binding.editTextMessage.setOnTouchListener { _, event ->
            // 检查是否点击了相机图标区域
            val drawableStart = binding.editTextMessage.compoundDrawables[0]
            if (drawableStart != null && event.x <= drawableStart.bounds.width() + binding.editTextMessage.paddingStart) {
                imagePickerLauncher.launch("image/*")
                true
            } else {
                false // 让输入框正常处理点击事件
            }
        }

        // 初始化按钮状态
        updateDeepThinkingButton()
        updateWebSearchButton()
        
        // 初始化位置工具并获取城市和天气信息
        locationUtils = Utils_Date_Location.LocationHelper(this)
        // 请求必要权限
        requestPermissionsIfNeeded()
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isEmpty() || isAiResponding) return

        // 处理图片分析（暂时禁用图片功能）
        var finalText = text
        if (selectedImageUri != null) {
            Toast.makeText(this, "图片分析功能暂时不可用，请发送纯文本消息", Toast.LENGTH_SHORT).show()
            selectedImageUri = null // 清除已选择的图片
            return
        }

        // 添加深度思考提示
        if (isDeepThinkingEnabled) {
            finalText = "请深度思考并详细分析：$finalText"
        }

        // 添加用户消息
        val userMessage = ChatMessage(finalText, true)
        messages.add(userMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        binding.editTextMessage.text?.clear()

        // 添加到对话历史
        conversationHistory.add(AIChatService.ChatMessage("user", finalText))

        // 显示AI正在思考的提示
        val thinkingMessage = ChatMessage("AI正在思考中...", false)
        messages.add(thinkingMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)

        // 调用AI服务
        isAiResponding = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                aiChatService.sendMessageWithLocation(
                    userMessage = finalText,
                    conversationHistory = conversationHistory,
                    enableDeepThinking = isDeepThinkingEnabled,
                    enableWebSearch = isWebSearchEnabled,
                    cityName = currentCityName,
                    weatherInfo = currentWeatherInfo,
                    callback = object : AIChatService.StreamCallback {
                        override fun onPartialResponse(content: String) {
                            runOnUiThread {
                                // 更新最后一条消息（思考中...）为AI的回复
                                if (messages.isNotEmpty() && messages.last().content == "AI正在思考中...") {
                                    messages[messages.lastIndex] = ChatMessage(content, false)
                                } else {
                                    // 如果已经有AI回复，则更新内容
                                    if (messages.isNotEmpty() && !messages.last().isSentByUser) {
                                        messages[messages.lastIndex] = ChatMessage(content, false)
                                    } else {
                                        // 添加新的AI回复
                                        messages.add(ChatMessage(content, false))
                                    }
                                }
                                adapter.notifyDataSetChanged()
                                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                            }
                        }

                        override fun onCompleteResponse(content: String) {
                            runOnUiThread {
                                // 确保最终回复正确显示
                                if (messages.isNotEmpty()) {
                                    messages[messages.lastIndex] = ChatMessage(content, false)
                                }
                                adapter.notifyDataSetChanged()
                                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                                
                                // 添加到对话历史
                                conversationHistory.add(AIChatService.ChatMessage("assistant", content))
                                isAiResponding = false
                            }
                        }

                        override fun onError(error: String) {
                            runOnUiThread {
                                // 移除思考中的消息，显示错误信息
                                if (messages.isNotEmpty() && messages.last().content == "AI正在思考中...") {
                                    messages.removeAt(messages.lastIndex)
                                }
                                messages.add(ChatMessage("抱歉，AI服务暂时不可用：$error", false))
                                adapter.notifyDataSetChanged()
                                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                                isAiResponding = false
                                
                                Toast.makeText(this@AIActivity, "AI服务错误：$error", Toast.LENGTH_LONG).show()
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                runOnUiThread {
                    // 移除思考中的消息，显示错误信息
                    if (messages.isNotEmpty() && messages.last().content == "AI正在思考中...") {
                        messages.removeAt(messages.lastIndex)
                    }
                    messages.add(ChatMessage("抱歉，发生了网络错误：${e.message}", false))
                    adapter.notifyDataSetChanged()
                    binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                    isAiResponding = false
                    
                    Toast.makeText(this@AIActivity, "网络错误：${e.message}", Toast.LENGTH_LONG).show()
                }
            }
        }
    }
    
    
    // 以下权限和语音识别逻辑保持不变
    private fun ensureAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this,
                arrayOf(Manifest.permission.RECORD_AUDIO),
                REQUEST_RECORD_AUDIO
            )
        } else {
            startSpeechInput()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int, permissions: Array<out String>, grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        when (requestCode) {
            REQUEST_RECORD_AUDIO -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    startSpeechInput()
                } else {
                    Toast.makeText(this, "需要录音权限才能识别语音", Toast.LENGTH_SHORT).show()
                }
            }
            PICK_IMAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults.all { it == PackageManager.PERMISSION_GRANTED }) {
                    getLocation()
                } else {
                    android.util.Log.w("AIActivity", "位置权限被拒绝")
                    // 即使没有位置权限，也尝试使用已保存的城市信息
                    getLocationAndWeather()
                }
            }
        }
    }

    private fun startSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL, RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话…")
        }
        try {
            startActivityForResult(intent, REQ_SPEECH)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "设备不支持语音识别", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SPEECH && resultCode == Activity.RESULT_OK) {
            val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull().orEmpty()
            binding.editTextMessage.setText(text)
        }
    }
    
    private fun hideFloatingIcon() {
        try {
            val intent = Intent(this, FloatingIconService::class.java).apply {
                action = "hide"
            }
            startService(intent)
        } catch (e: Exception) {
            android.util.Log.e("AIActivity", "隐藏悬浮按钮失败", e)
        }
    }
    
    override fun onPause() {
        super.onPause()
        // 暂停时显示悬浮按钮
        showFloatingIcon()
    }
    
    override fun onStop() {
        super.onStop()
        // 停止时显示悬浮按钮
        showFloatingIcon()
    }
    
    override fun onDestroy() {
        super.onDestroy()
        // 退出AI界面时显示悬浮按钮
        showFloatingIcon()
    }
    
    private fun showFloatingIcon() {
        try {
            val intent = Intent(this, FloatingIconService::class.java).apply {
                action = "show"
            }
            startService(intent)
            android.util.Log.d("AIActivity", "显示悬浮按钮")
        } catch (e: Exception) {
            android.util.Log.e("AIActivity", "显示悬浮按钮失败", e)
        }
    }

    private fun updateDeepThinkingButton() {
        if (isDeepThinkingEnabled) {
            binding.deepThinkingButton.setBackgroundResource(R.drawable.type_tag_background)
            binding.deepThinkingButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.deepThinkingButton.alpha = 1.0f
        } else {
            binding.deepThinkingButton.setBackgroundResource(R.drawable.button_unselected_background)
            binding.deepThinkingButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            binding.deepThinkingButton.alpha = 1.0f
        }
    }

    private fun updateWebSearchButton() {
        if (isWebSearchEnabled) {
            binding.webSearchButton.setBackgroundResource(R.drawable.type_tag_background)
            binding.webSearchButton.setTextColor(ContextCompat.getColor(this, R.color.white))
            binding.webSearchButton.alpha = 1.0f
        } else {
            binding.webSearchButton.setBackgroundResource(R.drawable.button_unselected_background)
            binding.webSearchButton.setTextColor(ContextCompat.getColor(this, R.color.text_secondary))
            binding.webSearchButton.alpha = 1.0f
        }
    }

    private fun convertImageToBase64(uri: Uri): String? {
        return try {
            android.util.Log.d("AIActivity", "开始转换图片: $uri")
            val inputStream: InputStream? = contentResolver.openInputStream(uri)
            if (inputStream == null) {
                android.util.Log.e("AIActivity", "无法打开图片流")
                return null
            }
            
            val bitmap = BitmapFactory.decodeStream(inputStream)
            inputStream.close()
            
            if (bitmap == null) {
                android.util.Log.e("AIActivity", "无法解码图片")
                return null
            }
            
            android.util.Log.d("AIActivity", "原始图片尺寸: ${bitmap.width}x${bitmap.height}")
            
            // 压缩图片
            val compressedBitmap = compressBitmap(bitmap, 800, 600)
            android.util.Log.d("AIActivity", "压缩后图片尺寸: ${compressedBitmap.width}x${compressedBitmap.height}")
            
            val outputStream = ByteArrayOutputStream()
            compressedBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            val byteArray = outputStream.toByteArray()
            outputStream.close()
            
            android.util.Log.d("AIActivity", "图片字节数组大小: ${byteArray.size}")
            val base64String = Base64.encodeToString(byteArray, Base64.DEFAULT)
            android.util.Log.d("AIActivity", "Base64字符串长度: ${base64String.length}")
            
            base64String
        } catch (e: Exception) {
            android.util.Log.e("AIActivity", "图片转换失败", e)
            null
        }
    }

    private fun compressBitmap(bitmap: Bitmap, maxWidth: Int, maxHeight: Int): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        
        if (width <= maxWidth && height <= maxHeight) {
            return bitmap
        }
        
        val scaleWidth = maxWidth.toFloat() / width
        val scaleHeight = maxHeight.toFloat() / height
        val scale = minOf(scaleWidth, scaleHeight)
        
        val newWidth = (width * scale).toInt()
        val newHeight = (height * scale).toInt()
        
        return Bitmap.createScaledBitmap(bitmap, newWidth, newHeight, true)
    }
    
    /**
     * 请求必要权限
     */
    private fun requestPermissionsIfNeeded() {
        val perms = mutableListOf<String>()
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            perms += android.Manifest.permission.ACCESS_FINE_LOCATION
        }
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION)
            != PackageManager.PERMISSION_GRANTED) {
            perms += android.Manifest.permission.ACCESS_COARSE_LOCATION
        }
        if (perms.isNotEmpty()) {
            ActivityCompat.requestPermissions(this, perms.toTypedArray(), PICK_IMAGE_REQUEST_CODE)
        } else {
            getLocation()
        }
    }
    
    
    /**
     * 获取位置信息（直接复制CreateRecordActivity的逻辑）
     */
    private fun getLocation() {
        android.util.Log.d("AIActivity", "开始获取GPS位置")
        locationUtils.getLocation { location ->
            if (location != null) {
                val (lat, lng) = location.latitude to location.longitude
                android.util.Log.d("AIActivity", "获取到GPS位置: $lng, $lat")
                CoroutineScope(Dispatchers.Main).launch { 
                    getCityIdSuspend("$lng,$lat") 
                }
            } else {
                android.util.Log.e("AIActivity", "GPS位置获取失败")
                // 如果GPS失败，尝试使用已保存的城市信息
                getLocationAndWeather()
            }
        }
    }
    
    /**
     * 获取位置和天气信息（备用方案）
     */
    private fun getLocationAndWeather() {
        android.util.Log.d("AIActivity", "使用备用方案获取位置和天气信息")
        // 尝试从PrefsManager获取已保存的城市信息
        val savedCityName = PrefsManager.getCityName(this)
        android.util.Log.d("AIActivity", "已保存的城市名称: $savedCityName")
        
        if (savedCityName.isNotEmpty()) {
            currentCityName = savedCityName
            // 获取该城市的天气信息
            val cityId = PrefsManager.getCityId(this)
            android.util.Log.d("AIActivity", "已保存的城市ID: $cityId")
            if (cityId != -1) {
                CoroutineScope(Dispatchers.Main).launch {
                    getWeatherInfoSuspend(cityId.toString())
                }
            } else {
                currentWeatherInfo = "城市ID无效"
                android.util.Log.w("AIActivity", "城市ID无效: $cityId")
            }
        } else {
            currentCityName = "位置获取失败"
            currentWeatherInfo = "天气信息获取失败"
            android.util.Log.e("AIActivity", "没有保存的城市信息且GPS失败")
        }
    }
    
    /**
     * 根据经纬度获取城市ID
     */
    private suspend fun getCityIdSuspend(location: String) {
        android.util.Log.d("AIActivity", "开始获取城市ID，位置: $location")
        try {
            val service = RetrofitBuilder.getCityInstance.create(WeatherService::class.java)
            val resp = withContext(Dispatchers.IO) { service.getCity(apiKey, location) }
            android.util.Log.d("AIActivity", "城市API响应状态: ${resp.code()}")
            android.util.Log.d("AIActivity", "城市API响应体: ${resp.body()}")
            
            if (resp.isSuccessful && resp.body()?.code == "200") {
                resp.body()?.location?.firstOrNull()?.let { loc ->
                    android.util.Log.d("AIActivity", "获取到城市信息: ${loc.name}, ID: ${loc.id}")
                    withContext(Dispatchers.Main) {
                        currentCityName = loc.name
                    }
                    getWeatherInfoSuspend(loc.id)
                } ?: run {
                    android.util.Log.e("AIActivity", "城市信息为空")
                    withContext(Dispatchers.Main) {
                        currentCityName = "城市获取失败"
                        currentWeatherInfo = "天气信息获取失败"
                    }
                }
            } else {
                android.util.Log.e("AIActivity", "城市API请求失败: ${resp.code()}, ${resp.message()}")
                withContext(Dispatchers.Main) {
                    currentCityName = "城市获取失败"
                    currentWeatherInfo = "天气信息获取失败"
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AIActivity", "城市API网络请求异常", e)
            withContext(Dispatchers.Main) {
                currentCityName = "网络请求失败"
                currentWeatherInfo = "天气信息获取失败"
            }
        }
    }
    
    /**
     * 根据城市ID获取天气信息
     */
    private suspend fun getWeatherInfoSuspend(cityId: String) {
        android.util.Log.d("AIActivity", "开始获取天气信息，城市ID: $cityId")
        try {
            val service = RetrofitBuilder.getWeatherInstance.create(WeatherService::class.java)
            val resp = withContext(Dispatchers.IO) { service.getWeather(apiKey, cityId) }
            android.util.Log.d("AIActivity", "天气API响应状态: ${resp.code()}")
            android.util.Log.d("AIActivity", "天气API响应体: ${resp.body()}")
            
            if (resp.isSuccessful && resp.body()?.code == "200") {
                val today = Utils_Date_Location.formatDate(Calendar.getInstance().time)
                android.util.Log.d("AIActivity", "今天的日期: $today")
                val todayWeather = resp.body()?.daily?.firstOrNull { it.fxDate == today }
                android.util.Log.d("AIActivity", "找到的今日天气: $todayWeather")
                
                withContext(Dispatchers.Main) {
                    currentWeatherInfo = todayWeather?.textDay ?: "天气信息获取失败"
                    android.util.Log.d("AIActivity", "最终天气信息: $currentWeatherInfo")
                }
            } else {
                android.util.Log.e("AIActivity", "天气API请求失败: ${resp.code()}, ${resp.message()}")
                withContext(Dispatchers.Main) {
                    currentWeatherInfo = "天气信息获取失败"
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("AIActivity", "天气API网络请求异常", e)
            withContext(Dispatchers.Main) {
                currentWeatherInfo = "天气信息获取失败"
            }
        }
    }
}
