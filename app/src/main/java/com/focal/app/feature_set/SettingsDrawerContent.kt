package com.focal.app.feature_set

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*
import com.focal.app.feature_read.NewsCategory

/**
 * 侧边独立展开的设置面板：
 * 纯图标与极简意象，包含：
 * 1. 资讯栏目偏好定制（综合、娱乐、体育、科技、财经、国际）
 * 2. 全网平台源开关（今日头条、微博、百度、知乎、抖音、B站、番茄畅听、喜马拉雅等）
 * 3. 身份对接工具与状态管理（豆包、Kimi、千问、元宝）
 */
@Composable
fun SettingsDrawerContent(
    prefStore: FocalPreferenceStore,
    onOpenAuthBridge: (platformKey: String) -> Unit,
    onCloseDrawer: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxHeight()
            .width(330.dp),
        color = InkSurface,
        tonalElevation = 8.dp
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .padding(20.dp)
        ) {
            // 顶栏：关闭图标与精简标识
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BambooGreenSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Tune,
                            contentDescription = null,
                            tint = BambooGreen,
                            modifier = Modifier.size(20.dp)
                        )
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(
                        text = "设",
                        color = MoonWhite,
                        fontSize = 18.sp
                    )
                }

                IconButton(onClick = onCloseDrawer) {
                    Icon(
                        imageVector = Icons.Rounded.ChevronLeft,
                        contentDescription = null,
                        tint = MistGrey
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // 1. 用户自定义关心的资讯栏目
                item {
                    Text(
                        text = "资讯感兴趣栏目定制",
                        color = MistGrey,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    NewsCategory.values().forEach { cat ->
                        CategoryToggleItem(
                            cat = cat,
                            prefStore = prefStore
                        )
                        Spacer(modifier = Modifier.height(6.dp))
                    }
                }

                // 2. 全网聚合内容源开关
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "全网聚合源启用",
                        color = MistGrey,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    SourceToggleItem(
                        icon = Icons.Rounded.Article,
                        name = "今日头条 (Toutiao)",
                        key = "toutiao",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.Whatshot,
                        name = "微博热搜 (Weibo)",
                        key = "weibo",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.Search,
                        name = "百度热搜 (Baidu)",
                        key = "baidu",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.Lightbulb,
                        name = "知乎热榜 (Zhihu)",
                        key = "zhihu",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.Public,
                        name = "国际热点 (Reddit/HN)",
                        key = "reddit",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.SmartDisplay,
                        name = "哔哩哔哩 (Bilibili)",
                        key = "video_bilibili",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.MusicVideo,
                        name = "抖音精选 (Douyin)",
                        key = "video_douyin",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.MenuBook,
                        name = "番茄畅听 / 有声书",
                        key = "audio_fanqie",
                        prefStore = prefStore
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    SourceToggleItem(
                        icon = Icons.Rounded.Podcasts,
                        name = "喜马拉雅与开放播客",
                        key = "audio_podcast",
                        prefStore = prefStore
                    )
                }

                // 3. 身份对接工具 (针对强校验的 AI 平台)
                item {
                    Spacer(modifier = Modifier.height(4.dp))
                    Text(
                        text = "AI 身份对接持久化",
                        color = MistGrey,
                        fontSize = 12.sp,
                        modifier = Modifier.padding(bottom = 8.dp)
                    )

                    AuthPlatformItem(
                        name = "豆包 (Doubao)",
                        platformKey = "doubao",
                        prefStore = prefStore,
                        onConnect = { onOpenAuthBridge("doubao") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AuthPlatformItem(
                        name = "Kimi 智能助手",
                        platformKey = "kimi",
                        prefStore = prefStore,
                        onConnect = { onOpenAuthBridge("kimi") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AuthPlatformItem(
                        name = "通义千问 (Qwen)",
                        platformKey = "qwen",
                        prefStore = prefStore,
                        onConnect = { onOpenAuthBridge("qwen") }
                    )
                    Spacer(modifier = Modifier.height(6.dp))
                    AuthPlatformItem(
                        name = "腾讯元宝 (Yuanbao)",
                        platformKey = "yuanbao",
                        prefStore = prefStore,
                        onConnect = { onOpenAuthBridge("yuanbao") }
                    )
                }
            }
        }
    }
}

@Composable
fun CategoryToggleItem(
    cat: NewsCategory,
    prefStore: FocalPreferenceStore
) {
    var enabled by remember { mutableStateOf(prefStore.isCategoryEnabled(cat.key, true)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = cat.icon,
                contentDescription = null,
                tint = if (enabled) BambooGreen else MistGrey,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = cat.displayName,
                color = if (enabled) MoonWhite else MistGrey,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                prefStore.setCategoryEnabled(cat.key, it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MoonWhite,
                checkedTrackColor = BambooGreen,
                uncheckedThumbColor = MistGrey,
                uncheckedTrackColor = InkBorder
            )
        )
    }
}

@Composable
fun SourceToggleItem(
    icon: ImageVector,
    name: String,
    key: String,
    prefStore: FocalPreferenceStore
) {
    var enabled by remember { mutableStateOf(prefStore.isSourceEnabled(key, true)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (enabled) BambooGreen else MistGrey,
                modifier = Modifier.size(18.dp)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                color = if (enabled) MoonWhite else MistGrey,
                fontSize = 13.sp
            )
        }
        Switch(
            checked = enabled,
            onCheckedChange = {
                enabled = it
                prefStore.setSourceEnabled(key, it)
            },
            colors = SwitchDefaults.colors(
                checkedThumbColor = MoonWhite,
                checkedTrackColor = BambooGreen,
                uncheckedThumbColor = MistGrey,
                uncheckedTrackColor = InkBorder
            )
        )
    }
}

@Composable
fun AuthPlatformItem(
    name: String,
    platformKey: String,
    prefStore: FocalPreferenceStore,
    onConnect: () -> Unit
) {
    var isAuthed by remember { mutableStateOf(prefStore.isAiAuthenticated(platformKey)) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(InkSurfaceVariant)
            .padding(horizontal = 14.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            // 状态指示圆点
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(if (isAuthed) BambooGreen else CinnabarRed)
            )
            Spacer(modifier = Modifier.width(10.dp))
            Text(
                text = name,
                color = MoonWhite,
                fontSize = 13.sp
            )
        }

        Row(verticalAlignment = Alignment.CenterVertically) {
            if (isAuthed) {
                IconButton(
                    onClick = {
                        prefStore.clearAiAuthSession(platformKey)
                        isAuthed = false
                    },
                    modifier = Modifier.size(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.LinkOff,
                        contentDescription = null,
                        tint = MistGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            } else {
                Button(
                    onClick = onConnect,
                    shape = RoundedCornerShape(8.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BambooGreenSoft),
                    contentPadding = PaddingValues(horizontal = 10.dp, vertical = 4.dp),
                    modifier = Modifier.height(28.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Key,
                        contentDescription = null,
                        tint = BambooGreen,
                        modifier = Modifier.size(14.dp)
                    )
                    Spacer(modifier = Modifier.width(4.dp))
                    Text(text = "对接", fontSize = 11.sp, color = BambooGreen)
                }
            }
        }
    }
}
