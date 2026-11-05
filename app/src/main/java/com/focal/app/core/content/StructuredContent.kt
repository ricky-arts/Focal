package com.focal.app.core.content

/**
 * 原生渲染多形态内容类型定义
 */
enum class ContentType {
    ARTICLE,  // 资讯图文排版
    VIDEO,    // 原生视频播放器
    GALLERY   // 原生图集画廊
}

enum class BlockType {
    PARAGRAPH, // 普通段落
    IMAGE,     // 原生正文图片
    SUBTITLE,  // 二级小标题
    HIGHLIGHT  // 金句引用/高亮
}

data class ContentBlock(
    val type: BlockType,
    val text: String? = null,
    val imageUrl: String? = null,
    val caption: String? = null
)

/**
 * 结构化主体内容模型（广告、无关推荐等已被彻底清洗过滤）
 */
data class StructuredContent(
    val id: String,
    val title: String,
    val author: String,
    val publishTime: String,
    val sourceName: String,
    val sourceTag: String,
    val contentType: ContentType,
    val blocks: List<ContentBlock> = emptyList(),
    val videoUrl: String? = null,
    val galleryImages: List<String> = emptyList(),
    val summaryText: String = "",
    val originalUrl: String = ""
)
