package com.focal.app.core.theme

import android.graphics.BitmapFactory
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.*
import androidx.compose.ui.layout.ContentScale
import java.io.File

/**
 * 现代 Material 3 沉浸式壁纸与色彩矩阵滤镜渲染容器
 * 支持算法意境预置壁纸与用户自定义上传图片，动态支持透明度与对比度矩阵调节。
 */
@Composable
fun WallpaperFilterContainer(
    wallpaperKey: String,
    customImagePath: String? = null,
    opacity: Float,
    contrast: Float,
    modifier: Modifier = Modifier,
    content: @Composable () -> Unit
) {
    val baseSurface = MaterialTheme.colorScheme.background

    // 计算对比度 ColorMatrix
    val scale = contrast.coerceIn(0.5f, 1.5f)
    val translate = (-0.5f * scale + 0.5f) * 255f
    val contrastMatrix = remember(contrast) {
        ColorMatrix(
            floatArrayOf(
                scale, 0f, 0f, 0f, translate,
                0f, scale, 0f, 0f, translate,
                0f, 0f, scale, 0f, translate,
                0f, 0f, 0f, 1f, 0f
            )
        )
    }

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(baseSurface)
    ) {
        if (wallpaperKey != "none") {
            if (wallpaperKey == "custom" && customImagePath != null && File(customImagePath).exists()) {
                // 用户自定义相册上传的本地图片渲染
                val customBitmap = remember(customImagePath) {
                    try {
                        BitmapFactory.decodeFile(customImagePath)?.asImageBitmap()
                    } catch (_: Exception) {
                        null
                    }
                }

                if (customBitmap != null) {
                    Image(
                        bitmap = customBitmap,
                        contentDescription = "Custom Wallpaper",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        alpha = opacity,
                        colorFilter = ColorFilter.colorMatrix(contrastMatrix)
                    )
                }
            } else {
                // 预置算法水墨意境壁纸
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .drawBehind {
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
        }

        content()
    }
}
