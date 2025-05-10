package com.example.filmguide

import android.app.AlarmManager
import android.app.PendingIntent
import android.app.TimePickerDialog
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.os.PowerManager
import android.provider.Settings
import android.view.ViewGroup
import android.widget.ImageView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.filmguide.ReminderReceiver
import com.example.filmguide.databinding.ActivityReminderBinding
import com.example.filmguide.logic.model.Reminder
import com.example.filmguide.ui.ReminderAdapter
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import java.util.Calendar
import java.util.UUID

class ReminderActivity : AppCompatActivity() {
    private lateinit var binding: ActivityReminderBinding
    private lateinit var rv: RecyclerView
    private lateinit var adapter: ReminderAdapter
    private val list = mutableListOf<Reminder>()
    private val prefs by lazy { getSharedPreferences("ReminderPrefs", MODE_PRIVATE) }
    private val permPrefs by lazy { getSharedPreferences("overlay_prefs", MODE_PRIVATE) }
    private val batteryPrefs by lazy { getSharedPreferences("battery_prefs", MODE_PRIVATE) }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityReminderBinding.inflate(layoutInflater)
        setContentView(binding.root)
        enableEdgeToEdge()
        ViewCompat.setOnApplyWindowInsetsListener(binding.root) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 申请通知权限(Android13+)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU &&
            ContextCompat.checkSelfPermission(this, android.Manifest.permission.POST_NOTIFICATIONS)
            != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(arrayOf(android.Manifest.permission.POST_NOTIFICATIONS), 1001)
        }

        // 首次进入依次询问：悬浮窗权限 -> 省电优化
        val askedOverlay = permPrefs.getBoolean("asked_overlay", false)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && !Settings.canDrawOverlays(this) && !askedOverlay) {
            // 弹出悬浮窗对话框，回调中继续询问省电优化
            showOverlayPermissionDialog()
            permPrefs.edit().putBoolean("asked_overlay", true).apply()
        } else {
            // 如果已经授权或已询问过悬浮窗，则直接询问省电优化
            askIgnoreBatteryOptimizationsOnce()
        }

        rv = findViewById(R.id.recyclerView)
        adapter = ReminderAdapter(list, ::deleteReminder)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        findViewById<ImageView>(R.id.addButton).setOnClickListener { openTimePicker() }
        loadReminders()
    }

    private fun openTimePicker() {
        val c = Calendar.getInstance()
        TimePickerDialog(
            this,
            { _, h, m ->
                if (list.any { it.hourOfDay == h && it.minute == m }) return@TimePickerDialog
                val r = Reminder(h, m).apply { id = UUID.randomUUID().hashCode() }
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
            val type = object : TypeToken<List<Reminder>>() {}.type
            list.clear()
            list.addAll(Gson().fromJson(it, type))
        }
        adapter.notifyDataSetChanged()
    }

    private fun deleteReminder(r: Reminder) {
        list.remove(r)
        saveList()
        val am = getSystemService(ALARM_SERVICE) as AlarmManager
        val i = Intent(this, ReminderReceiver::class.java).apply { putExtra("reminderId", r.id) }
        // 加上 FLAG_UPDATE_CURRENT，合并已有 PendingIntent
        val pi = PendingIntent.getBroadcast(
            this,
            r.id,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
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
        // 使用 FLAG_UPDATE_CURRENT，确保更新意图
        val pi = PendingIntent.getBroadcast(
            this,
            r.id,
            i,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        am.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, cal.timeInMillis, pi)
    }

    /** 询问悬浮窗（后台弹出）权限 */
    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(this)
            .setTitle("允许后台弹出界面")
            .setMessage("为了在锁屏或后台也能弹出提醒，请允许应用在其他应用上层显示。")
            .setPositiveButton("去授权") { _, _ ->
                openOverlaySettings()
                // 授权页面返回后，再询问省电优化
                binding.root.post { askIgnoreBatteryOptimizationsOnce() }
            }
            .setNegativeButton("取消") { _, _ ->
                askIgnoreBatteryOptimizationsOnce()
            }
            .show()
    }

    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            Uri.parse("package:$packageName")
        )
        startActivity(intent)
    }

    private fun askIgnoreBatteryOptimizationsOnce() {
        val askedBattery = batteryPrefs.getBoolean("asked_battery", false)
        if (!askedBattery) {
            batteryPrefs.edit().putBoolean("asked_battery", true).apply()
            askIgnoreBatteryOptimizations()
        }
    }

    private fun askIgnoreBatteryOptimizations() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            val pm = getSystemService(POWER_SERVICE) as PowerManager
            if (!pm.isIgnoringBatteryOptimizations(packageName)) {
                AlertDialog.Builder(this)
                    .setTitle("禁用省电优化")
                    .setMessage("为了保证闹钟能在后台准时触发，请允许应用忽略电池优化。")
                    .setPositiveButton("去设置") { _, _ ->
                        val intent = Intent(Settings.ACTION_IGNORE_BATTERY_OPTIMIZATION_SETTINGS)
                        startActivity(intent)
                    }
                    .setNegativeButton("取消", null)
                    .show()
            }
        }
    }
}