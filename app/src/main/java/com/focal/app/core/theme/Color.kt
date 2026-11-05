package com.focal.app.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.ui.graphics.Color

// 东方美学色彩体系：水墨意象、竹青点缀、留白与深空
val InkBlack = Color(0xFF0F1115)       // 浓墨深空
val InkSurface = Color(0xFF181B20)     // 玄灰卡片
val InkSurfaceVariant = Color(0xFF22262F)
val InkBorder = Color(0xFF2E3440)      // 淡淡墨界

val BambooGreen = Color(0xFF2EB872)    // 竹青生机（主激活色）
val BambooGreenSoft = Color(0xFF1E5C42)
val MoonWhite = Color(0xFFE8ECF2)      // 月白（高亮文本与核心图标）
val MistGrey = Color(0xFF8A93A4)       // 寒雾灰（弱化辅助与次要图标）

val CinnabarRed = Color(0xFFE05347)    // 朱砂红（状态微红点与警告）
val PorcelainBlue = Color(0xFF3880FF)  // 霁蓝（智汇星芒点缀）

val FocalColorScheme = darkColorScheme(
    primary = BambooGreen,
    onPrimary = MoonWhite,
    primaryContainer = BambooGreenSoft,
    surface = InkSurface,
    onSurface = MoonWhite,
    surfaceVariant = InkSurfaceVariant,
    onSurfaceVariant = MistGrey,
    background = InkBlack,
    onBackground = MoonWhite,
    outline = InkBorder
)
