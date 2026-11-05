package com.focal.app.core.theme

import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.ui.graphics.Color

/**
 * Material 3 官方动态色彩体系规范
 * 支持多种优雅主题方案：Dark (暗色墨韵)、Light (亮色素雅)、Emerald (竹青墨韵)
 * 拒绝硬编码颜色，所有组件颜色严格从 MaterialTheme.colorScheme 读取。
 */

// 1. 默认暗色高级方案 (Dark M3 Scheme)
val DarkColorScheme = darkColorScheme(
    primary = Color(0xFF6CDAA3),
    onPrimary = Color(0xFF003822),
    primaryContainer = Color(0xFF005234),
    onPrimaryContainer = Color(0xFF89F8BE),

    secondary = Color(0xFFB5CCBA),
    onSecondary = Color(0xFF213528),
    secondaryContainer = Color(0xFF374B3E),
    onSecondaryContainer = Color(0xFFD1E8D6),

    tertiary = Color(0xFFA5CDDE),
    onTertiary = Color(0xFF063543),
    tertiaryContainer = Color(0xFF234C5B),
    onTertiaryContainer = Color(0xFFC0E9FA),

    error = Color(0xFFFFB4AB),
    onError = Color(0xFF690005),
    errorContainer = Color(0xFF93000A),
    onErrorContainer = Color(0xFFFFDAD6),

    background = Color(0xFF111413),
    onBackground = Color(0xFFE1E3DF),
    surface = Color(0xFF191C1B),
    onSurface = Color(0xFFE1E3DF),
    surfaceVariant = Color(0xFF232725),
    onSurfaceVariant = Color(0xFF8B928E),
    outline = Color(0xFF3F4943),
    outlineVariant = Color(0xFF2A312D)
)

// 2. 亮色高雅方案 (Light M3 Scheme)
val LightColorScheme = lightColorScheme(
    primary = Color(0xFF006C47),
    onPrimary = Color(0xFFFFFFFF),
    primaryContainer = Color(0xFF89F8BE),
    onPrimaryContainer = Color(0xFF002113),

    secondary = Color(0xFF4E6354),
    onSecondary = Color(0xFFFFFFFF),
    secondaryContainer = Color(0xFFD1E8D6),
    onSecondaryContainer = Color(0xFF0B1F14),

    tertiary = Color(0xFF3C6473),
    onTertiary = Color(0xFFFFFFFF),
    tertiaryContainer = Color(0xFFC0E9FA),
    onTertiaryContainer = Color(0xFF001F29),

    error = Color(0xFFBA1A1A),
    onError = Color(0xFFFFFFFF),
    errorContainer = Color(0xFFFFDAD6),
    onErrorContainer = Color(0xFF410002),

    background = Color(0xFFFBFDFA),
    onBackground = Color(0xFF191C1B),
    surface = Color(0xFFF2F4F1),
    onSurface = Color(0xFF191C1B),
    surfaceVariant = Color(0xFFDBE5DE),
    onSurfaceVariant = Color(0xFF404944),
    outline = Color(0xFF707973),
    outlineVariant = Color(0xFFBFC9C2)
)

// 3. 经典青竹暗色方案 (Emerald Dark)
val EmeraldDarkColorScheme = darkColorScheme(
    primary = Color(0xFF4EDE9D),
    onPrimary = Color(0xFF003821),
    primaryContainer = Color(0xFF005232),
    onPrimaryContainer = Color(0xFF6FFBBA),

    secondary = Color(0xFFB3CCBB),
    onSecondary = Color(0xFF1F3529),
    secondaryContainer = Color(0xFF354B3E),
    onSecondaryContainer = Color(0xFFCFE8D7),

    background = Color(0xFF0B100E),
    onBackground = Color(0xFFE0E3E0),
    surface = Color(0xFF131A16),
    onSurface = Color(0xFFE0E3E0),
    surfaceVariant = Color(0xFF1C2520),
    onSurfaceVariant = Color(0xFF85938B),
    outline = Color(0xFF37443D),
    outlineVariant = Color(0xFF222D27)
)
