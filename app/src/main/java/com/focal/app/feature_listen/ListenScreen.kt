package com.focal.app.feature_listen

import androidx.compose.animation.AnimatedVisibility
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*

/**
 * 「听」听书与播客聚合流：
 * 纯图标悬浮胶囊播放器、背景播放支持、极简列表
 */
@Composable
fun ListenScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    var audioList by remember { mutableStateOf(AudioRepository.getFallbackAudioList()) }
    var currentPlayingItem by remember { mutableStateOf<AudioItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        audioList = AudioRepository.fetchRealAudioList()
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(InkBlack)
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // 顶栏：纯图标操作
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

                // 纯图标分类切换：全部 / 听书 / 播客
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                    Box(
                        modifier = Modifier
                            .size(36.dp)
                            .clip(CircleShape)
                            .background(BambooGreenSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.GraphicEq, // 动态声波
                            contentDescription = null,
                            tint = BambooGreen,
                            modifier = Modifier.size(18.dp)
                        )
                    }
                }
            }

            // 音频列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(start = 16.dp, end = 16.dp, top = 8.dp, bottom = 100.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(audioList, key = { it.id }) { item ->
                    AudioCard(
                        item = item,
                        isCurrentPlaying = currentPlayingItem?.id == item.id && isPlaying,
                        onClick = {
                            if (currentPlayingItem?.id == item.id) {
                                isPlaying = !isPlaying
                            } else {
                                currentPlayingItem = item
                                isPlaying = true
                            }
                        }
                    )
                }
            }
        }

        // 底部常驻悬浮微胶囊播放器（纯图标操作：播放/暂停/快进）
        if (currentPlayingItem != null) {
            Surface(
                modifier = Modifier
                    .align(Alignment.BottomCenter)
                    .padding(horizontal = 16.dp, vertical = 12.dp)
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(20.dp)),
                color = InkSurfaceVariant,
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Box(
                            modifier = Modifier
                                .size(38.dp)
                                .clip(CircleShape)
                                .background(BambooGreenSoft),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(
                                imageVector = Icons.Rounded.Headphones,
                                contentDescription = null,
                                tint = BambooGreen,
                                modifier = Modifier.size(20.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(12.dp))
                        Column {
                            Text(
                                text = currentPlayingItem!!.title,
                                color = MoonWhite,
                                fontSize = 13.sp,
                                maxLines = 1
                            )
                            Text(
                                text = currentPlayingItem!!.narratorOrHost,
                                color = MistGrey,
                                fontSize = 11.sp
                            )
                        }
                    }

                    // 纯图标控制器
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isPlaying = !isPlaying }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                contentDescription = null,
                                tint = BambooGreen,
                                modifier = Modifier.size(34.dp)
                            )
                        }
                        IconButton(onClick = { /* 后跳15秒 */ }) {
                            Icon(
                                imageVector = Icons.Rounded.Forward10,
                                contentDescription = null,
                                tint = MistGrey,
                                modifier = Modifier.size(22.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun AudioCard(
    item: AudioItem,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlaying) InkSurfaceVariant else InkSurface
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(46.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isCurrentPlaying) BambooGreenSoft else InkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Icon(
                    imageVector = if (isCurrentPlaying) Icons.Rounded.Equalizer else Icons.Rounded.PlayArrow,
                    contentDescription = null,
                    tint = if (isCurrentPlaying) BambooGreen else MistGrey,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(modifier = Modifier.width(14.dp))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    color = if (isCurrentPlaying) BambooGreen else MoonWhite,
                    fontSize = 14.sp
                )
                Spacer(modifier = Modifier.height(4.dp))
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(4.dp))
                            .background(BambooGreenSoft)
                            .padding(horizontal = 5.dp, vertical = 1.dp)
                    ) {
                        Text(
                            text = item.platformTag,
                            color = BambooGreen,
                            fontSize = 9.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(6.dp))
                    Text(
                        text = item.narratorOrHost,
                        color = MistGrey,
                        fontSize = 11.sp
                    )
                    Spacer(modifier = Modifier.width(8.dp))
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
