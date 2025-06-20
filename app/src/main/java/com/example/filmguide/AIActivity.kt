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

    // AI 回复集合
    private val aiReplies = arrayOf(
        ChatMessage("Hi，我是你的AI推荐官ShowGuide！无论你想看演唱会、话剧、音乐剧、电影，还是想找周末市集、艺术展览、亲子活动，" +
                "我都能根据你的喜好帮你推荐最合适的去处。只要告诉我你感兴趣的类型、预算和日期，我就能为你筛选最新最热门的玩乐资讯，" +
                "让你的休闲时光更精彩！快来和我聊聊你的需求吧~" +
                "", false),
        ChatMessage("以下是近期最火的电影推荐《酱园弄.悬案》，改部影片改编自民国四大奇案之一，讲述了一桩骇人听闻的杀父碎尸案引发的社会轰动。章子怡、" +
                "王传君、易烊千玺等强大阵容加盟，让这部剧情犯罪片备受期待！影片不仅探讨了案件背后的真相，还揭示了旧社会女性命运的挣扎。", false, imageResId = R.drawable.aichatimage),
        ChatMessage("哆啦A梦：大雄的绘画奇遇记 是一部充满奇幻色彩的动画电影，作为哆啦A梦系列的第45部作品，它延续了经典的冒险精神和温馨感。" +
                "影片讲述了大雄意外获得一幅价值连城的名画残片后，哆啦A梦与伙伴们进入画中世界展开冒险的故事。他们邂逅神秘少女可蕾雅，并一起踏上寻找传说中的宝石“雅托利亚蓝”的旅程，途中遭遇重重危机，甚至牵涉到“世界毁灭”的传说。\n" +
                "\n" +
                "这部电影不仅画面精美，情节也充满了想象力和情感张力，非常适合哆啦A梦的粉丝以及喜欢奇幻冒险题材的观众。淘票票评分高达9.6分，想看人数更是达到了9.2万，可见它的受欢迎程度✨。目前正值特惠购票阶段，最高还能减8.1元，非常值得一看哦～", false, imageResId = R.drawable.aichatimage2),
        ChatMessage("好的，您的票务信息已经帮您添加到票务管理中了，您可以前往票务管理界面查看详细信息和进行更多操作。",false, imageResId = R.drawable.aichatimage3),
    )
    private var replyStage = 0

    companion object {
        private const val REQUEST_RECORD_AUDIO = 100
        private const val REQ_SPEECH = 101
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityAiactivityBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()

        // 一进来显示欢迎语
        messages.add(aiReplies[0])

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
        if (text.isEmpty()) return

        // 添加用户消息
        messages.add(ChatMessage(text, true))
        adapter.notifyItemInserted(messages.lastIndex)
        binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        binding.editTextMessage.text?.clear()

        // 延迟 AI 回复
        Handler(Looper.getMainLooper()).postDelayed({
            val nextIndex = (replyStage + 1).coerceAtMost(aiReplies.lastIndex)
            val aiReply = aiReplies[nextIndex]
            replyStage = nextIndex
            messages.add(aiReply)
            adapter.notifyItemInserted(messages.lastIndex)
            binding.recyclerViewMessages.scrollToPosition(messages.lastIndex)
        }, 1200)
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
}
