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
 * 1. 初始直接挂载优质种子包，网络并发抓取完成后热更新，绝不展示空列表；
 * 2. 栏目过滤弹性容错：若选定细分栏目无数据，平滑展示全网热门，不白屏；
 * 3. 采用全板块统一左图右文卡片 (UnifiedMediaCard)，优先展示各源首图/封面缩略图；
 * 4. 支持下拉刷新 (Refresh) 与上滑触底自动加载更多 (Pagination)。
 */
@Composable
fun ReadScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit,
    onOpenDetail: (StructuredContent) -> Unit
) {
    // 初始即挂载种子数据，确保冷启动绝对不呈现空屏幕
    var feeds by remember { mutableStateOf(NewsRepository.getFallbackFeeds(page = 1)) }
    var selectedCategory by remember { mutableStateOf<NewsCategory?>(null) }
    var isLoading by remember { mutableStateOf(false) }
    var isLoadingMore by remember { mutableStateOf(false) }
    var currentPage by remember { mutableIntStateOf(1) }
    var hasMore by remember { mutableStateOf(true) }

    val coroutineScope = rememberCoroutineScope()
    val listState = rememberLazyListState()

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            currentPage = 1
            hasMore = true
            val items = NewsRepository.fetchUnifiedRankings(prefStore, page = 1)
            if (items.isNotEmpty()) {
                feeds = items
            }
            isLoading = false
        }
    }

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

        // 计算当前显示列表：若选定栏目有匹配则过滤，否则回退全量
        val displayList = remember(selectedCategory, feeds) {
            if (selectedCategory == null) {
                feeds
            } else {
                val match = feeds.filter { it.category == selectedCategory }
                if (match.isNotEmpty()) match else feeds
            }
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
