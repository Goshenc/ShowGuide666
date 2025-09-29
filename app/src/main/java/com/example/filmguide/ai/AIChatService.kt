package com.example.filmguide.ai

import android.util.Log
import com.google.gson.Gson
import com.google.gson.annotations.SerializedName
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.*
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.RequestBody.Companion.toRequestBody
import java.io.IOException
import java.util.concurrent.TimeUnit
import java.net.URLEncoder

/**
 * AI聊天服务类
 * 基于前端项目的AI API实现
 */
class AIChatService {
    
    companion object {
        private const val TAG = "AIChatService"
        // 使用通义千问API服务（支持联网搜索）
        private const val API_BASE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
        private const val API_KEY = "sk-e79e159422194e2ab425e80e67ac5494" // 通义千问API Key
        private const val TIMEOUT_SECONDS = 60L
    }
    
    private val client = OkHttpClient.Builder()
        .connectTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .readTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .writeTimeout(TIMEOUT_SECONDS, TimeUnit.SECONDS)
        .build()
    
    private val gson = Gson()
    
    /**
     * 聊天消息数据类
     */
    data class ChatMessage(
        @SerializedName("role") val role: String, // "user" 或 "assistant" 或 "system"
        @SerializedName("content") val content: String
    )
    
    /**
     * 通义千问API请求体
     */
    data class ChatRequest(
        @SerializedName("model") val model: String = "qwen-plus",
        @SerializedName("input") val input: InputData,
        @SerializedName("parameters") val parameters: Parameters
    )
    
    data class InputData(
        @SerializedName("messages") val messages: List<ChatMessage>
    )
    
    data class Parameters(
        @SerializedName("temperature") val temperature: Double = 0.1,
        @SerializedName("max_tokens") val maxTokens: Int = 4000,
        @SerializedName("enable_search") val enableSearch: Boolean = true,
        @SerializedName("search_result_count") val searchResultCount: Int = 5
    )
    
    /**
     * 流式响应回调接口
     */
    interface StreamCallback {
        fun onPartialResponse(content: String)
        fun onCompleteResponse(content: String)
        fun onError(error: String)
    }
    
