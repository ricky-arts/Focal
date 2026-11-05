package com.focal.app.feature_watch

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.content.ContentType
import com.focal.app.core.content.NativeContentExtractor
import com.focal.app.core.content.StructuredContent
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import kotlinx.coroutines.launch

/**
 * Material 3 官方规范「视」视频聚合重构：
 * 1. 严格使用 MaterialTheme.colorScheme 与 FocalTypography
 * 2. 16:9 标准比例视频缩略图，优雅卡片 12dp 圆角，1~2dp 微阴影
 * 3. 规范的 24dp 官方矢量图标与 8/16dp 间距模数
 * 4. 点击拉起 App 内原生 ExoPlayer 播放器页面，彻底移除外部浏览器
 */
@Composable
fun WatchScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit,
    onOpenDetail: (StructuredContent) -> Unit
) {
    var videoList by remember { mutableStateOf(VideoRepository.getFallbackFeeds()) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    val coroutineScope = rememberCoroutineScope()

    LaunchedEffect(Unit) {
        videoList = VideoRepository.fetchRealVideoFeeds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .statusBarsPadding()
    ) {
        // M3 顶栏
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FocalDimens.pageHorizontalPadding, vertical = FocalDimens.spaceSmall),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = "Menu",
                        tint = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
                Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                Text(
                    text = "视",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            // 纯图标 M3 FilterChips (全部 / 抖音短视频 / B站长视频)
            Row(horizontalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)) {
                M3WatchFilterChip(
                    isSelected = selectedFilter == "ALL",
                    icon = Icons.Rounded.AllInclusive,
                    label = "全部",
                    onClick = { selectedFilter = "ALL" }
                )
                M3WatchFilterChip(
                    isSelected = selectedFilter == "DY",
                    icon = Icons.Rounded.Smartphone,
                    label = "短视频",
                    onClick = { selectedFilter = "DY" }
                )
                M3WatchFilterChip(
                    isSelected = selectedFilter == "BILI",
                    icon = Icons.Rounded.Tv,
                    label = "长视频",
                    onClick = { selectedFilter = "BILI" }
                )
            }
        }

        val filteredVideos = remember(selectedFilter, videoList) {
            if (selectedFilter == "ALL") videoList else videoList.filter { it.platformTag == selectedFilter }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(
                start = FocalDimens.pageHorizontalPadding,
                end = FocalDimens.pageHorizontalPadding,
                top = FocalDimens.spaceSmall,
                bottom = FocalDimens.spaceLarge
            ),
            verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceMedium)
        ) {
            items(filteredVideos, key = { it.id }) { item ->
                M3VideoCard(
                    item = item,
                    onPlay = {
                        coroutineScope.launch {
                            val result = NativeContentExtractor.extractContent(
                                id = item.id,
                                title = item.title,
                                sourceName = if (item.platformTag == "BILI") "哔哩哔哩" else "抖音精选",
                                sourceTag = item.platformTag,
                                targetUrl = item.videoUrl,
                                preferredType = ContentType.VIDEO
                            )
                            result.onSuccess { structured ->
                                onOpenDetail(structured)
                            }
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun M3WatchFilterChip(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    label: String,
    onClick: () -> Unit
) {
    FilterChip(
        selected = isSelected,
        onClick = onClick,
        shape = FocalDimens.shapePill,
        leadingIcon = {
            Icon(
                imageVector = icon,
                contentDescription = null,
                modifier = Modifier.size(FocalDimens.iconSizeSmall)
            )
        },
        label = {
            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall
            )
        },
        colors = FilterChipDefaults.filterChipColors(
            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
            selectedLeadingIconColor = MaterialTheme.colorScheme.onPrimaryContainer,
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
            labelColor = MaterialTheme.colorScheme.onSurfaceVariant,
            iconColor = MaterialTheme.colorScheme.onSurfaceVariant
        ),
        border = FilterChipDefaults.filterChipBorder(
            enabled = true,
            selected = isSelected,
            borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
            selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
        )
    )
}

@Composable
fun M3VideoCard(
    item: VideoItem,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onPlay),
        shape = FocalDimens.shapeCard,
        elevation = CardDefaults.cardElevation(defaultElevation = FocalDimens.elevationCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(16f / 9f)
                    .background(MaterialTheme.colorScheme.surfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = item.coverEmoji,
                    style = MaterialTheme.typography.titleLarge.copy(fontSize = 42.sp)
                )

                Box(
                    modifier = Modifier
                        .size(48.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.5f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = "Play",
                        tint = MaterialTheme.colorScheme.onPrimary,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(FocalDimens.spaceSmall),
                    shape = FocalDimens.shapeTag,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = item.platformTag,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }

                Surface(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(FocalDimens.spaceSmall),
                    shape = FocalDimens.shapeTag,
                    color = MaterialTheme.colorScheme.surface.copy(alpha = 0.8f)
                ) {
                    Text(
                        text = item.duration,
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurface,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Column(modifier = Modifier.padding(FocalDimens.spaceMedium)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.author,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.playCount,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }
            }
        }
    }
}
