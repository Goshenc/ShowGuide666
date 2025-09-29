package com.example.filmguide

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.ObjectAnimator
import android.app.Notification
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.app.Service
import android.content.Context
import android.content.Intent
import android.graphics.PixelFormat
import android.os.Build
import android.os.IBinder
import android.provider.Settings
import android.view.Gravity
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.WindowManager
import android.widget.ImageView
import androidx.core.app.NotificationCompat
import androidx.core.content.ContextCompat
import com.example.filmguide.databinding.FloatingIconLayoutBinding

class FloatingIconService : Service() {
    
    private var windowManager: WindowManager? = null
    private var floatingView: View? = null
    private var binding: FloatingIconLayoutBinding? = null
    private var isVisible = true
    private var isExpanded = false
    private var initialX = 0
    private var initialY = 0
    private var initialTouchX = 0f
    private var initialTouchY = 0f
    private var autoCollapseRunnable: Runnable? = null
    private val autoCollapseDelay = 3000L // 3秒后自动收起
    private var activityCheckRunnable: Runnable? = null
    private val activityCheckDelay = 1000L // 1秒检查一次当前Activity
    
    companion object {
        private const val ACTION_TOGGLE_VISIBILITY = "toggle_visibility"
        private const val ACTION_SHOW = "show"
        private const val ACTION_HIDE = "hide"
        
        fun startService(context: Context) {
            val intent = Intent(context, FloatingIconService::class.java)
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun toggleVisibility(context: Context) {
            val intent = Intent(context, FloatingIconService::class.java).apply {
                action = ACTION_TOGGLE_VISIBILITY
            }
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun showFloatingIcon(context: Context) {
            val intent = Intent(context, FloatingIconService::class.java).apply {
                action = ACTION_SHOW
            }
            ContextCompat.startForegroundService(context, intent)
        }
        
        fun hideFloatingIcon(context: Context) {
            val intent = Intent(context, FloatingIconService::class.java).apply {
                action = ACTION_HIDE
            }
            ContextCompat.startForegroundService(context, intent)
        }
    }
    
    override fun onCreate() {
        super.onCreate()
        windowManager = getSystemService(WINDOW_SERVICE) as WindowManager
        createNotificationChannel()
    }
    
    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        when (intent?.action) {
            ACTION_TOGGLE_VISIBILITY -> toggleFloatingIcon()
            ACTION_SHOW -> showFloatingIcon()
            ACTION_HIDE -> hideFloatingIcon()
            else -> {
                if (floatingView == null) {
                    showFloatingIcon()
                }
            }
        }
        
        // 检查当前Activity，如果在AI界面则隐藏悬浮按钮
        checkCurrentActivity()
        
        return START_STICKY
    }
    
    override fun onBind(intent: Intent?): IBinder? = null
    
    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                "floating_icon_channel",
                "悬浮图标服务",
                NotificationManager.IMPORTANCE_LOW
            ).apply {
                description = "悬浮图标后台服务"
                setShowBadge(false)
            }
            val notificationManager = getSystemService(NotificationManager::class.java)
            notificationManager.createNotificationChannel(channel)
        }
    }
    
    private fun startForegroundService() {
        val notificationIntent = Intent(this, HomeActivity::class.java)
        val pendingIntent = PendingIntent.getActivity(
            this, 0, notificationIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )
        
        val notification = NotificationCompat.Builder(this, "floating_icon_channel")
            .setContentTitle("悬浮图标服务")
            .setContentText("点击进入AI界面")
            .setSmallIcon(R.drawable.icon_transparent)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .build()
        
        startForeground(1, notification)
    }
    
    private fun showFloatingIcon() {
        // 如果悬浮按钮已经显示，直接返回
        if (floatingView != null && isVisible) return
        
        // 如果悬浮按钮存在但不可见，先移除再重新添加
        if (floatingView != null && !isVisible) {
            hideFloatingIcon()
        }
        
        // 启动前台服务
        startForegroundService()
        
        binding = FloatingIconLayoutBinding.inflate(LayoutInflater.from(this))
        floatingView = binding?.root
        
        val layoutParams = WindowManager.LayoutParams().apply {
            type = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                WindowManager.LayoutParams.TYPE_APPLICATION_OVERLAY
            } else {
                @Suppress("DEPRECATION")
                WindowManager.LayoutParams.TYPE_PHONE
            }
            format = PixelFormat.TRANSLUCENT
            flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE
            width = WindowManager.LayoutParams.WRAP_CONTENT
            height = WindowManager.LayoutParams.WRAP_CONTENT
            gravity = Gravity.TOP or Gravity.START  // 改为左上角，允许自由移动
            x = 0
            y = 200
        }
        
        setupFloatingIcon()
        
        // 设置初始状态为虚化状态
        binding?.floatingButton?.let { button ->
            button.alpha = 0.3f
            button.scaleX = 0.5f
            button.scaleY = 0.5f
        }
        
        // 启动Activity检查定时器
        startActivityCheck()
        
        try {
            windowManager?.addView(floatingView, layoutParams)
            isVisible = true
            android.util.Log.d("FloatingIcon", "悬浮按钮已显示")
        } catch (e: Exception) {
            android.util.Log.e("FloatingIcon", "显示悬浮按钮失败", e)
        }
    }
    
    private fun setupFloatingIcon() {
        binding?.apply {
            // 悬浮按钮点击事件
            floatingButton.setOnClickListener {
                if (isExpanded) {
                    // 已展开状态，点击跳转到AI界面
                    val intent = Intent(this@FloatingIconService, AIActivity::class.java).apply {
                        flags = Intent.FLAG_ACTIVITY_NEW_TASK
                    }
                    startActivity(intent)
                    collapseButton()
                } else {
                    // 收起状态，点击展开
                    expandButton()
                }
            }
            
            // 设置拖拽功能
            setupDragListener()
        }
    }
    
    private fun expandButton() {
        binding?.floatingButton?.let { button ->
            // 取消之前的自动收起
            autoCollapseRunnable?.let { button.removeCallbacks(it) }
            
            // 展开动画：从虚化状态变为完全显示
            button.alpha = 0.3f
            button.scaleX = 0.5f
            button.scaleY = 0.5f
            
            val alphaAnim = ObjectAnimator.ofFloat(button, "alpha", 0.3f, 1f)
            val scaleXAnim = ObjectAnimator.ofFloat(button, "scaleX", 0.5f, 1f)
            val scaleYAnim = ObjectAnimator.ofFloat(button, "scaleY", 0.5f, 1f)
            
            alphaAnim.duration = 200
            scaleXAnim.duration = 200
            scaleYAnim.duration = 200
            
            alphaAnim.start()
            scaleXAnim.start()
            scaleYAnim.start()
            
            isExpanded = true
            
            // 设置自动收起
            autoCollapseRunnable = Runnable {
                collapseButton()
            }
            button.postDelayed(autoCollapseRunnable!!, autoCollapseDelay)
        }
    }
    
    private fun collapseButton() {
        binding?.floatingButton?.let { button ->
            // 取消自动收起
            autoCollapseRunnable?.let { button.removeCallbacks(it) }
            
            // 收起动画：从完全显示变为虚化状态
            val alphaAnim = ObjectAnimator.ofFloat(button, "alpha", 1f, 0.3f)
            val scaleXAnim = ObjectAnimator.ofFloat(button, "scaleX", 1f, 0.5f)
            val scaleYAnim = ObjectAnimator.ofFloat(button, "scaleY", 1f, 0.5f)
            
            alphaAnim.duration = 300
            scaleXAnim.duration = 300
            scaleYAnim.duration = 300
            
            alphaAnim.start()
            scaleXAnim.start()
            scaleYAnim.start()
            
            isExpanded = false
        }
    }
    
    private var isDragging = false
    private var longPressRunnable: Runnable? = null
    private val longPressDelay = 500L // 长按延迟时间
    
    private fun setupDragListener() {
        binding?.floatingButton?.setOnTouchListener { view, event ->
            when (event.action) {
                MotionEvent.ACTION_DOWN -> {
                    // 记录当前按钮位置
                    val currentParams = floatingView?.layoutParams as? WindowManager.LayoutParams
                    initialX = currentParams?.x ?: 0
                    initialY = currentParams?.y ?: 0
                    initialTouchX = event.rawX
                    initialTouchY = event.rawY
                    isDragging = false
                    
                    android.util.Log.d("FloatingIcon", "ACTION_DOWN: initialX=$initialX, initialY=$initialY, touchX=$initialTouchX, touchY=$initialTouchY")
                    
                    // 设置长按检测
                    longPressRunnable = Runnable {
                        hideFloatingIcon()
                    }
                    view.postDelayed(longPressRunnable!!, longPressDelay)
                    true
                }
                MotionEvent.ACTION_MOVE -> {
                    val deltaX = (event.rawX - initialTouchX).toInt()
                    val deltaY = (event.rawY - initialTouchY).toInt()
                    val distance = kotlin.math.sqrt((deltaX * deltaX + deltaY * deltaY).toDouble())
                    
                    android.util.Log.d("FloatingIcon", "MOVE: distance=$distance, isDragging=$isDragging")
                    
                    // 立即开始拖拽，不设置阈值
                    if (!isDragging && distance > 0) {
                        isDragging = true
                        android.util.Log.d("FloatingIcon", "开始拖拽")
                        
                        // 取消长按检测
                        longPressRunnable?.let { view.removeCallbacks(it) }
                        
                        // 展开按钮以便拖拽
                        if (!isExpanded) {
                            expandButton()
                        }
                    }
                    
                    if (isDragging) {
                        val layoutParams = floatingView?.layoutParams as? WindowManager.LayoutParams
                        layoutParams?.let { params ->
                            // 使用相对偏移量，但确保方向正确
                            val deltaX = (event.rawX - initialTouchX).toInt()
                            val deltaY = (event.rawY - initialTouchY).toInt()
                            
                            val newX = initialX + deltaX
                            val newY = initialY + deltaY
                            
                            // 边界检查 - 允许悬浮按钮在屏幕内自由移动
                            val screenWidth = resources.displayMetrics.widthPixels
                            val screenHeight = resources.displayMetrics.heightPixels
                            val buttonWidth = 56 // 悬浮按钮宽度
                            val buttonHeight = 56 // 悬浮按钮高度
                            
                            // 确保按钮不会超出屏幕边界
                            params.x = newX.coerceIn(0, screenWidth - buttonWidth)
                            params.y = newY.coerceIn(0, screenHeight - buttonHeight)
                            
                            android.util.Log.d("FloatingIcon", "更新位置: x=${params.x}, y=${params.y}, deltaX=$deltaX, deltaY=$deltaY")
                            windowManager?.updateViewLayout(floatingView, params)
                        }
                    }
                    true
                }
                MotionEvent.ACTION_UP -> {
                    // 取消长按检测
                    longPressRunnable?.let { view.removeCallbacks(it) }
                    
                    android.util.Log.d("FloatingIcon", "ACTION_UP: isDragging=$isDragging")
                    
                    if (isDragging) {
                        // 拖拽结束，智能吸附（只在靠近边缘时吸附）
                        android.util.Log.d("FloatingIcon", "拖拽结束，执行智能吸附")
                        snapToEdge()
                        isDragging = false // 重置拖拽状态
                    } else {
                        // 点击事件 - 展开或跳转
                        android.util.Log.d("FloatingIcon", "点击事件")
                        if (isExpanded) {
                            // 已展开状态，跳转到AI界面
                            val intent = Intent(this@FloatingIconService, AIActivity::class.java).apply {
                                flags = Intent.FLAG_ACTIVITY_NEW_TASK
                            }
                            startActivity(intent)
                            collapseButton()
                        } else {
                            // 收起状态，展开
                            expandButton()
                        }
                    }
                    true
                }
                MotionEvent.ACTION_CANCEL -> {
                    // 取消长按检测
                    longPressRunnable?.let { view.removeCallbacks(it) }
                    true
                }
                else -> false
            }
        }
    }
    
    private fun snapToEdge() {
        val layoutParams = floatingView?.layoutParams as? WindowManager.LayoutParams
        layoutParams?.let { params ->
            val screenWidth = resources.displayMetrics.widthPixels
            val screenHeight = resources.displayMetrics.heightPixels
            val buttonWidth = 56 // 悬浮按钮宽度
            val buttonHeight = 56 // 悬浮按钮高度
            
            // 只在靠近屏幕边缘时才吸附，否则保持当前位置
            val edgeThreshold = 100 // 距离边缘100像素内才吸附
            
            if (params.x < edgeThreshold) {
                // 靠近左边缘，吸附到左边
                params.x = 0
            } else if (params.x > screenWidth - buttonWidth - edgeThreshold) {
                // 靠近右边缘，吸附到右边
                params.x = screenWidth - buttonWidth
            }
            // 否则保持当前位置，不强制吸附
            
            // 确保Y坐标在合理范围内
            params.y = params.y.coerceIn(0, screenHeight - buttonHeight)
            
            windowManager?.updateViewLayout(floatingView, params)
        }
    }
    
    private fun hideFloatingIcon() {
        if (floatingView != null) {
            try {
                windowManager?.removeView(floatingView)
                android.util.Log.d("FloatingIcon", "悬浮按钮已隐藏")
            } catch (e: Exception) {
                android.util.Log.e("FloatingIcon", "隐藏悬浮按钮失败", e)
            }
            floatingView = null
            binding = null
        }
        isVisible = false
        // 停止Activity检查
        stopActivityCheck()
    }
    
    private fun checkCurrentActivity() {
        try {
            // 使用更简单的方法检查当前Activity
            val activityManager = getSystemService(Context.ACTIVITY_SERVICE) as android.app.ActivityManager
            val runningTasks = activityManager.getRunningTasks(1)
            if (runningTasks.isNotEmpty()) {
                val topActivity = runningTasks[0].topActivity
                val className = topActivity?.className
                
                android.util.Log.d("FloatingIcon", "当前Activity: $className")
                
                // 如果在AI界面，隐藏悬浮按钮
                if (className?.contains("AIActivity") == true || className?.contains("ai") == true) {
                    if (isVisible) {
                        android.util.Log.d("FloatingIcon", "在AI界面，隐藏悬浮按钮")
                        hideFloatingIcon()
                    }
                } else {
                    // 如果不在AI界面且悬浮按钮未显示，则显示
                    if (!isVisible && floatingView == null) {
                        android.util.Log.d("FloatingIcon", "不在AI界面，显示悬浮按钮")
                        showFloatingIcon()
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("FloatingIcon", "检查Activity失败", e)
            // 如果检查失败，默认显示悬浮按钮
            if (!isVisible && floatingView == null) {
                showFloatingIcon()
            }
        }
    }
    
    private fun startActivityCheck() {
        activityCheckRunnable = Runnable {
            checkCurrentActivity()
            // 继续检查
            activityCheckRunnable?.let { 
                binding?.floatingButton?.postDelayed(it, activityCheckDelay)
            }
        }
        binding?.floatingButton?.postDelayed(activityCheckRunnable!!, activityCheckDelay)
    }
    
    private fun stopActivityCheck() {
        activityCheckRunnable?.let { 
            binding?.floatingButton?.removeCallbacks(it)
        }
        activityCheckRunnable = null
    }
    
    private fun toggleFloatingIcon() {
        if (isVisible) {
            hideFloatingIcon()
        } else {
            showFloatingIcon()
        }
    }
    
    override fun onDestroy() {
        super.onDestroy()
        stopActivityCheck()
        hideFloatingIcon()
    }
}
