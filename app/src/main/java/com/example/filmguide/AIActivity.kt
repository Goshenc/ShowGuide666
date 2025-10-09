package com.example.filmguide

import android.Manifest
import android.app.Activity
import android.app.AlarmManager
import android.app.DatePickerDialog
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.ActivityNotFoundException
import android.content.Context
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
import android.text.TextUtils
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import java.io.ByteArrayOutputStream
import java.io.InputStream
import com.example.filmguide.ai.AIChatService
import com.example.filmguide.databinding.ActivityAiactivityBinding
import com.example.filmguide.logic.model.ChatMessage
import com.example.filmguide.logic.model.Reminder
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
import java.util.UUID
import java.util.regex.Pattern

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

        // 原来的语音识别功能 - 只识别语音，不处理命令
        binding.voice.setOnClickListener {
            RecordDialogFragment { recognizedText ->
                // 如果用户主动结束录音（空字符串），不处理
                if (recognizedText.isEmpty()) {
                    return@RecordDialogFragment
                }
                
                // 直接设置到输入框，不处理命令
                binding.editTextMessage.setText(recognizedText)
            }.show(supportFragmentManager, "record")
        }
        
        // 新的语音对话功能 - 支持语音命令处理
        binding.voiceChat.setOnClickListener {
            RecordDialogFragment { recognizedText ->
                // 如果用户主动结束录音（空字符串），不处理
                if (recognizedText.isEmpty()) {
                    return@RecordDialogFragment
                }
                
                // 直接显示用户语音内容，不加前缀
                val voiceMessage = ChatMessage(recognizedText, true)
                messages.add(voiceMessage)
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                
                // 处理语音命令
                if (handleVoiceCommand(recognizedText)) {
                    // 如果是语音命令，不发送给AI
                    return@RecordDialogFragment
                }
                
                // 普通语音输入，直接发送给AI处理
                sendVoiceMessageToAI(recognizedText)
            }.show(supportFragmentManager, "voiceChat")
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

        // 检查是否是语音命令（文本输入也支持）
        if (handleVoiceCommand(text)) {
            // 如果是语音命令，不发送给AI
            binding.editTextMessage.text?.clear()
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
    
    /**
     * 发送语音消息给AI处理
     */
    private fun sendVoiceMessageToAI(text: String) {
        if (text.isEmpty() || isAiResponding) return

        // 添加深度思考提示
        var finalText = text
        if (isDeepThinkingEnabled) {
            finalText = "请深度思考并详细分析：$finalText"
        }

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
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话，支持语音控制：打开搜索、查看记录、设置提醒、推荐电影、导航等")
            putExtra(RecognizerIntent.EXTRA_MAX_RESULTS, 3) // 获取多个识别结果
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true) // 支持部分结果
        }
        try {
            startActivityForResult(intent, REQ_SPEECH)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "设备不支持语音识别", Toast.LENGTH_SHORT).show()
        }
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SPEECH) {
            if (resultCode == Activity.RESULT_OK) {
                val results = data?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                val text = results?.firstOrNull().orEmpty()
                
                if (text.isNotEmpty()) {
                    // 直接显示用户语音内容，不加前缀
                    val voiceMessage = ChatMessage(text, true)
                    messages.add(voiceMessage)
                    adapter.notifyItemInserted(messages.lastIndex)
                    binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                    
                    // 处理语音命令
                    if (handleVoiceCommand(text)) {
                        // 如果是语音命令，不发送给AI
                        return
                    }
                    
                    // 普通语音输入，直接发送给AI处理
                    sendVoiceMessageToAI(text)
                } else {
                    val errorMessage = ChatMessage("语音识别失败，请重试", true)
                    messages.add(errorMessage)
                    adapter.notifyItemInserted(messages.lastIndex)
                    binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                }
            } else {
                // 语音识别被取消或失败
                val cancelMessage = ChatMessage("语音识别已取消", true)
                messages.add(cancelMessage)
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            }
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
    
    /**
     * 处理语音命令
     * @param text 语音识别的文本
     * @return true 如果是语音命令并已处理，false 如果是普通对话
     */
    private fun handleVoiceCommand(text: String): Boolean {
        if (text.isEmpty()) return false
        
        val lowerText = text.lowercase()
        Log.d("AIActivity", "处理语音命令: $text")
        
        // 1. 打开搜索页面 - 扩展关键词
        if (lowerText.contains("打开搜索") || lowerText.contains("搜索页面") || lowerText.contains("搜索功能") ||
            lowerText.contains("搜索") || lowerText.contains("查找") || lowerText.contains("找电影") ||
            lowerText.contains("找演出") || lowerText.contains("搜索电影") || lowerText.contains("搜索演出")) {
            openSearchPage()
            return true
        }
        
        // 2. 查看观影记录 - 扩展关键词
        if (lowerText.contains("观影记录") || lowerText.contains("我的记录") || lowerText.contains("查看记录") ||
            lowerText.contains("记录") || lowerText.contains("日记") || lowerText.contains("我的日记") ||
            lowerText.contains("观影日记") || lowerText.contains("看过的电影") || lowerText.contains("历史记录")) {
            openRecordsPage()
            return true
        }
        
        // 3. 设置提醒 - 扩展关键词
        if (lowerText.contains("设置提醒") || lowerText.contains("设置闹钟") || lowerText.contains("提醒") ||
            lowerText.contains("闹钟") || lowerText.contains("定时") || lowerText.contains("提醒我") ||
            lowerText.contains("叫我") || lowerText.contains("提醒功能")) {
            if (lowerText.contains("明天") || lowerText.contains("下周") || lowerText.contains("点") || 
                lowerText.contains("时") || lowerText.contains("早上") || lowerText.contains("晚上") ||
                lowerText.contains("上午") || lowerText.contains("下午") || lowerText.contains("凌晨")) {
                // 智能解析时间并设置提醒
                parseAndSetReminder(text)
            } else {
                // 打开提醒设置页面
                openReminderPage()
            }
            return true
        }
        
        // 4. 推荐电影 - 扩展关键词
        if (lowerText.contains("推荐电影") || lowerText.contains("推荐") || lowerText.contains("电影推荐") ||
            lowerText.contains("推荐一些") || lowerText.contains("有什么好看的") || lowerText.contains("好看的电影") ||
            lowerText.contains("电影推荐") || lowerText.contains("推荐影片") || lowerText.contains("推荐几部")) {
            requestMovieRecommendation()
            return true
        }
        
        // 5. 导航设置 - 扩展关键词
        if (lowerText.contains("导航") || lowerText.contains("地图") || lowerText.contains("想去") || 
            lowerText.contains("导航到") || lowerText.contains("怎么去") || lowerText.contains("路线") ||
            lowerText.contains("导航功能") || lowerText.contains("地图导航") || lowerText.contains("导航到") ||
            lowerText.contains("我要去") || lowerText.contains("去") || lowerText.contains("到") ||
            lowerText.contains("怎么走") || lowerText.contains("路线") || lowerText.contains("导航") ||
            lowerText.contains("地图") || lowerText.contains("位置") || lowerText.contains("地址")) {
            handleNavigationCommand(text)
            return true
        }
        
        // 6. 帮助命令
        if (lowerText.contains("帮助") || lowerText.contains("语音命令") || lowerText.contains("能做什么") ||
            lowerText.contains("功能") || lowerText.contains("命令") || lowerText.contains("语音控制")) {
            showVoiceHelp()
            return true
        }
        
        return false
    }
    
    /**
     * 显示语音帮助信息
     */
    private fun showVoiceHelp() {
        val helpText = """
            🎤 语音功能说明：
            
            📱 两个语音按钮：
            • 左侧按钮：纯语音识别，识别后填入输入框
            • 右侧按钮：语音对话，支持语音命令处理
            
            🔍 语音命令功能：
            • "打开搜索" / "搜索电影" / "查找演出"
            • "查看记录" / "我的日记" / "观影记录"  
            • "设置提醒" / "提醒我明天8点" / "设置闹钟"
            • "推荐电影" / "有什么好看的" / "推荐一些电影"
            • "我要去南京" / "想去故宫" / "导航到天安门"
            • "南京怎么去" / "故宫路线" / "怎么去北京"
            
            💡 提示：文本输入也支持所有语音命令功能
        """.trimIndent()
        
        val helpMessage = ChatMessage(helpText, false)
        messages.add(helpMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
    }
    
    /**
     * 打开搜索页面
     */
    private fun openSearchPage() {
        val intent = Intent(this, SearchActivity::class.java)
        intent.putExtra("cityId", PrefsManager.getCityId(this))
        intent.putExtra("keyword", "")
        startActivity(intent)
        
        // 显示确认消息
        val confirmMessage = ChatMessage("🔍 已为您打开搜索页面", false)
        messages.add(confirmMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
    }
    
    /**
     * 打开观影记录页面
     */
    private fun openRecordsPage() {
        val intent = Intent(this, RecordsActivity::class.java)
        startActivity(intent)
        
        // 显示确认消息
        val confirmMessage = ChatMessage("📝 已为您打开观影记录页面", false)
        messages.add(confirmMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
    }
    
    /**
     * 打开提醒设置页面
     */
    private fun openReminderPage() {
        val intent = Intent(this, ReminderActivity::class.java)
        startActivity(intent)
        
        // 显示确认消息
        val confirmMessage = ChatMessage("⏰ 已为您打开提醒设置页面", false)
        messages.add(confirmMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
    }
    
    /**
     * 请求电影推荐
     */
    private fun requestMovieRecommendation() {
        val recommendMessage = ChatMessage("🎬 正在为您推荐电影...", false)
        messages.add(recommendMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        
        // 调用AI推荐功能
        CoroutineScope(Dispatchers.Main).launch {
            try {
                val userMessage = "请推荐一些好看的电影，考虑当前天气：$currentWeatherInfo，位置：$currentCityName"
                
                // 使用sendMessageWithLocation方法
                aiChatService.sendMessageWithLocation(
                    userMessage = userMessage,
                    cityName = currentCityName,
                    weatherInfo = currentWeatherInfo,
                    enableDeepThinking = false,
                    enableWebSearch = true,
                    callback = object : AIChatService.StreamCallback {
                        override fun onPartialResponse(content: String) {
                            // 部分响应，可以实时更新
                        }
                        
                        override fun onCompleteResponse(content: String) {
                            runOnUiThread {
                                // 移除"正在推荐"消息
                                if (messages.isNotEmpty() && messages.last().content == "🎬 正在为您推荐电影...") {
                                    messages.removeAt(messages.lastIndex)
                                }
                                
                                val aiResponse = ChatMessage(content, false)
                                messages.add(aiResponse)
                                adapter.notifyDataSetChanged()
                                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                            }
                        }
                        
                        override fun onError(error: String) {
                            runOnUiThread {
                                // 移除"正在推荐"消息
                                if (messages.isNotEmpty() && messages.last().content == "🎬 正在为您推荐电影...") {
                                    messages.removeAt(messages.lastIndex)
                                }
                                
                                val errorMessage = ChatMessage("抱歉，推荐服务暂时不可用：$error", false)
                                messages.add(errorMessage)
                                adapter.notifyDataSetChanged()
                                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                            }
                        }
                    }
                )
            } catch (e: Exception) {
                runOnUiThread {
                    // 移除"正在推荐"消息
                    if (messages.isNotEmpty() && messages.last().content == "🎬 正在为您推荐电影...") {
                        messages.removeAt(messages.lastIndex)
                    }
                    
                    val errorMessage = ChatMessage("抱歉，推荐服务暂时不可用，请稍后再试", false)
                    messages.add(errorMessage)
                    adapter.notifyDataSetChanged()
                    binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                }
            }
        }
    }
    
    /**
     * 解析时间并设置提醒
     */
    private fun parseAndSetReminder(text: String) {
        try {
            Log.d("AIActivity", "开始解析时间: $text")
            val calendar = Calendar.getInstance()
            val now = Calendar.getInstance()
            
            // 解析时间表达式 - 支持多种格式，避免匹配日期
            val timePatterns = listOf(
                Pattern.compile("(\\d{1,2})点(\\d{1,2})分"), // 6点30分
                Pattern.compile("(\\d{1,2})点(\\d{1,2})"), // 6点30
                Pattern.compile("(\\d{1,2}):(\\d{0,2})"), // 8:30
                Pattern.compile("(\\d{1,2})时(\\d{0,2})"), // 8时30
                Pattern.compile("(\\d{1,2})点"), // 8点
                Pattern.compile("(\\d{1,2})时"), // 8时
                // 更精确的匹配，避免匹配日期
                Pattern.compile("(\\d{1,2})点(?!月)"), // 8点（后面不是"月"）
                Pattern.compile("(\\d{1,2})时(?!月)"), // 8时（后面不是"月"）
                Pattern.compile("(\\d{1,2})点(\\d{0,2})(?!月)"), // 8点30（后面不是"月"）
                Pattern.compile("(\\d{1,2})时(\\d{0,2})(?!月)") // 8时30（后面不是"月"）
            )
            
            var hour = -1
            var minute = 0
            
            // 尝试匹配各种时间格式
            for (pattern in timePatterns) {
                val matcher = pattern.matcher(text)
                if (matcher.find()) {
                    hour = matcher.group(1)?.toIntOrNull() ?: -1
                    // 安全地获取第二个捕获组，如果不存在则默认为0
                    minute = try {
                        matcher.group(2)?.toIntOrNull() ?: 0
                    } catch (e: IndexOutOfBoundsException) {
                        0
                    }
                    Log.d("AIActivity", "匹配到时间: 小时=$hour, 分钟=$minute")
                    if (hour != -1) break
                }
            }
            
            // 处理中文时间表达
            if (hour == -1) {
                hour = parseChineseTime(text)
            }
            
            // 处理下午时间
            if (text.contains("下午") || text.contains("下午")) {
                if (hour in 1..11) {
                    hour += 12
                    Log.d("AIActivity", "下午时间转换: $hour 点")
                }
            }
            
            // 处理"半"字，表示30分钟
            if (text.contains("半")) {
                minute = 30
                Log.d("AIActivity", "半字解析: 分钟=30")
            }
            
            if (hour == -1) {
                Log.d("AIActivity", "无法解析时间，打开提醒设置页面")
                // 无法解析时间，打开提醒设置页面
                openReminderPage()
                return
            }
            
            Log.d("AIActivity", "解析到时间: 小时=$hour, 分钟=$minute")
            
            // 处理日期
            if (text.contains("明天")) {
                calendar.add(Calendar.DAY_OF_MONTH, 1)
            } else if (text.contains("下周")) {
                calendar.add(Calendar.WEEK_OF_YEAR, 1)
            } else if (text.contains("后天")) {
                calendar.add(Calendar.DAY_OF_MONTH, 2)
            } else if (text.contains("大后天")) {
                calendar.add(Calendar.DAY_OF_MONTH, 3)
            } else {
                // 处理具体日期，如"10月15日"
                val datePattern = Pattern.compile("(\\d{1,2})月(\\d{1,2})日")
                val dateMatcher = datePattern.matcher(text)
                if (dateMatcher.find()) {
                    val month = dateMatcher.group(1)?.toIntOrNull() ?: -1
                    val day = dateMatcher.group(2)?.toIntOrNull() ?: -1
                    if (month != -1 && day != -1) {
                        val currentYear = calendar.get(Calendar.YEAR)
                        calendar.set(Calendar.YEAR, currentYear)
                        calendar.set(Calendar.MONTH, month - 1) // Calendar月份从0开始
                        calendar.set(Calendar.DAY_OF_MONTH, day)
                        Log.d("AIActivity", "解析到具体日期: ${month}月${day}日")
                    }
                }
            }
            
            calendar.set(Calendar.HOUR_OF_DAY, hour)
            calendar.set(Calendar.MINUTE, minute)
            calendar.set(Calendar.SECOND, 0)
            calendar.set(Calendar.MILLISECOND, 0)
            
            // 检查时间是否已过
            if (calendar.timeInMillis <= now.timeInMillis) {
                val errorMessage = ChatMessage("⏰ 设置的时间已过，请重新设置", false)
                messages.add(errorMessage)
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
                return
            }
            
            // 创建提醒
            val reminder = Reminder(
                year = calendar.get(Calendar.YEAR),
                month = calendar.get(Calendar.MONTH),
                dayOfMonth = calendar.get(Calendar.DAY_OF_MONTH),
                hourOfDay = calendar.get(Calendar.HOUR_OF_DAY),
                minute = calendar.get(Calendar.MINUTE)
            ).apply {
                id = UUID.randomUUID().hashCode()
            }
            
            // 设置闹钟并保存到SharedPreferences
            scheduleReminder(reminder)
            saveReminderToPrefs(reminder)
            
            // 使用calendar对象中的正确时间，而不是原始解析的hour和minute
            val finalHour = calendar.get(Calendar.HOUR_OF_DAY)
            val finalMinute = calendar.get(Calendar.MINUTE)
            val timeStr = String.format("%02d:%02d", finalHour, finalMinute)
            val dateStr = when {
                text.contains("明天") -> "明天"
                text.contains("下周") -> "下周"
                text.contains("后天") -> "后天"
                text.contains("大后天") -> "大后天"
                text.contains("月") && text.contains("日") -> {
                    val month = calendar.get(Calendar.MONTH) + 1
                    val day = calendar.get(Calendar.DAY_OF_MONTH)
                    "${month}月${day}日"
                }
                else -> "今天"
            }
            val confirmMessage = ChatMessage("⏰ 已为您设置${dateStr}${timeStr}的提醒", false)
            messages.add(confirmMessage)
            adapter.notifyItemInserted(messages.lastIndex)
            binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            
        } catch (e: Exception) {
            Log.e("AIActivity", "解析提醒时间失败", e)
            val errorMessage = ChatMessage("⏰ 时间解析失败，已为您打开提醒设置页面", false)
            messages.add(errorMessage)
            adapter.notifyItemInserted(messages.lastIndex)
            binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            openReminderPage()
        }
    }
    
    /**
     * 解析中文时间表达
     */
    private fun parseChineseTime(text: String): Int {
        val timeMap = mapOf(
            "凌晨" to 0, "早上" to 6, "上午" to 9, "中午" to 12,
            "下午" to 14, "傍晚" to 18, "晚上" to 20, "深夜" to 23
        )
        
        for ((keyword, hour) in timeMap) {
            if (text.contains(keyword)) {
                return hour
            }
        }
        
        return -1
    }
    
    /**
     * 设置提醒闹钟
     */
    private fun scheduleReminder(reminder: Reminder) {
        val calendar = Calendar.getInstance().apply {
            set(Calendar.YEAR, reminder.year)
            set(Calendar.MONTH, reminder.month)
            set(Calendar.DAY_OF_MONTH, reminder.dayOfMonth)
            set(Calendar.HOUR_OF_DAY, reminder.hourOfDay)
            set(Calendar.MINUTE, reminder.minute)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        
        val alarmManager = getSystemService(ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, ReminderReceiver::class.java).apply {
            putExtra("reminderId", reminder.id)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            this,
            reminder.id,
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.timeInMillis, pendingIntent)
    }
    
    /**
     * 保存提醒到SharedPreferences
     */
    private fun saveReminderToPrefs(reminder: Reminder) {
        try {
            val prefs = getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
            val existingJson = prefs.getString("reminders", null)
            val type = object : com.google.gson.reflect.TypeToken<List<Reminder>>() {}.type
            val existingList = if (existingJson != null) {
                com.google.gson.Gson().fromJson<MutableList<Reminder>>(existingJson, type) ?: mutableListOf()
            } else {
                mutableListOf<Reminder>()
            }
            
            // 添加到现有列表
            existingList.add(reminder)
            
            // 按时间排序
            existingList.sortWith { a: Reminder, b: Reminder ->
                val aTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, a.year)
                    set(Calendar.MONTH, a.month)
                    set(Calendar.DAY_OF_MONTH, a.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, a.hourOfDay)
                    set(Calendar.MINUTE, a.minute)
                }.timeInMillis
                val bTime = Calendar.getInstance().apply {
                    set(Calendar.YEAR, b.year)
                    set(Calendar.MONTH, b.month)
                    set(Calendar.DAY_OF_MONTH, b.dayOfMonth)
                    set(Calendar.HOUR_OF_DAY, b.hourOfDay)
                    set(Calendar.MINUTE, b.minute)
                }.timeInMillis
                aTime.compareTo(bTime)
            }
            
            // 保存到SharedPreferences
            val json = com.google.gson.Gson().toJson(existingList)
            prefs.edit().putString("reminders", json).apply()
            
            Log.d("AIActivity", "提醒已保存到SharedPreferences: ${reminder.hourOfDay}:${reminder.minute}")
        } catch (e: Exception) {
            Log.e("AIActivity", "保存提醒失败", e)
        }
    }
    
    /**
     * 处理导航命令
     */
    private fun handleNavigationCommand(text: String) {
        Log.d("AIActivity", "处理导航命令: $text")
        
        // 提取地点名称 - 支持多种表达方式
        val placePatterns = listOf(
            Pattern.compile("想去(.+?)(?:$|，|。|！|！|\\s)"), // 想去故宫
            Pattern.compile("导航到(.+?)(?:$|，|。|！|！|\\s)"), // 导航到故宫
            Pattern.compile("怎么去(.+?)(?:$|，|。|！|！|\\s)"), // 怎么去故宫
            Pattern.compile("去(.+?)(?:$|，|。|！|！|\\s)"), // 去故宫
            Pattern.compile("我要去(.+?)(?:$|，|。|！|！|\\s)"), // 我要去南京
            Pattern.compile("到(.+?)(?:$|，|。|！|！|\\s)"), // 到南京
            Pattern.compile("(.+?)怎么去"), // 故宫怎么去
            Pattern.compile("(.+?)路线"), // 故宫路线
            Pattern.compile("(.+?)导航"), // 故宫导航
            Pattern.compile("(.+?)怎么走"), // 故宫怎么走
            Pattern.compile("(.+?)位置"), // 故宫位置
            Pattern.compile("(.+?)地址") // 故宫地址
        )
        
        var placeName = ""
        for (pattern in placePatterns) {
            val matcher = pattern.matcher(text)
            if (matcher.find()) {
                placeName = matcher.group(1)?.trim() ?: ""
                if (placeName.isNotEmpty()) {
                    Log.d("AIActivity", "提取到地点: $placeName")
                    break
                }
            }
        }
        
        Log.d("AIActivity", "最终地点名称: '$placeName'")
        
        if (placeName.isEmpty()) {
            // 如果没有提取到地点，显示地图选择器让用户输入
            Log.d("AIActivity", "未提取到地点，显示输入对话框")
            showMapChooserDialog()
        } else {
            // 直接显示地图选择器
            Log.d("AIActivity", "开始显示地图选择器，地点: $placeName")
            showMapChooser(this, placeName)
        }
        
        val confirmMessage = ChatMessage("🗺️ 正在为您打开地图导航", false)
        messages.add(confirmMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
    }
    
    /**
     * 显示地图选择对话框
     */
    private fun showMapChooserDialog() {
        val input = android.widget.EditText(this)
        input.hint = "请输入目的地"
        
        AlertDialog.Builder(this)
            .setTitle("选择导航应用")
            .setMessage("请先输入目的地")
            .setView(input)
            .setPositiveButton("确定") { _, _ ->
                val placeName = input.text.toString().trim()
                if (placeName.isNotEmpty()) {
                    showMapChooser(this, placeName)
                } else {
                    Toast.makeText(this, "请输入目的地", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    /**
     * 显示地图选择器（复用CityActivity的逻辑）
     */
    private fun showMapChooser(context: Context, placeName: String) {
        Log.d("AIActivity", "开始检测地图应用，地点: $placeName")
        val options = mutableListOf<Pair<String, Intent>>()
        
        // 百度地图
        val baiduIntent = getBaiduMapIntent(context, placeName)
        if (baiduIntent != null) {
            options.add("百度地图" to baiduIntent)
            Log.d("AIActivity", "百度地图可用")
        } else {
            Log.d("AIActivity", "百度地图不可用")
        }
        
        // 高德地图
        val amapIntent = getAmapIntent(context, placeName)
        if (amapIntent != null) {
            options.add("高德地图" to amapIntent)
            Log.d("AIActivity", "高德地图可用")
        } else {
            Log.d("AIActivity", "高德地图不可用")
        }
        
        // Google地图
        val googleIntent = getGoogleMapIntent(context, placeName)
        if (googleIntent != null) {
            options.add("Google 地图" to googleIntent)
            Log.d("AIActivity", "Google地图可用")
        } else {
            Log.d("AIActivity", "Google地图不可用")
        }
        
        // Google Earth
        val earthIntent = getGoogleEarthIntent(context, placeName)
        if (earthIntent != null) {
            options.add("Google Earth" to earthIntent)
            Log.d("AIActivity", "Google Earth可用")
        } else {
            Log.d("AIActivity", "Google Earth不可用")
        }
        
        Log.d("AIActivity", "检测到 ${options.size} 个可用地图应用")
        
        if (options.isEmpty()) {
            Log.d("AIActivity", "未检测到任何地图应用")
            Toast.makeText(context, "未检测到已安装的地图应用", Toast.LENGTH_SHORT).show()
            
            // 显示错误消息
            val errorMessage = ChatMessage("❌ 未检测到已安装的地图应用，请先安装百度地图、高德地图或Google地图", false)
            messages.add(errorMessage)
            adapter.notifyItemInserted(messages.lastIndex)
            binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            return
        }
        
        // 如果只有一个应用，直接启动
        if (options.size == 1) {
            Log.d("AIActivity", "只有一个地图应用，直接启动: ${options[0].first}")
            try {
                context.startActivity(options[0].second)
                Log.d("AIActivity", "成功启动地图应用")
            } catch (e: Exception) {
                Log.e("AIActivity", "启动地图应用失败", e)
                Toast.makeText(context, "无法启动${options[0].first}", Toast.LENGTH_SHORT).show()
            }
            return
        }
        
        // 显示选择对话框 - 使用CityActivity的美观样式
        Log.d("AIActivity", "显示地图应用选择对话框")
        showCustomMapChooser(context, options)
    }
    
    /**
     * 显示自定义地图选择器（复用CityActivity的美观样式）
     */
    private fun showCustomMapChooser(context: Context, options: List<Pair<String, Intent>>) {
        val dialog = AlertDialog.Builder(context)
            .setView(R.layout.dialog_map_chooser)
            .create()
        
        dialog.show()
        
        // 获取容器并添加地图选项
        val container = dialog.findViewById<LinearLayout>(R.id.mapOptionsContainer)
        options.forEach { (name, intent) ->
            val optionView = createMapOptionView(context, name, intent, dialog)
            container?.addView(optionView)
        }
        
        // 美化对话框样式
        dialog.window?.let { window ->
            window.setBackgroundDrawable(context.getDrawable(R.drawable.card_background))
            window.attributes?.let { attributes ->
                attributes.dimAmount = 0.3f
                window.attributes = attributes
            }
        }
    }
    
    /**
     * 创建地图选项视图（复用CityActivity的样式）
     */
    private fun createMapOptionView(context: Context, name: String, intent: Intent, dialog: AlertDialog): View {
        val view = LayoutInflater.from(context).inflate(R.layout.item_map_option, null)
        
        val iconView = view.findViewById<ImageView>(R.id.mapIcon)
        val nameView = view.findViewById<TextView>(R.id.mapName)
        val descView = view.findViewById<TextView>(R.id.mapDescription)
        
        // 设置图标和文本
        when (name) {
            "百度地图" -> {
                iconView.setImageResource(R.drawable.ic_baidu_map)
                descView.text = "使用百度地图查看位置"
            }
            "高德地图" -> {
                iconView.setImageResource(R.drawable.ic_amap)
                descView.text = "使用高德地图查看位置"
            }
            "Google 地图" -> {
                iconView.setImageResource(R.drawable.ic_google_maps)
                descView.text = "使用Google地图查看位置"
            }
            "Google Earth" -> {
                iconView.setImageResource(R.drawable.ic_google_earth)
                descView.text = "使用Google Earth查看位置"
            }
        }
        
        nameView.text = name
        
        // 设置点击事件
        view.setOnClickListener {
            dialog.dismiss()
            try {
                Log.d("AIActivity", "用户选择: $name")
                context.startActivity(intent)
                Log.d("AIActivity", "成功启动选择的地图应用")
            } catch (e: Exception) {
                Log.e("AIActivity", "启动选择的地图应用失败", e)
                Toast.makeText(context, "无法启动$name", Toast.LENGTH_SHORT).show()
            }
        }
        
        return view
    }
    
    // 以下是从CityActivity复用的地图相关方法
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
    
    private fun getBaiduMapIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.baidu.BaiduMap")) return null
        val uri = Uri.parse(
            "baidumap://map/place/search?query=${Uri.encode(query)}&src=${Uri.encode(context.packageName)}"
        )
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.baidu.BaiduMap")
        return if (isIntentAvailable(context, intent)) intent else null
    }
    
    private fun getAmapIntent(context: Context, query: String): Intent? {
        val pkg = "com.autonavi.minimap"
        if (!isPackageInstalled(context, pkg)) return null
        
        val uriCandidates = listOf(
            "androidamap://keyword?keywords=${Uri.encode(query)}",
            "androidamap://keyword?sourceApplication=${Uri.encode(context.packageName)}&keywords=${Uri.encode(query)}&dev=0",
            "amapuri://keyword?sourceApplication=${Uri.encode(context.packageName)}&keyword=${Uri.encode(query)}",
            "amapuri://keyword?sourceApplication=${Uri.encode(context.packageName)}&keywords=${Uri.encode(query)}",
            "amapuri://keyword?keywords=${Uri.encode(query)}"
        )
        
        for (uriString in uriCandidates) {
            val uri = Uri.parse(uriString)
            val intent = Intent(Intent.ACTION_VIEW, uri).apply {
                setPackage(pkg)
            }
            if (isIntentAvailable(context, intent)) {
                return intent
            }
        }
        
        // fallback: Web URL
        val webUrl = "https://uri.amap.com/search?query=${Uri.encode(query)}"
        val webIntent = Intent(Intent.ACTION_VIEW, Uri.parse(webUrl))
        return if (isIntentAvailable(context, webIntent)) webIntent else null
    }
    
    private fun getGoogleMapIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.google.android.apps.maps")) return null
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.android.apps.maps")
        return if (isIntentAvailable(context, intent)) intent else null
    }
    
    private fun getGoogleEarthIntent(context: Context, query: String): Intent? {
        if (!isPackageInstalled(context, "com.google.earth")) return null
        val uri = Uri.parse("geo:0,0?q=${Uri.encode(query)}")
        val intent = Intent(Intent.ACTION_VIEW, uri)
        intent.setPackage("com.google.earth")
        return if (isIntentAvailable(context, intent)) intent else null
    }
}
