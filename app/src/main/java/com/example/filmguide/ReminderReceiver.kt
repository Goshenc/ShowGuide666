package com.example.filmguide

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import androidx.core.app.NotificationCompat
import com.example.filmguide.R
import com.example.filmguide.logic.model.Reminder
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderReceiver : BroadcastReceiver() {
    override fun onReceive(context: Context, intent: Intent) {
        val id = intent.getIntExtra("reminderId", -1)
        if (id < 0) return

        // —— 1. 发通知 ——
        val nm = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            val chan = NotificationChannel(
                "default", "提醒通知", NotificationManager.IMPORTANCE_HIGH
            ).apply { description = "闹钟时间到" }
            nm.createNotificationChannel(chan)
        }
        val notif = NotificationCompat.Builder(context, "default")
            .setSmallIcon(R.drawable.clock)      // 替换为你自己的图标
            .setContentTitle("提醒")
            .setContentText("您设置的闹钟已到时间！")
            .setAutoCancel(true)
            .build()
        nm.notify(id, notif)

        // —— 2. 从 SharedPreferences 删除这条提醒 ——
        val prefs: SharedPreferences =
            context.getSharedPreferences("ReminderPrefs", Context.MODE_PRIVATE)
        val json = prefs.getString("reminders", null)
        if (json != null) {
            val type = object : TypeToken<MutableList<Reminder>>() {}.type
            val list: MutableList<Reminder> = Gson().fromJson(json, type)
            if (list.removeAll { it.id == id }) {
                prefs.edit().putString("reminders", Gson().toJson(list)).apply()
            }
        }

        // —— 3. 取消 AlarmManager 的 PendingIntent ——
        val am = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val cancelIntent = Intent(context, ReminderReceiver::class.java)
            .apply { putExtra("reminderId", id) }
        val pi = PendingIntent.getBroadcast(
            context, id, cancelIntent, PendingIntent.FLAG_IMMUTABLE
        )
        am.cancel(pi)
    }
}
