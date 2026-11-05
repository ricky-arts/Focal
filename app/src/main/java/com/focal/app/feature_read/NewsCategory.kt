package com.focal.app.feature_read

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 用户关心的资讯栏目/分类
 */
enum class NewsCategory(
    val key: String,
    val displayName: String,
    val icon: ImageVector,
    val token: String // 极简单字意象
) {
    GENERAL("general", "综合热点", Icons.Rounded.TrendingUp, "榜"),
    ENTERTAINMENT("entertainment", "娱乐文娱", Icons.Rounded.TheaterComedy, "娱"),
    SPORTS("sports", "体育竞技", Icons.Rounded.SportsBasketball, "体"),
    TECH("tech", "科技数码", Icons.Rounded.Memory, "技"),
    FINANCE("finance", "商业财经", Icons.Rounded.ShowChart, "财"),
    WORLD("world", "全球视野", Icons.Rounded.Public, "环");

    companion object {
        fun fromKey(key: String): NewsCategory {
            return values().firstOrNull { it.key == key } ?: GENERAL
        }
    }
}
