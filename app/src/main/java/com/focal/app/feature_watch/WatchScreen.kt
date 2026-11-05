package com.focal.app.feature_watch

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*

/**
 * 「视」长短视频聚合（抖音 + B站）：
 * 卡片沉浸式视频流，纯图标控制（播放、原链、全屏手势）
 */
@Composable
fun WatchScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var videoList by remember { mutableStateOf(VideoRepository.getFallbackFeeds()) }
    var selectedFilter by remember { mutableStateOf("ALL") }

    LaunchedEffect(Unit) {
        videoList = VideoRepository.fetchRealVideoFeeds()
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InkBlack)
            .statusBarsPadding()
    ) {
        // 顶栏：抽屉图标与平台筛选图标
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            IconButton(onClick = onOpenDrawer) {
                Icon(
                    imageVector = Icons.Rounded.Menu,
                    contentDescription = null,
                    tint = MoonWhite
                )
            }

            // 纯图标筛选：全部 / 抖音(短) / B站(长)
            Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedFilter == "ALL") BambooGreenSoft else InkSurface)
                        .clickable { selectedFilter = "ALL" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AllInclusive,
                        contentDescription = null,
                        tint = if (selectedFilter == "ALL") BambooGreen else MistGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedFilter == "DY") BambooGreenSoft else InkSurface)
                        .clickable { selectedFilter = "DY" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Smartphone, // 抖音竖屏短视频
                        contentDescription = null,
                        tint = if (selectedFilter == "DY") BambooGreen else MistGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }

                Box(
                    modifier = Modifier
                        .size(34.dp)
                        .clip(CircleShape)
                        .background(if (selectedFilter == "BILI") BambooGreenSoft else InkSurface)
                        .clickable { selectedFilter = "BILI" },
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.Tv, // B站横屏长视频
                        contentDescription = null,
                        tint = if (selectedFilter == "BILI") BambooGreen else MistGrey,
                        modifier = Modifier.size(16.dp)
                    )
                }
            }
        }

        val filteredVideos = remember(selectedFilter, videoList) {
            if (selectedFilter == "ALL") videoList else videoList.filter { it.platformTag == selectedFilter }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            items(filteredVideos, key = { it.id }) { item ->
                VideoCard(
                    item = item,
                    onPlay = {
                        val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.videoUrl))
                        context.startActivity(intent)
                    }
                )
            }
        }
    }
}

@Composable
fun VideoCard(
    item: VideoItem,
    onPlay: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .clickable(onClick = onPlay),
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(modifier = Modifier.fillMaxWidth()) {
            // 视频虚拟封面区域（带中央播放纯图标）
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .background(InkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                // 封面意象符号
                Text(
                    text = item.coverEmoji,
                    fontSize = 48.sp
                )

                // 纯图标半透明毛玻璃播放按钮
                Box(
                    modifier = Modifier
                        .size(52.dp)
                        .clip(CircleShape)
                        .background(InkBlack.copy(alpha = 0.6f)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.PlayArrow,
                        contentDescription = null,
                        tint = MoonWhite,
                        modifier = Modifier.size(30.dp)
                    )
                }

                // 左上角平台微标
                Box(
                    modifier = Modifier
                        .align(Alignment.TopStart)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(InkBlack.copy(alpha = 0.75f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        text = item.platformTag,
                        color = if (item.platformTag == "DY") CinnabarRed else PorcelainBlue,
                        fontSize = 10.sp
                    )
                }

                // 右下角时长微标
                Box(
                    modifier = Modifier
                        .align(Alignment.BottomEnd)
                        .padding(10.dp)
                        .clip(RoundedCornerShape(6.dp))
                        .background(InkBlack.copy(alpha = 0.75f))
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text(
                        text = item.duration,
                        color = MoonWhite,
                        fontSize = 10.sp
                    )
                }
            }

            // 视频信息
            Column(modifier = Modifier.padding(14.dp)) {
                Text(
                    text = item.title,
                    color = MoonWhite,
                    fontSize = 15.sp,
                    lineHeight = 20.sp
                )
                Spacer(modifier = Modifier.height(8.dp))
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = item.author,
                        color = MistGrey,
                        fontSize = 12.sp
                    )
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.Visibility,
                            contentDescription = null,
                            tint = MistGrey,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = item.playCount,
                            color = MistGrey,
                            fontSize = 11.sp
                        )
                    }
                }
            }
        }
    }
}
