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
import com.example.filmguide.databinding.ActivityAiactivityBinding
import com.example.filmguide.logic.model.ChatMessage
import com.example.filmguide.ui.ChatAdapter
import java.util.*

class AIActivity : AppCompatActivity() {
    private lateinit var binding: ActivityAiactivityBinding
    private lateinit var adapter: ChatAdapter
    private val messages = mutableListOf<ChatMessage>()

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
        private const val REQ_SPEECH = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        adapter = ChatAdapter(messages)
        binding.recyclerViewMessages.layoutManager = LinearLayoutManager(this)
        binding.recyclerViewMessages.adapter = adapter

        binding.buttonSend.setOnClickListener {
            sendMessage()
        }

        // 点击“语音”图标时，先检查权限再启动语音识别
        binding.voice.setOnClickListener {
            ensureAudioPermission()
        }
    }//onCreate end

    // 检查录音权限
    private fun ensureAudioPermission() {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.RECORD_AUDIO)
            != PackageManager.PERMISSION_GRANTED
        ) {
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
        if (requestCode == REQUEST_RECORD_AUDIO &&
            grantResults.isNotEmpty() &&
            grantResults[0] == PackageManager.PERMISSION_GRANTED
        ) {
            startSpeechInput()
        } else {
            Toast.makeText(this, "需要录音权限才能识别语音", Toast.LENGTH_SHORT).show()
        }
    }

    // 启动系统语音识别
    private fun startSpeechInput() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(
                RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
            )
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
            putExtra(RecognizerIntent.EXTRA_PROMPT, "请说话…")
        }
        try {
            startActivityForResult(intent, REQ_SPEECH)
        } catch (e: ActivityNotFoundException) {
            Toast.makeText(this, "设备不支持语音识别", Toast.LENGTH_SHORT).show()
        }
    }

    // 接收语音识别结果，填入 editText
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        if (requestCode == REQ_SPEECH && resultCode == Activity.RESULT_OK) {
            val results = data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
            val text = results?.firstOrNull().orEmpty()
            // 直接填充到输入框
            binding.editTextMessage.setText(text)
        }
    }

    private fun sendMessage() {
        val text = binding.editTextMessage.text.toString().trim()
        if (text.isNotEmpty()) {
            messages.add(ChatMessage(text, true))
            adapter.notifyItemInserted(messages.lastIndex)
            binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            binding.editTextMessage.text?.clear()

            // 模拟回复
            Handler(Looper.getMainLooper()).postDelayed({
                messages.add(ChatMessage("$text", false))
                adapter.notifyItemInserted(messages.lastIndex)
                binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
            }, 1200)
        }
    }
}
