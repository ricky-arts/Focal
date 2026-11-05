package com.focal.app.feature_read

import androidx.compose.animation.*
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.rememberScrollState
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
 * 优化重构「阅」资讯板块：
 * 1. 采用全板块统一左图右文卡片 (UnifiedMediaCard)，优先展示各源首图/封面缩略图；
 * 2. 支持下拉刷新 (Refresh) 与上滑触底自动加载更多 (Pagination)；
 * 3. 规整 M3 规范，数据拉取失败或为空时给出友好交互与一键重试。
 */
@Composable
fun ReadScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit,
    onOpenDetail: (StructuredContent) -> Unit
) {
    var feeds by remember { mutableStateOf<List<NewsItem>>(emptyList()) }
    var selectedCategory by remember { mutableStateOf<NewsCategory?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    // 初始或刷新数据
    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            currentPage = 1
            hasMore = true
            val items = NewsRepository.fetchUnifiedRankings(prefStore, page = 1)
            feeds = items
            isLoading = false
        }
    }

    // 分页加载更多
    fun loadMoreData() {
        if (isLoadingMore || !hasMore) return
        coroutineScope.launch {
            isLoadingMore = true
            val nextPage = currentPage + 1
            val moreItems = NewsRepository.fetchUnifiedRankings(prefStore, page = nextPage)
            if (moreItems.isEmpty()) {
                hasMore = false
            } else {
                currentPage = nextPage
                feeds = feeds + moreItems
            }
            isLoadingMore = false
        }
    }

    // 触底监听自动触发加载更多
    val shouldLoadMore = remember {
        derivedStateOf {
            val totalItems = listState.layoutInfo.totalItemsCount
            val lastVisibleItemIndex = listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: 0
            totalItems > 0 && lastVisibleItemIndex >= totalItems - 2
        }
    }

    LaunchedEffect(shouldLoadMore.value) {
        if (shouldLoadMore.value && !isLoading && !isLoadingMore && hasMore) {
            loadMoreData()
        }
    }

    LaunchedEffect(Unit) {
        refreshData()
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
                    text = "阅",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            IconButton(onClick = { refreshData() }) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = "Refresh",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(FocalDimens.iconSizeStandard)
                )
            }
        }

        // 栏目 FilterChips 滚动条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = FocalDimens.pageHorizontalPadding, vertical = FocalDimens.spaceSmall),
            horizontalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
        ) {
            M3CategoryChip(
                isSelected = selectedCategory == null,
                icon = Icons.Rounded.Whatshot,
                label = "全网热榜",
                onClick = { selectedCategory = null }
            )

            NewsCategory.values().forEach { cat ->
                if (prefStore.isCategoryEnabled(cat.key, true)) {
                    M3CategoryChip(
                        isSelected = selectedCategory == cat,
                        icon = cat.icon,
                        label = cat.displayName,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

        // 列表区与分页
        AnimatedContent(
            targetState = isLoading && feeds.isEmpty(),
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "feedLoadingTransition"
        ) { initialLoading ->
            if (initialLoading) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(horizontal = FocalDimens.pageHorizontalPadding),
                    verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
                ) {
                    repeat(6) {
                        M3SkeletonCard()
                    }
                }
            } else if (feeds.isEmpty()) {
                // 空状态与重试
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
                            text = "未获取到该栏目内容或网络受限",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                        Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                        FilledTonalButton(onClick = { refreshData() }) {
                            Text("点击重新抓取")
                        }
                    }
                }
            } else {
                val displayList = remember(selectedCategory, feeds) {
                    if (selectedCategory == null) feeds else feeds.filter { it.category == selectedCategory }
                }

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
                    itemsIndexed(displayList, key = { _, item -> item.id }) { index, item ->
                        UnifiedMediaCard(
                            title = item.title,
                            sourceName = item.sourceName,
                            timeAgo = item.timeAgo,
                            hotScoreDisplay = item.hotScoreDisplay,
                            rank = index + 1,
                            thumbnailUrl = item.thumbnailUrl,
                            isVideo = false,
                            isGallery = item.title.contains("图集") || item.title.contains("组图"),
                            fallbackIcon = when (item.category) {
                                NewsCategory.TECH -> Icons.Rounded.Memory
                                NewsCategory.ENTERTAINMENT -> Icons.Rounded.TheaterComedy
                                NewsCategory.SPORTS -> Icons.Rounded.SportsBasketball
                                NewsCategory.FINANCE -> Icons.Rounded.TrendingUp
                                NewsCategory.WORLD -> Icons.Rounded.Public
                                else -> Icons.Rounded.Article
                            },
                            onClick = {
                                coroutineScope.launch {
                                    val result = NativeContentExtractor.extractContent(
                                        id = item.id,
                                        title = item.title,
                                        sourceName = item.sourceName,
                                        sourceTag = item.sourceTag,
                                        targetUrl = item.url,
                                        preferredType = ContentType.ARTICLE
                                    )
                                    result.onSuccess { structured ->
                                        onOpenDetail(structured)
                                    }
                                }
                            }
                        )
                    }

                    // 底部上滑加载更多指示器
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
                                        text = "正在深度挖掘更多全网热点...",
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
                                    text = "— 已汇聚全网最新全部内容 —",
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
fun M3CategoryChip(
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
                style = MaterialTheme.typography.bodyMedium
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
fun M3SkeletonCard() {
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
