# AI聊天功能配置说明

## 问题分析
从日志可以看出，原来的API `https://redservingapi.devops.xiaohongshu.com` 是小红书内部的办公网络API，需要特殊的网络环境和认证，无法在外部网络访问。

## 解决方案
已修改为使用DeepSeek API服务（在中国更稳定，价格更便宜）。您需要配置以下内容：

### 1. 获取DeepSeek API密钥
1. 访问 [DeepSeek官网](https://platform.deepseek.com/)
2. 注册账号并登录
3. 进入 API Keys 页面
4. 创建新的API密钥
5. 复制生成的密钥（格式：sk-xxxxxxxxxxxxxxxxxxxxxxxxxxxxxxxx）

### 2. 配置API密钥
在 `AIChatService.kt` 文件中，将以下行：
```kotlin
private const val API_KEY = "sk-your-deepseek-api-key-here"
```
替换为您的真实API密钥：
```kotlin
private const val API_KEY = "sk-您的真实DeepSeek API密钥"
```

### 3. 其他可选方案
如果您想使用其他AI API服务，也可以选择：

#### 方案A：使用OpenAI API（需要科学上网）
```kotlin
private const val API_BASE_URL = "https://api.openai.com/v1/chat/completions"
private const val API_KEY = "您的OpenAI API密钥"
```

#### 方案B：使用Claude API
```kotlin
private const val API_BASE_URL = "https://api.anthropic.com/v1/messages"
private const val API_KEY = "您的Claude API密钥"
```

#### 方案C：使用通义千问API
```kotlin
private const val API_BASE_URL = "https://dashscope.aliyuncs.com/api/v1/services/aigc/text-generation/generation"
private const val API_KEY = "您的通义千问API密钥"
```

### 4. 测试连接
配置完成后，重新编译运行应用，AI聊天功能应该可以正常工作。

## 注意事项
- 请妥善保管您的API密钥，不要将其提交到公共代码仓库
- 不同API服务的请求格式可能略有不同，如需要可以进一步调整
- 建议在生产环境中使用环境变量或配置文件来管理API密钥
