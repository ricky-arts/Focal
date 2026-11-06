package com.focal.app.core.model

sealed class ContentBlockModel {
    data class Heading(val level: Int, val text: String) : ContentBlockModel()
    data class Paragraph(val text: String) : ContentBlockModel()
    data class Image(val originalUrl: String, val proxyUrl: String, val caption: String?) : ContentBlockModel()
    data class Video(val bvid: String?, val streamUrl: String?, val videoTrackUrl: String?, val audioTrackUrl: String?, val coverUrl: String?) : ContentBlockModel()
    data class Quote(val text: String) : ContentBlockModel()
    data class Code(val code: String, val language: String?) : ContentBlockModel()
    object Divider : ContentBlockModel()
}

data class UnifiedComment(
    val id: String,
    val authorName: String,
    val authorAvatar: String,
    val content: String,
    val publishTime: String,
    val likeCount: Int,
    val location: String?,
    val subComments: List<UnifiedComment> = emptyList()
)

data class ArticleDetailModel(
    val id: String,
    val platform: String,
    val title: String,
    val authorName: String?,
    val authorAvatar: String?,
    val publishTime: String,
    val originUrl: String,
    val blocks: List<ContentBlockModel>,
    val commentCount: Int,
    val likeCount: Int? = null
)
