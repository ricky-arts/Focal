package com.focal.app.feature_watch

import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focal.app.core.content.ContentType
import com.focal.app.core.content.NativeContentExtractor
import com.focal.app.core.content.StructuredContent
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import com.focal.app.core.ui.UnifiedMediaCard
import kotlinx.coroutines.launch

/**
 * 优化重构「视」视频聚合板块：
 * 1. 采用全板块统一左图右文卡片 (UnifiedMediaCard)，视频自动叠放播放微标；
 * 2. 深度抓取 B 站各分区与抖音热榜，提取官方高清 cover 缩略图；
 * 3. 支持下拉刷新与上滑触底自动分页加载更多；
 * 4. 点击拉起 App 内原生 ExoPlayer 播放器页面，完全不使用 WebView。
 */
@Composable
fun WatchScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit,
    onOpenDetail: (StructuredContent) -> Unit
) {
    var videoList by remember { mutableStateOf<List<VideoItem>>(emptyList()) }
    var selectedFilter by remember { mutableStateOf("ALL") }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun refreshVideos() {
        coroutineScope.launch {
            isLoading = true
            currentPage = 1
            hasMore = true
            val items = VideoRepository.fetchRealVideoFeeds(page = 1, filter = selectedFilter)
            videoList = items
            isLoading = false
        }
    }

    fun loadMoreVideos() {
        if (isLoadingMore || !hasMore) return
        coroutineScope.launch {
            isLoadingMore = true
            val nextPage = currentPage + 1
            val more = VideoRepository.fetchRealVideoFeeds(page = nextPage, filter = selectedFilter)
            if (more.isEmpty()) {
                hasMore = false
            } else {
                currentPage = nextPage
                videoList = videoList + more
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
            loadMoreVideos()
        }
    }

    LaunchedEffect(selectedFilter) {
        refreshVideos()
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

        // 视频列表与分页
        AnimatedContent(
            targetState = isLoading && videoList.isEmpty(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "videoLoadingTransition"
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
            } else if (videoList.isEmpty()) {
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
                            text = "未获取到该分类视频或网络受限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                        FilledTonalButton(onClick = { refreshVideos() }) {
                            Text("重新拉取")
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
                        bottom = FocalDimens.spaceLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
                ) {
                    itemsIndexed(videoList, key = { _, item -> item.id }) { index, item ->
                        UnifiedMediaCard(
                            title = item.title,
                            sourceName = if (item.platformTag == "BILI") "哔哩哔哩" else "抖音精选",
                            timeAgo = item.duration,
                            hotScoreDisplay = item.playCount,
                            subtitleExtra = item.danmakuCount ?: item.author,
                            thumbnailUrl = item.thumbnailUrl,
                            isVideo = true,
                            fallbackIcon = Icons.Rounded.VideoLibrary,
                            onClick = {
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

                    // 触底加载指示
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
                                        text = "正在检索更多精彩视频流...",
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
                                    text = "— 已加载全部视频 —",
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
