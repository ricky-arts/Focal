package com.focal.app.feature_listen

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
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens

/**
 * Material 3 官方规范「听」界面重构：
 * 1. 严格使用 MaterialTheme.colorScheme 与 FocalTypography
 * 2. 唱片/精美书籍质感缩略图，统一 12dp 卡片圆角，1~2dp 微阴影
 * 3. 悬浮微胶囊播放器采用 M3 Surface 规范设计
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
            .statusBarsPadding()
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
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
                        text = "听",
                        style = MaterialTheme.typography.titleLarge,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                // 纯矢量图标
                Icon(
                    imageVector = Icons.Rounded.GraphicEq,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(FocalDimens.iconSizeStandard)
                )
            }

            // 音频列表
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    start = FocalDimens.pageHorizontalPadding,
                    end = FocalDimens.pageHorizontalPadding,
                    top = FocalDimens.spaceSmall,
                    bottom = 100.dp
                ),
                verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
            ) {
                items(audioList, key = { it.id }) { item ->
                    M3AudioCard(
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

        // 底部常驻悬浮微胶囊播放器（Material 3 高级拟态悬浮卡片）
        AnimatedVisibility(
            visible = currentPlayingItem != null,
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(horizontal = FocalDimens.pageHorizontalPadding, vertical = FocalDimens.spaceMedium),
            enter = slideInVertically(initialOffsetY = { it }) + fadeIn(),
            exit = slideOutVertically(targetOffsetY = { it }) + fadeOut()
        ) {
            Surface(
                modifier = Modifier.fillMaxWidth(),
                shape = FocalDimens.shapePill,
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = FocalDimens.elevationFloat,
                shadowElevation = FocalDimens.elevationCard
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FocalDimens.spaceMedium, vertical = 8.dp),
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.SpaceBetween
                ) {
                    Row(
                        modifier = Modifier.weight(1f),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            modifier = Modifier.size(40.dp),
                            shape = CircleShape,
                            color = MaterialTheme.colorScheme.primaryContainer
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.Headphones,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(20.dp)
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(FocalDimens.spaceMedium))

                        Column {
                            Text(
                                text = currentPlayingItem?.title ?: "",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant,
                                maxLines = 1,
                                overflow = TextOverflow.Ellipsis
                            )
                            Text(
                                text = currentPlayingItem?.narratorOrHost ?: "",
                                style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                            )
                        }
                    }

                    // 纯图标标准控制
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        IconButton(onClick = { isPlaying = !isPlaying }) {
                            Icon(
                                imageVector = if (isPlaying) Icons.Rounded.PauseCircle else Icons.Rounded.PlayCircle,
                                contentDescription = "Play/Pause",
                                tint = MaterialTheme.colorScheme.primary,
                                modifier = Modifier.size(32.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
fun M3AudioCard(
    item: AudioItem,
    isCurrentPlaying: Boolean,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FocalDimens.shapeCard,
        elevation = CardDefaults.cardElevation(defaultElevation = FocalDimens.elevationSubtle),
        colors = CardDefaults.cardColors(
            containerColor = if (isCurrentPlaying) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.35f)
            } else {
                MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
            }
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FocalDimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 唱片/书籍质感缩略图封套
            Surface(
                modifier = Modifier
                    .size(54.dp)
                    .clip(FocalDimens.shapeButton),
                color = if (isCurrentPlaying) MaterialTheme.colorScheme.primaryContainer else MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = FocalDimens.elevationSubtle
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isCurrentPlaying) Icons.Rounded.Equalizer else Icons.Rounded.MenuBook,
                        contentDescription = null,
                        tint = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
            }

            Spacer(modifier = Modifier.width(FocalDimens.spaceMedium))

            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = if (isCurrentPlaying) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )

                Spacer(modifier = Modifier.height(4.dp))

                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        shape = FocalDimens.shapeTag,
                        color = MaterialTheme.colorScheme.secondaryContainer
                    ) {
                        Text(
                            text = item.platformTag,
                            style = MaterialTheme.typography.labelSmall,
                            color = MaterialTheme.colorScheme.onSecondaryContainer,
                            modifier = Modifier.padding(horizontal = 5.dp, vertical = 2.dp)
                        )
                    }

                    Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))

                    Text(
                        text = item.narratorOrHost,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )

                    Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))

                    Text(
                        text = item.playCount,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.7f)
                    )
                }
            }
        }
    }
}
