package com.focal.app.core.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import coil.compose.SubcomposeAsyncImage
import coil.request.ImageRequest
import com.focal.app.core.network.FocalHttpClient
import com.focal.app.core.theme.FocalDimens

/**
 * 全板块统一卡片布局规范：
 * 【左侧固定比例缩略图区域 + 右侧文字信息区域】
 * 1. 缩略图集成 Coil 异步加载 + 内存/磁盘双重缓存；
 * 2. 视频类自动叠放播放小图标，图集类自动叠放图集小微标；
 * 3. 无图/加载失败展示柔和 Material 3 矢量占位图（杜绝生硬空白黑块）；
 * 4. 右侧两端规整对齐：上部标题 (2行)+ 底部【来源微标 + 时间 + 热度】。
 */
@Composable
fun UnifiedMediaCard(
    title: String,
    sourceName: String,
    timeAgo: String,
    hotScoreDisplay: String,
    rank: Int? = null,
    thumbnailUrl: String?,
    isVideo: Boolean = false,
    isGallery: Boolean = false,
    fallbackIcon: ImageVector = Icons.Rounded.Article,
    thumbWidth: Dp = 108.dp,
    thumbHeight: Dp = 76.dp,
    onClick: () -> Unit
) {
    val context = LocalContext.current
    val imageLoader = FocalHttpClient.getImageLoader(context)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FocalDimens.shapeCard,
        elevation = CardDefaults.cardElevation(defaultElevation = FocalDimens.elevationSubtle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FocalDimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 左侧缩略图区域 (Coil 异步加载 + 优雅占位)
            Box(
                modifier = Modifier
                    .size(width = thumbWidth, height = thumbHeight)
                    .clip(FocalDimens.shapeButton)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                if (!thumbnailUrl.isNullOrBlank()) {
                    SubcomposeAsyncImage(
                        model = ImageRequest.Builder(context)
                            .data(thumbnailUrl)
                            .crossfade(true)
                            .build(),
                        imageLoader = imageLoader,
                        contentDescription = "Thumbnail",
                        modifier = Modifier.fillMaxSize(),
                        contentScale = ContentScale.Crop,
                        loading = {
                            Box(
                                modifier = Modifier
                                    .fillMaxSize()
                                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator(
                                    modifier = Modifier.size(20.dp),
                                    strokeWidth = 2.dp,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        },
                        error = {
                            VectorPlaceholderBox(fallbackIcon)
                        }
                    )
                } else {
                    VectorPlaceholderBox(fallbackIcon)
                }

                // 如果是视频内容，在缩略图正中叠加半透明播放微标
                if (isVideo) {
                    Box(
                        modifier = Modifier
                            .size(32.dp)
                            .clip(CircleShape)
                            .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.45f)),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.PlayArrow,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onPrimary,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }

                // 如果是图集内容，在缩略图右下角叠放图集小胶囊
                if (isGallery) {
                    Surface(
                        modifier = Modifier
                            .align(Alignment.BottomEnd)
                            .padding(4.dp),
                        shape = FocalDimens.shapeTag,
                        color = MaterialTheme.colorScheme.scrim.copy(alpha = 0.6f)
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier.padding(horizontal = 4.dp, vertical = 2.dp)
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Collections,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(10.dp)
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.width(FocalDimens.spaceMedium))

            // 右侧文字信息区域（上两行标题，底栏来源与热度）
            Column(
                modifier = Modifier
                    .weight(1f)
                    .heightIn(min = thumbHeight),
                verticalArrangement = Arrangement.SpaceBetween
            ) {
                // 上部：排行序号 + 标题
                Row(verticalAlignment = Alignment.Top) {
                    if (rank != null) {
                        Box(
                            modifier = Modifier
                                .padding(top = 2.dp)
                                .size(20.dp)
                                .clip(CircleShape)
                                .background(
                                    if (rank <= 3) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                                ),
                            contentAlignment = Alignment.Center
                        ) {
                            Text(
                                text = "$rank",
                                style = MaterialTheme.typography.labelSmall,
                                color = if (rank <= 3) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        }
                        Spacer(modifier = Modifier.width(8.dp))
                    }

                    Text(
                        text = title,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurface,
                        maxLines = 2,
                        overflow = TextOverflow.Ellipsis
                    )
                }

                // 底部元信息栏：来源 + 时间 + 热度
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = FocalDimens.shapeTag,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = sourceName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = timeAgo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (rank != null && rank <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = hotScoreDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rank != null && rank <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun VectorPlaceholderBox(icon: ImageVector) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.surfaceVariant),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.6f),
            modifier = Modifier.size(28.dp)
        )
    }
}
