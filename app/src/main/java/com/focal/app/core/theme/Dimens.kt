package com.focal.app.core.theme

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * Material 3 统一设计度量与尺寸规范
 */
object FocalDimens {
    // 间距模数（8dp / 16dp 系统）
    val spaceExtraSmall = 4.dp
    val spaceSmall = 8.dp
    val spaceMedium = 16.dp
    val spaceLarge = 24.dp
    val spaceExtraLarge = 32.dp

    // 页面安全左右边距
    val pageHorizontalPadding = 16.dp

    // 图标统一尺寸 (Material 3 标准 24dp)
    val iconSizeStandard = 24.dp
    val iconSizeSmall = 18.dp
    val iconSizeLarge = 28.dp

    // 圆角全局规范：卡片统一 12dp，常规按钮 8dp，小标签 4dp
    val shapeCard = RoundedCornerShape(12.dp)
    val shapeButton = RoundedCornerShape(8.dp)
    val shapeTag = RoundedCornerShape(4.dp)
    val shapePill = RoundedCornerShape(20.dp)

    // 柔和微阴影 Elevation (1 ~ 3dp)
    val elevationSubtle = 1.dp
    val elevationCard = 2.dp
    val elevationFloat = 3.dp

    // 文字字号层级规范 (Roboto 字体系统)
    val textTitle = 18.sp       // 大标题 18sp
    val textHeadline = 16.sp
    val textBody = 14.sp        // 正文 14sp
    val textCaption = 12.sp     // 辅助说明文字 12sp
    val textBadge = 10.sp       // 极简微标 10sp
}
