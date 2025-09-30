package com.example.filmguide.social

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Typeface
import android.net.Uri
import android.view.View
import androidx.core.content.FileProvider
import com.example.filmguide.logic.recordroom.RecordEntity
import java.io.File
import java.io.FileOutputStream
import java.text.SimpleDateFormat
import java.util.*

/**
 * 社交分享管理器
 * 处理观影记录的分享功能
 */
class ShareManager(private val context: Context) {
    
    /**
     * 分享观影记录到微信
     */
    fun shareToWeChat(record: RecordEntity) {
        val shareText = generateShareText(record)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.tencent.mm") // 微信包名
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    /**
     * 分享到朋友圈
     */
    fun shareToMoments(record: RecordEntity) {
        val shareText = generateShareText(record)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.tencent.mm")
            putExtra("Kdescription", shareText) // 朋友圈专用参数
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    /**
     * 分享到微博
     */
    fun shareToWeibo(record: RecordEntity) {
        val shareText = generateShareText(record)
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, shareText)
            setPackage("com.sina.weibo")
        }
        
        if (intent.resolveActivity(context.packageManager) != null) {
            context.startActivity(intent)
        }
    }
    
    /**
     * 生成分享图片
     */
    fun generateShareImage(record: RecordEntity): Uri? {
        return try {
            val bitmap = createShareBitmap(record)
            val file = File(context.cacheDir, "share_${System.currentTimeMillis()}.jpg")
            val outputStream = FileOutputStream(file)
            bitmap.compress(Bitmap.CompressFormat.JPEG, 90, outputStream)
            outputStream.close()
            
            FileProvider.getUriForFile(
                context,
                "${context.packageName}.fileprovider",
                file
            )
        } catch (e: Exception) {
            null
        }
    }
    
    /**
     * 分享图片到社交媒体
     */
    fun shareImageToSocial(record: RecordEntity) {
        val imageUri = generateShareImage(record)
        if (imageUri != null) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                type = "image/*"
                putExtra(Intent.EXTRA_STREAM, imageUri)
                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }
            
            context.startActivity(Intent.createChooser(intent, "分享到"))
        }
    }
    
    /**
     * 生成分享文本
     */
    private fun generateShareText(record: RecordEntity): String {
        val date = SimpleDateFormat("yyyy年MM月dd日", Locale.getDefault()).format(Date())
        
        return """
            🎬 观影记录分享
            
            📽️ 电影：${record.title}
            📅 时间：${record.date}
            📍 地点：${record.location ?: "未知"}
            🌤️ 天气：${record.weather ?: "未知"}
            
            💭 我的感受：
            ${record.article}
            
            #ShowGuide #观影记录 #电影推荐
        """.trimIndent()
    }
    
    /**
     * 创建分享图片
     */
    private fun createShareBitmap(record: RecordEntity): Bitmap {
        val width = 800
        val height = 1000
        val bitmap = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(bitmap)
        
        // 背景
        canvas.drawColor(Color.parseColor("#1a1a1a"))
        
        val paint = Paint().apply {
            isAntiAlias = true
            textSize = 48f
            color = Color.WHITE
            typeface = Typeface.DEFAULT_BOLD
        }
        
        // 标题
        canvas.drawText("🎬 我的观影记录", 50f, 100f, paint)
        
        // 电影信息
        paint.textSize = 36f
        canvas.drawText("📽️ ${record.title}", 50f, 200f, paint)
        canvas.drawText("📅 ${record.date}", 50f, 280f, paint)
        canvas.drawText("📍 ${record.location ?: "未知"}", 50f, 360f, paint)
        canvas.drawText("🌤️ ${record.weather ?: "未知"}", 50f, 440f, paint)
        
        // 感受
        paint.textSize = 32f
        canvas.drawText("💭 我的感受：", 50f, 540f, paint)
        
        // 分段显示文章内容
        val article = record.article
        val lines = article.chunked(30) // 每行30个字符
        var y = 600f
        lines.take(8).forEach { line -> // 最多显示8行
            canvas.drawText(line, 50f, y, paint)
            y += 40f
        }
        
        // 底部标签
        paint.textSize = 28f
        paint.color = Color.parseColor("#4CAF50")
        canvas.drawText("#ShowGuide #观影记录", 50f, 950f, paint)
        
        return bitmap
    }
    
    /**
     * 获取分享选项
     */
    fun getShareOptions(): List<ShareOption> {
        return listOf(
            ShareOption("微信", "com.tencent.mm", com.example.filmguide.R.drawable.ic_wechat),
            ShareOption("朋友圈", "com.tencent.mm", com.example.filmguide.R.drawable.ic_moments),
            ShareOption("微博", "com.sina.weibo", com.example.filmguide.R.drawable.ic_weibo),
            ShareOption("QQ", "com.tencent.mobileqq", com.example.filmguide.R.drawable.ic_qq),
            ShareOption("更多", "", com.example.filmguide.R.drawable.ic_more)
        )
    }
    
    data class ShareOption(
        val name: String,
        val packageName: String,
        val iconRes: Int
    )
}
