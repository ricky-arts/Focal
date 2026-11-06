package com.focal.app.feature_set

import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.Article
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

@Composable
fun SettingsDrawerContent(
    prefStore: FocalPreferenceStore,
    onOpenAuthBridge: (platformKey: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    var wallpaperKey by remember { mutableStateOf(prefStore.getWallpaperKey()) }
    var opacity by remember { mutableFloatStateOf(prefStore.getWallpaperOpacity()) }
    var contrast by remember { mutableFloatStateOf(prefStore.getWallpaperContrast()) }
    var hasCustomImage by remember { mutableStateOf(prefStore.getCustomWallpaperPath() != null) }

    val photoPickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri: Uri? ->
        if (uri != null) {
            val success = prefStore.saveCustomWallpaperFromUri(uri)
            if (success) {
                wallpaperKey = "custom"
                hasCustomImage = true
            }
        }
    }

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
                        text = "设置与账号中心",
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
                // 1. 账号绑定中心 (Account Hub)
                item {
                    Text(
                        text = "平台账号绑定与授权托管",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    listOf(
                        "知乎账号 (Zhihu)" to "zhihu",
                        "哔哩哔哩 (Bilibili)" to "bilibili",
                        "微博账号 (Weibo)" to "weibo"
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

                // 2. 背景视觉工坊
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

                            Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                            FilledTonalButton(
                                onClick = { photoPickerLauncher.launch("image/*") },
                                shape = FocalDimens.shapeButton,
                                modifier = Modifier.fillMaxWidth()
                            ) {
                                Icon(Icons.Rounded.AddPhotoAlternate, contentDescription = null, modifier = Modifier.size(FocalDimens.iconSizeSmall))
                                Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                                Text(
                                    text = if (hasCustomImage && wallpaperKey == "custom") "已应用自定义图片 (更换)" else "从手机相册选取背景图",
                                    style = MaterialTheme.typography.bodyMedium
                                )
                            }

                            Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("背景透光度", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text("${(opacity * 100).toInt()}%", style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = opacity,
                                onValueChange = {
                                    opacity = it
                                    prefStore.setWallpaperOpacity(it)
                                },
                                valueRange = 0.1f..1.0f
                            )

                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text("对比度调校", style = MaterialTheme.typography.bodyMedium, color = MaterialTheme.colorScheme.onSurface)
                                Text(String.format("%.1fx", contrast), style = MaterialTheme.typography.bodySmall, color = MaterialTheme.colorScheme.primary)
                            }
                            Slider(
                                value = contrast,
                                onValueChange = {
                                    contrast = it
                                    prefStore.setWallpaperContrast(it)
                                },
                                valueRange = 0.5f..1.5f
                            )
                        }
                    }
                }

                // 3. 资讯感兴趣栏目定制
                item {
                    Text(
                        text = "资讯感兴趣栏目定制",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    NewsCategory.values().forEach { cat ->
                        M3CategoryToggleItem(cat = cat, prefStore = prefStore)
                        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
                    }
                }

                // 4. 平台数据源开关
                item {
                    Text(
                        text = "数据源启用与屏蔽",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                    listOf(
                        Triple(Icons.AutoMirrored.Rounded.Article, "今日头条 (Toutiao)", "toutiao"),
                        Triple(Icons.Rounded.Whatshot, "微博热搜 (Weibo)", "weibo"),
                        Triple(Icons.Rounded.Search, "百度热搜 (Baidu)", "baidu"),
                        Triple(Icons.Rounded.Lightbulb, "知乎热榜 (Zhihu)", "zhihu"),
                        Triple(Icons.Rounded.SmartDisplay, "B站热门 (Bilibili)", "bilibili"),
                        Triple(Icons.Rounded.Memory, "IT之家 (ITHOME)", "ithome"),
                        Triple(Icons.Rounded.ShowChart, "36氪创投 (36KR)", "36kr"),
                        Triple(Icons.Rounded.EditNote, "少数派专栏 (SSPAI)", "sspai")
                    ).forEach { (icon, name, key) ->
                        M3SourceToggleItem(icon = icon, name = name, key = key, prefStore = prefStore)
                        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
                    }
                }
            }
        }
    }
}

@Composable
fun M3CategoryToggleItem(cat: NewsCategory, prefStore: FocalPreferenceStore) {
    var enabled by remember { mutableStateOf(prefStore.isCategoryEnabled(cat.key, true)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
fun M3SourceToggleItem(icon: ImageVector, name: String, key: String, prefStore: FocalPreferenceStore) {
    var enabled by remember { mutableStateOf(prefStore.isSourceEnabled(key, true)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                Spacer(modifier = Modifier.width(10.dp))
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
fun M3AuthPlatformItem(name: String, platformKey: String, prefStore: FocalPreferenceStore, onConnect: () -> Unit) {
    var isAuthed by remember { mutableStateOf(prefStore.isAiAuthenticated(platformKey)) }

    Card(
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f))
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
                        .background(if (isAuthed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
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
                    Text("绑定授权", style = MaterialTheme.typography.labelSmall)
                }
            }
        }
    }
}
