// file: com/example/filmguide/ui/ReminderAdapter.kt
package com.example.filmguide.ui

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.R
import com.example.filmguide.logic.model.Reminder

class ReminderAdapter(
    private val data: List<Reminder>,
    private val onDelete: (Reminder) -> Unit
) : RecyclerView.Adapter<ReminderAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTime: TextView = itemView.findViewById(R.id.tvTime)
        val tvDate: TextView = itemView.findViewById(R.id.tvDate)
        val tvDelete: TextView = itemView.findViewById(R.id.tvDelete)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_reminder, parent, false)
        return ViewHolder(view)
    }

    override fun getItemCount(): Int = data.size

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val r = data[position]

        // month 存的是 0~11，要显示时 +1
        val displayMonth = r.month + 1

        // 先给 tvTime 设置 “HH:mm”
        val timeText = String.format("%02d:%02d", r.hourOfDay, r.minute)
        holder.tvTime.text = timeText

        // 再给 tvDate 设置 “yyyy-MM-dd”
        val dateText = String.format("%04d-%02d-%02d", r.year, displayMonth, r.dayOfMonth)
        holder.tvDate.text = dateText

        holder.tvDelete.setOnClickListener { onDelete(r) }
    }

}
