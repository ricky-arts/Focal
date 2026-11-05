package com.focal.app.feature_read

import android.content.Intent
import android.net.Uri
import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.TrendingUp
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import kotlinx.coroutines.launch

/**
 * Material 3 官方规范「阅」界面重构：
 * 1. 严格使用 MaterialTheme.colorScheme 与 FocalTypography，杜绝硬编码颜色
 * 2. 统一圆角规范 (卡片 12dp, 标签 4dp)，轻微柔和 elevation 1~2dp
 * 3. 规整左右 16dp 安全内边距与 8/16dp 间距模数
 * 4. 优雅缩略图排版支持
 * 5. 加载骨架屏与淡入过渡动画
 */
@Composable
fun ReadScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var feeds by remember { mutableStateOf(NewsRepository.getFallbackFeeds()) }
    var selectedCategory by remember { mutableStateOf<NewsCategory?>(null) }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    fun refreshData() {
        coroutineScope.launch {
            isLoading = true
            feeds = NewsRepository.fetchUnifiedRankings(prefStore)
            isLoading = false
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

        // M3 栏目 FilterChips 滚动条
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

        // 数据列表与骨架屏
        AnimatedContent(
            targetState = isLoading,
            transitionSpec = { fadeIn() togetherWith fadeOut() },
            label = "feedTransition"
        ) { loading ->
            if (loading && feeds.isEmpty()) {
                // 骨架屏占位
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
            } else {
                val displayList = remember(selectedCategory, feeds) {
                    if (selectedCategory == null) feeds else feeds.filter { it.category == selectedCategory }
                }

                LazyColumn(
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
                        M3NewsCard(
                            rank = index + 1,
                            item = item,
                            onClick = {
                                val intent = Intent(Intent.ACTION_VIEW, Uri.parse(item.url))
                                context.startActivity(intent)
                            }
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

@Composable
fun M3NewsCard(
    rank: Int,
    item: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick),
        shape = FocalDimens.shapeCard,
        elevation = CardDefaults.cardElevation(defaultElevation = FocalDimens.elevationSubtle),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(FocalDimens.spaceMedium),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 排位序号徽章
            Box(
                modifier = Modifier
                    .size(24.dp)
                    .clip(CircleShape)
                    .background(
                        if (rank <= 3) MaterialTheme.colorScheme.errorContainer else MaterialTheme.colorScheme.surfaceVariant
                    ),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    style = MaterialTheme.typography.labelSmall,
                    color = if (rank <= 3) MaterialTheme.colorScheme.onErrorContainer else MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(modifier = Modifier.width(FocalDimens.spaceMedium))

            // 标题与来源信息区
            Column(modifier = Modifier.weight(1f)) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = FocalDimens.shapeTag,
                            color = MaterialTheme.colorScheme.secondaryContainer
                        ) {
                            Text(
                                text = item.sourceName,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onSecondaryContainer,
                                modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                            )
                        }
                        Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                        Text(
                            text = item.timeAgo,
                            style = MaterialTheme.typography.bodySmall,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }

                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (rank <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(14.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.hotScoreDisplay,
                            style = MaterialTheme.typography.bodySmall,
                            color = if (rank <= 3) MaterialTheme.colorScheme.error else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                }

                Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

                Text(
                    text = item.title,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 2,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 优雅卡片右侧缩略图支持（统一精致 64x64dp 尺寸）
            Spacer(modifier = Modifier.width(FocalDimens.spaceMedium))
            Surface(
                modifier = Modifier
                    .size(64.dp)
                    .clip(FocalDimens.shapeButton),
                color = MaterialTheme.colorScheme.surfaceVariant,
                tonalElevation = FocalDimens.elevationSubtle
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = when (item.category) {
                            NewsCategory.TECH -> Icons.Rounded.Memory
                            NewsCategory.ENTERTAINMENT -> Icons.Rounded.TheaterComedy
                            NewsCategory.SPORTS -> Icons.Rounded.SportsBasketball
                            NewsCategory.FINANCE -> Icons.Rounded.TrendingUp
                            NewsCategory.WORLD -> Icons.Rounded.Public
                            else -> Icons.Rounded.Article
                        },
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.primary.copy(alpha = 0.7f),
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
            }
        }
    }
}

@Composable
fun M3SkeletonCard() {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .height(76.dp),
        shape = FocalDimens.shapeCard,
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.35f)
        )
    ) {}
}
