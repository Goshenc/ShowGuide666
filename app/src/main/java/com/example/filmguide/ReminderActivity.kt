package com.example.filmguide

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.logic.model.Reminder
import com.example.filmguide.ui.ReminderAdapter
import java.util.Calendar
import java.util.UUID
import android.content.SharedPreferences
import com.example.filmguide.ReminderReceiver
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken

class ReminderActivity : AppCompatActivity() {
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val list = mutableListOf<Reminder>()
    private lateinit var prefs: SharedPreferences

    override fun onCreate(savedInstanceState: Bundle?) {


        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_reminder)

        // 申请通知权限(Android13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU
            && ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(
                this, arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001
            )
        }

        prefs = getSharedPreferences("ReminderPrefs", MODE_PRIVATE)
        rv = findViewById(R.id.recyclerView)
        adapter = ReminderAdapter(list, ::deleteReminder)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<Button>(R.id.addButton).setOnClickListener { openTimePicker() }
        loadReminders()
    }

    private fun openTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                if (list.any { it.hourOfDay == h && it.minute == m }) return@TimePickerDialog
                val r = Reminder(h, m)
                r.id = UUID.randomUUID().hashCode()
                list.add(r)
                saveList()
                schedule(r)
            },
            c.get(Calendar.HOUR_OF_DAY), c.get(Calendar.MINUTE), true
        ).show()
    }

    private fun saveList() {
        prefs.edit().putString("reminders", Gson().toJson(list)).apply()
        adapter.notifyDataSetChanged()
    }

    private fun loadReminders() {
        prefs.getString("reminders", null)?.let {
            val type = object: TypeToken<List<Reminder>>() {}.type
            list.clear()
            list.addAll(Gson().fromJson(it, type))
        }
        adapter.notifyDataSetChanged()
    }

    private fun deleteReminder(r: Reminder) {
        list.remove(r); saveList()
        // 同步取消闹钟
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val i = Intent(this, ReminderReceiver::class.java).apply { putExtra("reminderId", r.id) }
        val pi = PendingIntent.getBroadcast(this, r.id, i, PendingIntent.FLAG_IMMUTABLE)
        am.cancel(pi)
        adapter.notifyDataSetChanged()
    }

    private fun schedule(r: Reminder) {
        val cal = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, r.hourOfDay)
            set(Calendar.MINUTE, r.minute)
            set(Calendar.SECOND, 0)
            if (before(Calendar.getInstance())) add(Calendar.DAY_OF_MONTH, 1)
        }
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val i = Intent(this, ReminderReceiver::class.java).apply { putExtra("reminderId", r.id) }
        val pi = PendingIntent.getBroadcast(this, r.id, i, PendingIntent.FLAG_IMMUTABLE)
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }
}