    /**
     * 发送聊天消息并获取流式响应
     */
    suspend fun sendMessage(
        userMessage: String,
        conversationHistory: List<ChatMessage> = emptyList(),
        callback: StreamCallback
    ) = withContext(Dispatchers.IO) {
        
        try {
            // 添加超时处理
            val timeoutHandler = android.os.Handler(android.os.Looper.getMainLooper())
            val timeoutRunnable = Runnable {
                callback.onError("请求超时，请重试")
            }
            timeoutHandler.postDelayed(timeoutRunnable, TIMEOUT_SECONDS * 1000)
            
            // 构建消息列表
            val messages = mutableListOf<ChatMessage>().apply {
                // 添加系统提示词
                add(ChatMessage("system", getSystemPrompt()))
                // 添加对话历史
                addAll(conversationHistory)
                // 添加用户消息
                add(ChatMessage("user", userMessage))
            }
            
            // 构建请求体
            val requestBody = ChatRequest(
                input = InputData(messages = messages),
                parameters = Parameters(
                    enableSearch = true,
                    searchResultCount = 5
                )
            )
            
            val request = Request.Builder()
                .url(API_BASE_URL)
                .post(gson.toJson(requestBody).toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $API_KEY")
                .addHeader("X-DashScope-SSE", "enable")
                .build()
            
            Log.d(TAG, "发送AI请求: ${gson.toJson(requestBody)}")
            Log.d(TAG, "请求URL: $API_BASE_URL")
            Log.d(TAG, "请求头: Content-Type=application/json, Authorization=Bearer $API_KEY")
            Log.d(TAG, "联网搜索已启用: enableSearch=${requestBody.parameters.enableSearch}, searchResultCount=${requestBody.parameters.searchResultCount}")
            
            val response = client.newCall(request).execute()
            
            Log.d(TAG, "响应状态码: ${response.code}")
            Log.d(TAG, "响应头: ${response.headers}")
            
            if (!response.isSuccessful) {
                val errorBody = response.body?.string() ?: "未知错误"
                Log.e(TAG, "AI API请求失败: ${response.code} - $errorBody")
                Log.e(TAG, "响应体: $errorBody")
                callback.onError("请求失败: ${response.code} - $errorBody")
                return@withContext
            }
            
            // 处理流式响应
            response.body?.let { body ->
                val source = body.source()
                var buffer = ""
                var fullContent = ""
                
                try {
                    var lineCount = 0
                    while (!source.exhausted()) {
                        val line = source.readUtf8Line()
                        lineCount++
                        Log.d(TAG, "读取第${lineCount}行: $line")
                        
                        if (line != null) {
                            buffer += line + "\n"
                            val lines = buffer.split("\n")
                            buffer = lines.lastOrNull() ?: ""
                            
                            for (dataLine in lines.dropLast(1)) {
                                Log.d(TAG, "处理数据行: $dataLine")
                                
                                if (dataLine.trim().startsWith("data:")) {
                                    val data = dataLine.substring(5).trim()
                                    Log.d(TAG, "解析数据: $data")
                                    
                                    if (data == "[DONE]") {
                                        Log.d(TAG, "收到完成信号")
                                        timeoutHandler.removeCallbacks(timeoutRunnable)
                                        callback.onCompleteResponse(fullContent)
                                        return@withContext
                                    }
                                    
                                    try {
                                        val jsonData = gson.fromJson(data, Map::class.java)
                                        Log.d(TAG, "解析JSON成功: $jsonData")
                                        
                                        // 通义千问API的响应格式
                                        val output = jsonData["output"] as? Map<*, *>
                                        if (output != null) {
                                            val text = output["text"] as? String
                                            val finishReason = output["finish_reason"] as? String
                                            
                                            Log.d(TAG, "找到output.text: $text, finish_reason: $finishReason")
                                            
                                            if (!text.isNullOrEmpty()) {
                                                // 通义千问返回的是完整文本，不是增量
                                                fullContent = text
                                                Log.d(TAG, "更新内容: $fullContent")
                                                callback.onPartialResponse(fullContent)
                                            }
                                            
                                            // 检查是否完成
                                            if (finishReason == "stop") {
                                                Log.d(TAG, "收到完成信号: $finishReason")
                                                timeoutHandler.removeCallbacks(timeoutRunnable)
                                                callback.onCompleteResponse(fullContent)
                                                return@withContext
                                            }
                                        }
                                        
                                        // 兼容其他格式
                                        val choices = jsonData["choices"] as? List<*>
                                        if (choices != null && choices.isNotEmpty()) {
                                            val choice = choices[0] as? Map<*, *>
                                            val delta = choice?.get("delta") as? Map<*, *>
                                            val content = delta?.get("content") as? String
                                            
                                            if (!content.isNullOrEmpty()) {
                                                fullContent += content
                                                Log.d(TAG, "更新内容(choices): $fullContent")
                                                callback.onPartialResponse(fullContent)
                                            }
                                        }
                                        
                                    } catch (e: Exception) {
                                        Log.w(TAG, "解析流数据失败: $e, 数据: $data")
                                    }
                                } else if (dataLine.trim().startsWith("event: ")) {
                                    Log.d(TAG, "收到事件: $dataLine")
                                } else if (dataLine.trim().isNotEmpty()) {
                                    Log.d(TAG, "其他数据: $dataLine")
                                }
                            }
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "处理流式响应失败", e)
                    timeoutHandler.removeCallbacks(timeoutRunnable)
                    callback.onError("处理响应失败: ${e.message}")
                } finally {
                    source.close()
                }
            }
            
        } catch (e: Exception) {
            Log.e(TAG, "AI聊天请求失败", e)
            callback.onError("网络请求失败: ${e.message}")
        }
    }
    
    
    /**
     * 测试API连接
     */
    suspend fun testConnection(): Boolean = withContext(Dispatchers.IO) {
        try {
            val request = Request.Builder()
                .url(API_BASE_URL)
                .post("{}".toRequestBody("application/json".toMediaType()))
                .addHeader("Content-Type", "application/json")
                .addHeader("Authorization", "Bearer $API_KEY")
                .build()
            
            val response = client.newCall(request).execute()
            Log.d(TAG, "API连接测试 - 状态码: ${response.code}")
            response.close()
            response.code in 200..499 // 即使是400错误也说明连接成功
        } catch (e: Exception) {
            Log.e(TAG, "API连接测试失败", e)
            false
        }
    }
    
    /**
     * 获取系统提示词
     */
    private fun getSystemPrompt(): String {
        val currentTime = java.text.SimpleDateFormat("yyyy年MM月dd日 HH:mm", java.util.Locale.getDefault()).format(java.util.Date())
        
        return """你是ShowGuide的AI推荐官，专门帮助用户发现和推荐各种娱乐活动。

你的主要职责：
1. 你可以推荐演唱会、话剧、音乐剧、电影等演出，还可以推荐旅游景点名胜古迹、告知今日$currentTime 天气、告诉用户怎么去某一个地方等等等智能AI助手
2. 推荐周末市集、艺术展览、亲子活动等休闲活动
3. 根据用户的喜好、预算和日期提供个性化推荐
4. 提供活动相关的实用信息（时间、地点、票价等）
5. 提供的信息一定一定要最新的，这极度重要，你是有联网搜索能力的，可以获取截止$currentTime 的最新数据

重要提醒：
- 你具备联网搜索功能，可以获取最新的信息
- 当前时间是：$currentTime，请基于这个时间点搜索最新的信息
- 当用户询问电影、演出、活动等信息时，请主动使用联网搜索获取最新数据
- 电影推荐需要时效性，请搜索并推荐正在热映或即将上映的电影
- 演唱会、演出活动请搜索最新的档期信息
- 提供准确的票价、时间、地点等实用信息
- 如果用户询问具体电影信息，请搜索最新的上映状态和评价
- 提供今日$currentTime 天气
- 所有推荐的信息必须基于当前时间 $currentTime 的最新数据

输出格式要求：
- 严禁使用markdown格式（如#、**、*、```等符号）
- 严禁使用任何markdown语法
- 只使用纯文本格式回复
- 可以使用简单的换行和空格来组织内容
- 保持内容清晰易读，但必须是纯文本

请保持友好、专业的语调，提供准确、有用的信息。"""
    }
}
