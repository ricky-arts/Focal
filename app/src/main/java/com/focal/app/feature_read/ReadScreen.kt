package com.focal.app.feature_read

import android.content.Intent
import android.net.Uri
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.rememberScrollState
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
import kotlinx.coroutines.launch

/**
 * 「阅」资讯聚合流：
 * 1. 顶部提供纯图标栏目筛选（综合、娱乐、体育、科技、财经、国际）
 * 2. 全网多源（头条、微博、知乎、百度、Reddit、HN）热度归一化排行榜
 * 3. 极简卡片：序号高亮、平台微标、归一化热度指标
 */
@Composable
fun ReadScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var feeds by remember { mutableStateOf(NewsRepository.getFallbackFeeds()) }
    var selectedCategory by remember { mutableStateOf<NewsCategory?>(null) } // null 表示展示用户配置的综合统一榜
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
            .background(InkBlack)
            .statusBarsPadding()
    ) {
        // 极简顶栏：仅图标操作
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 12.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                IconButton(onClick = onOpenDrawer) {
                    Icon(
                        imageVector = Icons.Rounded.Menu,
                        contentDescription = null,
                        tint = MoonWhite
                    )
                }
                Spacer(modifier = Modifier.width(4.dp))
                // 状态圆点
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(if (isLoading) CinnabarRed else BambooGreen)
                )
            }

            IconButton(onClick = { refreshData() }) {
                Icon(
                    imageVector = Icons.Rounded.Refresh,
                    contentDescription = null,
                    tint = MistGrey
                )
            }
        }

        // 纯图标栏目筛选条（用户可快速在所启用的分类间切换）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            // "全网统一总榜" 纯图标
            CategoryFilterChip(
                isSelected = selectedCategory == null,
                icon = Icons.Rounded.Whatshot,
                token = "全",
                onClick = { selectedCategory = null }
            )

            // 各细分品类图标
            NewsCategory.values().forEach { cat ->
                if (prefStore.isCategoryEnabled(cat.key, true)) {
                    CategoryFilterChip(
                        isSelected = selectedCategory == cat,
                        icon = cat.icon,
                        token = cat.token,
                        onClick = { selectedCategory = cat }
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(4.dp))

        // 过滤后按热度归一化排序的榜单列表
        val displayList = remember(selectedCategory, feeds) {
            if (selectedCategory == null) {
                feeds
            } else {
                feeds.filter { it.category == selectedCategory }
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            itemsIndexed(displayList, key = { _, item -> item.id }) { index, item ->
                NewsRankCard(
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

@Composable
fun CategoryFilterChip(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    token: String,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isSelected) BambooGreenSoft else InkSurface)
            .clickable(onClick = onClick)
            .padding(horizontal = 12.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = if (isSelected) BambooGreen else MistGrey,
                modifier = Modifier.size(16.dp)
            )
            Spacer(modifier = Modifier.width(6.dp))
            Text(
                text = token,
                color = if (isSelected) MoonWhite else MistGrey,
                fontSize = 12.sp
            )
        }
    }
}

@Composable
fun NewsRankCard(
    rank: Int,
    item: NewsItem,
    onClick: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(14.dp))
            .clickable(onClick = onClick),
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 热度排行榜位（Top 3 朱砂红高亮）
            Box(
                modifier = Modifier
                    .size(28.dp)
                    .clip(CircleShape)
                    .background(if (rank <= 3) CinnabarRed.copy(alpha = 0.15f) else InkSurfaceVariant),
                contentAlignment = Alignment.Center
            ) {
                Text(
                    text = "$rank",
                    color = if (rank <= 3) CinnabarRed else MistGrey,
                    fontSize = 13.sp
                )
            }

            Spacer(modifier = Modifier.width(12.dp))

            Column(modifier = Modifier.weight(1f)) {
                // 顶端微标：来源平台（头条/微博/知乎/百度等）与归一化热度
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .clip(RoundedCornerShape(4.dp))
                                .background(InkSurfaceVariant)
                                .padding(horizontal = 5.dp, vertical = 2.dp)
                        ) {
                            Text(
                                text = item.sourceName,
                                color = BambooGreen,
                                fontSize = 10.sp
                            )
                        }
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = item.timeAgo,
                            color = MistGrey,
                            fontSize = 11.sp
                        )
                    }

                    // 热度指标
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = if (rank <= 3) CinnabarRed else MistGrey,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.hotScoreDisplay,
                            color = if (rank <= 3) CinnabarRed else MistGrey,
                            fontSize = 11.sp
                        )
                    }
                }

                Spacer(modifier = Modifier.height(8.dp))

                // 资讯标题
                Text(
                    text = item.title,
                    color = MoonWhite,
                    fontSize = 14.sp,
                    lineHeight = 20.sp
                )
            }
        }
    }
}
