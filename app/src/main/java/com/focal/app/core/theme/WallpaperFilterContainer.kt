package com.focal.app.core.theme

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*

/**
 * 现代 Material 3 沉浸式壁纸与色彩矩阵滤镜渲染容器
 * 根据用户在设置中的选择，动态调整背景图层透明度与对比度。
 */
@Composable
fun WallpaperFilterContainer(
    wallpaperKey: String,
    opacity: Float,
    contrast: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseSurface = MaterialTheme.colorScheme.background

    // 计算对比度 ColorMatrix (围绕 0.5 进行放大缩小，公式: scale * (color - 0.5) + 0.5)
    val scale = contrast.coerceIn(0.5f, 1.5f)
    val translate = (-0.5f * scale + 0.5f) * 255f
    val contrastMatrix = ColorMatrix(
        floatArrayOf(
            scale, 0f, 0f, 0f, translate,
            0f, scale, 0f, 0f, translate,
            0f, 0f, scale, 0f, translate,
            0f, 0f, 0f, 1f, 0f
        )
    )

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseSurface)
    ) {
        // 壁纸渲染层（支持预置壁纸纹理与透明度遮罩）
        if (wallpaperKey != "none") {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .drawBehind {
                        // 绘制基于算法的优雅东方美学意境底色纹理（兼顾免外置大图资源）
                        when (wallpaperKey) {
                            "bamboo" -> {
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF0D1E16),
                                            Color(0xFF0A1410),
                                            Color(0xFF080D0B)
                                        ),
                                        start = Offset(0f, 0f),
                                        end = Offset(size.width, size.height)
                                    ),
                                    alpha = opacity,
                                    colorFilter = ColorFilter.colorMatrix(contrastMatrix)
                                )
                            }
                            "watertown" -> {
                                drawRect(
                                    brush = Brush.radialGradient(
                                        colors = listOf(
                                            Color(0xFF161B22),
                                            Color(0xFF0E1116),
                                            Color(0xFF07090C)
                                        ),
                                        center = Offset(size.width * 0.7f, size.height * 0.3f),
                                        radius = size.width
                                    ),
                                    alpha = opacity,
                                    colorFilter = ColorFilter.colorMatrix(contrastMatrix)
                                )
                            }
                            "sunset" -> {
                                drawRect(
                                    brush = Brush.verticalGradient(
                                        colors = listOf(
                                            Color(0xFF261916),
                                            Color(0xFF1C1312),
                                            Color(0xFF0F0C0B)
                                        )
                                    ),
                                    alpha = opacity,
                                    colorFilter = ColorFilter.colorMatrix(contrastMatrix)
                                )
                            }
                            else -> {
                                // Default Subtle Mesh
                                drawRect(
                                    brush = Brush.linearGradient(
                                        colors = listOf(
                                            Color(0xFF14191C),
                                            Color(0xFF0E1315),
                                            Color(0xFF0A0C0E)
                                        )
                                    ),
                                    alpha = opacity,
                                    colorFilter = ColorFilter.colorMatrix(contrastMatrix)
                                )
                            }
                        }
                    }
            )
        }

        // 上层内容挂载
        content()
    }
}
