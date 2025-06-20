package com.example.filmguide.ui

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.Demonstration2Activity
import com.example.filmguide.DemonstrationActivity
import com.example.filmguide.ManageActivity
import com.example.filmguide.R
import com.example.filmguide.databinding.ItemMessageReceivedBinding
import com.example.filmguide.databinding.ItemMessageSentBinding
import com.example.filmguide.logic.model.ChatMessage

class ChatAdapter(
    private val messages: MutableList<ChatMessage>
) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {

    companion object {
        private const val TYPE_SENT = 0
        private const val TYPE_RECEIVED = 1
    }

    override fun getItemViewType(position: Int): Int {
        return if (messages[position].isSentByUser) TYPE_SENT else TYPE_RECEIVED
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        return if (viewType == TYPE_SENT) {
            SentViewHolder(
                ItemMessageSentBinding.inflate(inflater, parent, false)
            )
        } else {
            ReceivedViewHolder(
                ItemMessageReceivedBinding.inflate(inflater, parent, false)
            )
        }
    }

    override fun getItemCount(): Int = messages.size

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        val msg = messages[position]
        if (holder is SentViewHolder) {
            holder.bind(msg)
        } else if (holder is ReceivedViewHolder) {
            holder.bind(msg)
        }
    }

    inner class SentViewHolder(private val binding: ItemMessageSentBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.textMessage.text = msg.content
        }
    }

    inner class ReceivedViewHolder(private val binding: ItemMessageReceivedBinding)
        : RecyclerView.ViewHolder(binding.root) {
        fun bind(msg: ChatMessage) {
            binding.textMessage.text = msg.content
            msg.imageResId?.let { resId ->
                binding.aiImage.visibility = View.VISIBLE
                binding.aiImage.setImageResource(resId)
                binding.aiImage.setOnClickListener { view ->
                    val ctx = view.context
                    val intent = when (resId) {
                        R.drawable.aichatimage -> Intent(ctx, DemonstrationActivity::class.java)
                        R.drawable.aichatimage2 -> Intent(ctx, Demonstration2Activity::class.java)
                        R.drawable.aichatimage3 -> Intent(ctx,ManageActivity::class.java)
                        else -> null
                    }
                    intent?.let { ctx.startActivity(it) }
                }
            } ?: run {
                binding.aiImage.visibility = View.GONE
                binding.aiImage.setOnClickListener(null)
            }
        }
    }

}
