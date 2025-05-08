package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.R
import com.example.filmguide.logic.model.Reminder

class ReminderAdapter(private val reminderList: List<Reminder>, private val deleteReminder: (Reminder) -> Unit) : RecyclerView.Adapter<ReminderAdapter.ReminderViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ReminderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_reminder, parent, false)
        return ReminderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ReminderViewHolder, position: Int) {
        val reminder = reminderList[position]
        holder.timeText.text = reminder.toString()
        holder.deleteButton.setOnClickListener {
            deleteReminder(reminder)
        }
    }

    override fun getItemCount(): Int = reminderList.size

    class ReminderViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val timeText: TextView = itemView.findViewById(R.id.timeText)
        val deleteButton: Button = itemView.findViewById(R.id.deleteButton)
    }
}
