package com.focal.app.feature_listen

import androidx.compose.animation.*
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import com.focal.app.core.ui.UnifiedMediaCard
import kotlinx.coroutines.launch

/**
 * Material 3 规范「听」听书/播客板块优化：
 * 1. 采用全板块统一左图右文卡片 (UnifiedMediaCard)，展示番茄/喜马/播客书籍与专辑缩略图；
 * 2. 支持下拉刷新与上滑触底分页加载更多；
 * 3. 悬浮微胶囊播放器采用 M3 Surface 拟态设计。
 */
@Composable
fun ListenScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    var audioList by remember { mutableStateOf<List<AudioItem>>(emptyList()) }
    var currentPlayingItem by remember { mutableStateOf<AudioItem?>(null) }
    var isPlaying by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun refreshAudio() {
        coroutineScope.launch {
            isLoading = true
            currentPage = 1
            hasMore = true
            val items = AudioRepository.fetchRealAudioList(page = 1)
            audioList = items
            isLoading = false
        }
    }

    fun loadMoreAudio() {
        if (isLoadingMore || !hasMore) return
        coroutineScope.launch {
            isLoadingMore = true
            val nextPage = currentPage + 1
            val more = AudioRepository.fetchRealAudioList(page = nextPage)
            if (more.isEmpty()) {
                hasMore = false
            } else {
                currentPage = nextPage
                audioList = audioList + more
            }
            isLoadingMore = false
        }
    }

    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisible = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisible >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && !isLoadingMore && hasMore) {
            loadMoreAudio()
        }
    }

    LaunchedEffect(Unit) {
        refreshAudio()
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

                IconButton(onClick = { refreshAudio() }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = "Refresh",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
            }

            // 音频列表与分页
            AnimatedContent(
                targetState = isLoading && audioList.isEmpty(),
                transitionSpec = { fadeIn() togetherWith fadeOut() },
                label = "audioLoadingTransition"
            ) { initialLoading ->
                if (initialLoading) {
                    Column(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(horizontal = FocalDimens.pageHorizontalPadding),
                        verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
                    ) {
                        repeat(6) {
                            Card(
                                modifier = Modifier
                                    .fillMaxWidth()
                                    .height(84.dp),
                                shape = FocalDimens.shapeCard,
                                colors = CardDefaults.cardColors(
                                    containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
                                )
                            ) {}
                        }
                    }
                } else if (audioList.isEmpty()) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding(FocalDimens.spaceLarge),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Icon(
                                imageVector = Icons.Rounded.ErrorOutline,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant,
                                modifier = Modifier.size(48.dp)
                            )
                            Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                            Text(
                                text = "音频节目加载失败或网络受限",
                                style = MaterialTheme.typography.bodyMedium,
                                color = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                            Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                            FilledTonalButton(onClick = { refreshAudio() }) {
                                Text("重新连接")
                            }
                        }
                    }
                } else {
                    LazyColumn(
                        state = listState,
                        modifier = Modifier.fillMaxSize(),
                        contentPadding = PaddingValues(
                            start = FocalDimens.pageHorizontalPadding,
                            end = FocalDimens.pageHorizontalPadding,
                            top = FocalDimens.spaceSmall,
                            bottom = 100.dp
                        ),
                        verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
                    ) {
                        itemsIndexed(audioList, key = { _, item -> item.id }) { _, item ->
                            val isCurrent = currentPlayingItem?.id == item.id && isPlaying
                            UnifiedMediaCard(
                                title = item.title,
                                sourceName = item.platformTag,
                                timeAgo = item.duration,
                                hotScoreDisplay = item.playCount,
                                thumbnailUrl = item.thumbnailUrl,
                                isVideo = false,
                                isGallery = false,
                                fallbackIcon = Icons.Rounded.Headphones,
                                thumbWidth = 76.dp,
                                thumbHeight = 76.dp,
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

                        // 触底分页加载提示
                        item {
                            if (isLoadingMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = FocalDimens.spaceMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        CircularProgressIndicator(
                                            modifier = Modifier.size(18.dp),
                                            strokeWidth = 2.dp,
                                            color = MaterialTheme.colorScheme.primary
                                        )
                                        Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                                        Text(
                                            text = "正在调取更多热门有声剧与播客...",
                                            style = MaterialTheme.typography.bodySmall,
                                            color = MaterialTheme.colorScheme.onSurfaceVariant
                                        )
                                    }
                                }
                            } else if (!hasMore) {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .padding(vertical = FocalDimens.spaceMedium),
                                    contentAlignment = Alignment.Center
                                ) {
                                    Text(
                                        text = "— 已加载全部有声书与播客节目 —",
                                        style = MaterialTheme.typography.bodySmall,
                                        color = MaterialTheme.colorScheme.onSurfaceVariant.copy(alpha = 0.6f)
                                    )
                                }
                            }
                        }
                    }
                }
            }
        }

        // 底部常驻悬浮微胶囊播放器
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
