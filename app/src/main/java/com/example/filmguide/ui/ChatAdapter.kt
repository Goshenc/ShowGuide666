package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
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
        val message = messages[position]
        if (holder is SentViewHolder) holder.bind(message)
        else if (holder is ReceivedViewHolder) holder.bind(message)
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
        }
    }
}