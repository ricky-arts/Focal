package com.focal.app.feature_set

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
import androidx.compose.material.icons.automirrored.rounded.MenuBook
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import com.focal.app.feature_read.NewsCategory

/**
 * Material 3 规范侧边独立设置抽屉重构：
 * 1. 严格使用 MaterialTheme.colorScheme 与 FocalTypography
 * 2. 增加视觉画境配置：壁纸选择、透明度滑块 (0.0~1.0)、对比度滑块 (0.5~1.5)
 * 3. 规范圆角 (12dp 卡片, 8dp 按钮, 4dp 标签) 与标准 24dp 矢量图标
 */
@Composable
fun SettingsDrawerContent(
    prefStore: FocalPreferenceStore,
    onOpenAuthBridge: (platformKey: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var wallpaperKey by remember { mutableStateOf(prefStore.getWallpaperKey()) }
    var opacity by remember { mutableFloatStateOf(prefStore.getWallpaperOpacity()) }
    var contrast by remember { mutableFloatStateOf(prefStore.getWallpaperContrast()) }

    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(340.dp),
        color = MaterialTheme.colorScheme.surface,
        tonalElevation = FocalDimens.elevationFloat
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(FocalDimens.spaceMedium)
        ) {
            // 顶栏：关闭与标题
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        imageVector = Icons.Rounded.Tune,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                    Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                    Text(
                        text = "设置与画境",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Rounded.Close,
                        contentDescription = "Close",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
            }

            Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceMedium)
            ) {
                // 1. 背景视觉工坊 (Wallpaper & Contrast)
                item {
                    Text(
                        text = "背景壁纸与滤镜微调",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    Card(
                        shape = FocalDimens.shapeCard,
                        colors = CardDefaults.cardColors(
                            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
                        )
                    ) {
                        Column(modifier = Modifier.padding(FocalDimens.spaceMedium)) {
                            // 壁纸主题预设选择
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
                            ) {
                                listOf(
                                    "default" to "默认",
                                    "bamboo" to "竹影",
                                    "watertown" to "水乡",
                                    "sunset" to "落日",
                                    "none" to "纯粹"
                                ).forEach { (key, label) ->
                                    val isSelected = wallpaperKey == key
                                    FilterChip(
                                        selected = isSelected,
                                        onClick = {
                                            wallpaperKey = key
                                            prefStore.setWallpaperKey(key)
                                        },
                                        shape = FocalDimens.shapeTag,
                                        label = { Text(label, style = MaterialTheme.typography.bodySmall) },
                                        colors = FilterChipDefaults.filterChipColors(
                                            selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                                            selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer
                                        )
                                    )
                                }
                            }

                            Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))

                            // 透明度滑块 (0.1 ~ 1.0)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "背景透光度",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = "${(opacity * 100).toInt()}%",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = opacity,
                                onValueChange = {
                                    opacity = it
                                    prefStore.setWallpaperOpacity(it)
                                },
                                valueRange = 0.1f..1.0f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )

                            // 对比度滑块 (0.5 ~ 1.5)
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "对比度调校",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = MaterialTheme.colorScheme.onSurface
                                )
                                Text(
                                    text = String.format("%.1fx", contrast),
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                            Slider(
                                value = contrast,
                                onValueChange = {
                                    contrast = it
                                    prefStore.setWallpaperContrast(it)
                                },
                                valueRange = 0.5f..1.5f,
                                colors = SliderDefaults.colors(
                                    thumbColor = MaterialTheme.colorScheme.primary,
                                    activeTrackColor = MaterialTheme.colorScheme.primary
                                )
                            )
                        }
                    }
                }

                // 2. 资讯感兴趣栏目定制
                item {
                    Text(
                        text = "资讯栏目定制",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    NewsCategory.values().forEach { cat ->
                        M3CategoryToggleItem(
                            cat = cat,
                            prefStore = prefStore
                        )
                        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
                    }
                }

                // 3. 全网聚合内容源开关
                item {
                    Text(
                        text = "全网聚合源启用",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    listOf(
                        Triple(Icons.AutoMirrored.Rounded.Article, "今日头条 (Toutiao)", "toutiao"),
                        Triple(Icons.Rounded.Whatshot, "微博热搜 (Weibo)", "weibo"),
                        Triple(Icons.Rounded.Search, "百度热搜 (Baidu)", "baidu"),
                        Triple(Icons.Rounded.Lightbulb, "知乎热榜 (Zhihu)", "zhihu"),
                        Triple(Icons.Rounded.Public, "国际资讯 (Reddit/HN)", "reddit"),
                        Triple(Icons.Rounded.SmartDisplay, "哔哩哔哩 (Bilibili)", "video_bilibili"),
                        Triple(Icons.Rounded.MusicVideo, "抖音精选 (Douyin)", "video_douyin"),
                        Triple(Icons.AutoMirrored.Rounded.MenuBook, "番茄畅听 / 有声书", "audio_fanqie"),
                        Triple(Icons.Rounded.Podcasts, "喜马拉雅与开放播客", "audio_podcast")
                    ).forEach { (icon, name, key) ->
                        M3SourceToggleItem(
                            icon = icon,
                            name = name,
                            key = key,
                            prefStore = prefStore
                        )
                        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
                    }
                }

                // 4. AI 身份对接持久化
                item {
                    Text(
                        text = "AI 身份对接持久化",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    listOf(
                        "豆包 (Doubao)" to "doubao",
                        "Kimi 智能助手" to "kimi",
                        "通义千问 (Qwen)" to "qwen",
                        "腾讯元宝 (Yuanbao)" to "yuanbao"
                    ).forEach { (name, key) ->
                        M3AuthPlatformItem(
                            name = name,
                            platformKey = key,
                            prefStore = prefStore,
                            onConnect = { onOpenAuthBridge(key) }
                        )
                        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
                    }
                }
            }
        }
    }
}

