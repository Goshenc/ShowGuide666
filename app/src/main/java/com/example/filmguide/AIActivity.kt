package com.example.filmguide

import android.Manifest
import android.app.Activity
import android.content.ActivityNotFoundException
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognizerIntent
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.filmguide.ai.AIChatService
import com.example.filmguide.databinding.ActivityAiactivityBinding
import com.example.filmguide.logic.model.ChatMessage
import com.example.filmguide.ui.ChatAdapter
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import java.util.*

class AIActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiactivityBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()
    private lateinit var aiChatService: AIChatService
    private val conversationHistory = mutableListOf<AIChatService.ChatMessage>()
    private var isAiResponding = false

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
        private const val REQ_SPEECH = 101
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
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isEmpty() || isAiResponding) return

        // 添加用户消息
        val userMessage = ChatMessage(text, true)
        messages.add(userMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        binding.editTextMessage.text?.clear()

        // 添加到对话历史
        conversationHistory.add(AIChatService.ChatMessage("user", text))

        // 显示AI正在思考的提示
        val thinkingMessage = ChatMessage("AI正在思考中...", false)
        messages.add(thinkingMessage)
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)

        // 调用AI服务
        isAiResponding = true
        CoroutineScope(Dispatchers.Main).launch {
            try {
                aiChatService.sendMessage(
                    userMessage = text,
                    conversationHistory = conversationHistory,
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
        if (requestCode == REQUEST_RECORD_AUDIO && grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
            startSpeechInput()
        } else {
            Toast.makeText(this, "需要录音权限才能识别语音", Toast.LENGTH_SHORT).show()
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
}
