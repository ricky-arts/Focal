package com.focal.app.core.navigation

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.AutoAwesome
import androidx.compose.material.icons.rounded.Headphones
import androidx.compose.material.icons.rounded.PlayCircle
import androidx.compose.material.icons.rounded.ViewAgenda
import androidx.compose.ui.graphics.vector.ImageVector

/**
 * 极简单字意象板块枚举（主界面导航仅以纯图标标识）
 */
enum class FocalSection(
    val icon: ImageVector,
    val token: String // 单字符意象：阅、视、听、问
) {
    READ(Icons.Rounded.ViewAgenda, "阅"),
    WATCH(Icons.Rounded.PlayCircle, "视"),
    LISTEN(Icons.Rounded.Headphones, "听"),
    ASK(Icons.Rounded.AutoAwesome, "问")
}
