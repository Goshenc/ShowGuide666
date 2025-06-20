package com.example.filmguide

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.speech.RecognitionListener
import android.speech.RecognizerIntent
import android.speech.SpeechRecognizer
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.bumptech.glide.Glide

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [RecordDialogFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class RecordDialogFragment(
    private val onResult: (String) -> Unit
) : DialogFragment(), RecognitionListener {

    private lateinit var recognizer: SpeechRecognizer
    private lateinit var ivMic: ImageView
    private lateinit var volumeBar: ProgressBar
    private lateinit var tvHint: TextView
    private lateinit var animation:ImageView

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val view = LayoutInflater.from(context).inflate(R.layout.dialog_record, null)
        ivMic = view.findViewById(R.id.ivMic)
        volumeBar = view.findViewById(R.id.volumeBar)
        tvHint = view.findViewById(R.id.tvHint)
        animation=view.findViewById(R.id.animation)
        recognizer = SpeechRecognizer.createSpeechRecognizer(requireContext()).apply {
            setRecognitionListener(this@RecordDialogFragment)
        }




        val density = requireContext().resources.displayMetrics.density
        val newWidthPx = (150 * density).toInt()
        val newHeightPx = (150 * density).toInt()

        val lp = animation.layoutParams
        lp.width = newWidthPx
        lp.height = newHeightPx
        animation.layoutParams = lp
        Glide.with(this)
            .asGif()
            .load(R.drawable.voiceanimation)
            .into(animation)


        val dialog = AlertDialog.Builder(requireContext())
            .setView(view)
            .setCancelable(false)
            .create()

        dialog.setOnShowListener {
            startListening()
        }
        return dialog
    }

    private fun startListening() {
        val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
            putExtra(RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                RecognizerIntent.LANGUAGE_MODEL_FREE_FORM)
            putExtra(RecognizerIntent.EXTRA_LANGUAGE, "zh-CN")
            putExtra(RecognizerIntent.EXTRA_PARTIAL_RESULTS, true)
        }
        recognizer.startListening(intent)
    }

    override fun onReadyForSpeech(params: Bundle?) {
        tvHint.text = "请开始说话"
    }

    override fun onBeginningOfSpeech() {
        ivMic.setImageResource(R.drawable.icon1round) // 切换高亮图标
    }


    override fun onRmsChanged(rmsdB: Float) {
        // rmsdB 大致在 0–10 之间，映射到 0–100
        val level = ((rmsdB.coerceIn(0f, 10f) / 10f) * 100).toInt()
        volumeBar.progress = level
    }

    override fun onEndOfSpeech() {
        tvHint.text = "识别中…"
    }

    override fun onError(error: Int) {
        tvHint.text = "识别失败，请重试"
        Handler(Looper.getMainLooper()).postDelayed({ dismiss() }, 1000)
    }

    override fun onResults(results: Bundle?) {
        val list = results?.getStringArrayList(SpeechRecognizer.RESULTS_RECOGNITION)
        val text = list?.firstOrNull().orEmpty()
        onResult(text)
        dismiss()
    }

    override fun onPartialResults(partialResults: Bundle?) {
        // 可选：显示中间识别结果
    }

    override fun onBufferReceived(buffer: ByteArray?) {}
    override fun onEvent(eventType: Int, params: Bundle?) {}
    override fun onDestroy() {
        super.onDestroy()
        recognizer.destroy()
    }
    override fun onStart() {
        super.onStart()
        dialog?.window?.let { window ->
            val metrics = requireContext().resources.displayMetrics
            // 将 dp 转 px
            val widthPx = (350 * metrics.density).toInt()
            val heightPx = (500 * metrics.density).toInt()
            window.setLayout(widthPx, heightPx)
            // 如果想去掉默认的外层 padding，可：
            window.setBackgroundDrawableResource(android.R.color.transparent)
        }
    }

}
