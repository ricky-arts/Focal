package com.focal.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Star
import androidx.compose.material.icons.rounded.Tune
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.material.icons.rounded.Whatshot
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 聚焦全网资讯与热榜的核心板块枚举：
 * 专注于资讯聚合、全网热榜看板、精选收藏与自定义频道
 */
enum class FocalSection(
    val icon: ImageVector,
    val token: String
) {
    HOT(Icons.Rounded.Whatshot, "榜"),     // 全平台实时热榜与快讯
    FEEDS(Icons.Rounded.ViewAgenda, "阅"),  // 关注频道与深度图文流
    FAVORITES(Icons.Rounded.Star, "藏"),   // 离线收藏与稍后读
    SETTINGS(Icons.Rounded.Tune, "设")     // 账号绑定中心与画境
}
