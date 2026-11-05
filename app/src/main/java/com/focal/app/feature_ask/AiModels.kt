package com.focal.app.feature_ask

/**
 * 免费/直用 AI 平台定义
 */
enum class AiPlatform(
    val key: String,
    val displayName: String,
    val badgeChar: String // 单字符极简徽标："豆"、"K"、"问"、"宝"
) {
    DOUBAO("doubao", "豆包", "豆"),
    KIMI("kimi", "Kimi", "K"),
    QWEN("qwen", "通义千问", "问"),
    YUANBAO("yuanbao", "腾讯元宝", "宝")
}

data class AiResponseItem(
    val platform: AiPlatform,
    val text: String,
    val isStreaming: Boolean = false,
    val error: String? = null
)
