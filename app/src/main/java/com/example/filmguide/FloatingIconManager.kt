package com.example.filmguide

import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import android.provider.Settings
import androidx.appcompat.app.AlertDialog

class FloatingIconManager(private val context: Context) {
    
    private val prefs: SharedPreferences = context.getSharedPreferences("floating_icon_prefs", Context.MODE_PRIVATE)
    
    companion object {
        private const val KEY_IS_ENABLED = "floating_icon_enabled"
        private const val KEY_HAS_ASKED_PERMISSION = "has_asked_permission"
    }
    
    fun isFloatingIconEnabled(): Boolean {
        return prefs.getBoolean(KEY_IS_ENABLED, true) // 默认开启
    }
    
    fun setFloatingIconEnabled(enabled: Boolean) {
        prefs.edit().putBoolean(KEY_IS_ENABLED, enabled).apply()
    }
    
    fun hasOverlayPermission(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            Settings.canDrawOverlays(context)
        } else {
            true
        }
    }
    
    fun requestOverlayPermissionIfNeeded() {
        if (!hasOverlayPermission()) {
            val hasAsked = prefs.getBoolean(KEY_HAS_ASKED_PERMISSION, false)
            if (!hasAsked) {
                showOverlayPermissionDialog()
                prefs.edit().putBoolean(KEY_HAS_ASKED_PERMISSION, true).apply()
            }
        }
    }
    
    private fun showOverlayPermissionDialog() {
        AlertDialog.Builder(context)
            .setTitle("悬浮图标权限")
            .setMessage("为了显示全局悬浮图标，需要允许应用在其他应用上层显示。")
            .setPositiveButton("去设置") { _, _ ->
                openOverlaySettings()
            }
            .setNegativeButton("取消", null)
            .show()
    }
    
    private fun openOverlaySettings() {
        val intent = Intent(
            Settings.ACTION_MANAGE_OVERLAY_PERMISSION,
            android.net.Uri.parse("package:${context.packageName}")
        )
        context.startActivity(intent)
    }
    
    fun startFloatingIcon() {
        if (hasOverlayPermission()) {
            FloatingIconService.startService(context)
        } else {
            requestOverlayPermissionIfNeeded()
        }
    }
    
    fun stopFloatingIcon() {
        val intent = Intent(context, FloatingIconService::class.java)
        context.stopService(intent)
    }
    
    fun toggleFloatingIcon() {
        if (isFloatingIconEnabled()) {
            stopFloatingIcon()
            setFloatingIconEnabled(false)
        } else {
            startFloatingIcon()
            setFloatingIconEnabled(true)
        }
    }
    
    fun showFloatingIcon() {
        if (hasOverlayPermission()) {
            FloatingIconService.showFloatingIcon(context)
        }
    }
    
    fun hideFloatingIcon() {
        FloatingIconService.hideFloatingIcon(context)
    }
}