@Composable
fun M3CategoryToggleItem(
    cat: NewsCategory,
    prefStore: FocalPreferenceStore
) {
    var enabled by remember { mutableStateOf(prefStore.isCategoryEnabled(cat.key, true)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FocalDimens.spaceMedium, vertical = FocalDimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = cat.icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FocalDimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                Text(
                    text = cat.displayName,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    prefStore.setCategoryEnabled(cat.key, it)
                }
            )
        }
    }
}

@Composable
fun M3SourceToggleItem(
    icon: ImageVector,
    name: String,
    key: String,
    prefStore: FocalPreferenceStore
) {
    var enabled by remember { mutableStateOf(prefStore.isSourceEnabled(key, true)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FocalDimens.spaceMedium, vertical = FocalDimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = if (enabled) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FocalDimens.iconSizeSmall)
                )
                Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = if (enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
            Switch(
                checked = enabled,
                onCheckedChange = {
                    enabled = it
                    prefStore.setSourceEnabled(key, it)
                }
            )
        }
    }
}

@Composable
fun M3AuthPlatformItem(
    name: String,
    platformKey: String,
    prefStore: FocalPreferenceStore,
    onConnect: () -> Unit
) {
    var isAuthed by remember { mutableStateOf(prefStore.isAiAuthenticated(platformKey)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FocalDimens.spaceMedium, vertical = FocalDimens.spaceSmall),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(if (isAuthed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error)
                )
                Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                Text(
                    text = name,
                    style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (isAuthed) {
                IconButton(
                    onClick = {
                        prefStore.clearAiAuthSession(platformKey)
                        isAuthed = false
                    }
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LinkOff,
                        contentDescription = "Disconnect",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FocalDimens.iconSizeSmall)
                    )
                }
            } else {
                FilledTonalButton(
                    onClick = onConnect,
                    shape = FocalDimens.shapeButton,
                    contentPadding = PaddingValues(horizontal = 12.dp, vertical = 4.dp),
                    modifier = Modifier.height(32.dp)
                ) {
                    Text(
                        text = "对接",
                        style = MaterialTheme.typography.labelSmall
                    )
                }
            }
        }
    }
}
