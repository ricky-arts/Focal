package com.focal.app.feature_read

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
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*
import kotlinx.coroutines.launch

/**
 * 「阅」资讯聚合流：
 * 极简、图标准确、无冗余描述，以来源徽标和时间刻度呈现
 */
@Composable
fun ReadScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    val context = LocalContext.current
    var feeds by remember { mutableStateOf(NewsRepository.getFallbackFeeds()) }
    var selectedTag by remember { mutableStateOf("ALL") }
    var isLoading by remember { mutableStateOf(false) }

    LaunchedEffect(Unit) {
        isLoading = true
        feeds = NewsRepository.fetchRealHotFeeds()
        isLoading = false
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
                // 呼吸圆点与板块意象
                Box(
                    modifier = Modifier
                        .size(6.dp)
                        .clip(CircleShape)
                        .background(BambooGreen)
                )
            }

            Row(verticalAlignment = Alignment.CenterVertically) {
                val coroutineScope = rememberCoroutineScope()
                IconButton(onClick = {
                    coroutineScope.launch {
                        isLoading = true
                        feeds = NewsRepository.fetchRealHotFeeds()
                        isLoading = false
                    }
                }) {
                    Icon(
                        imageVector = Icons.Rounded.Refresh,
                        contentDescription = null,
                        tint = MistGrey
                    )
                }
            }
        }

        // 纯图标与微标流筛选条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            FilterChipIcon(
                isSelected = selectedTag == "ALL",
                icon = Icons.Rounded.FilterList,
                onClick = { selectedTag = "ALL" }
            )
            FilterChipIcon(
                isSelected = selectedTag == "WB",
                icon = Icons.Rounded.TrendingUp,
                onClick = { selectedTag = "WB" }
            )
            FilterChipIcon(
                isSelected = selectedTag == "ZH",
                icon = Icons.Rounded.Lightbulb,
                onClick = { selectedTag = "ZH" }
            )
            FilterChipIcon(
                isSelected = selectedTag == "HN",
                icon = Icons.Rounded.Code,
                onClick = { selectedTag = "HN" }
            )
        }

        Spacer(modifier = Modifier.height(6.dp))

        // 资讯卡片列表
        val filteredFeeds = remember(selectedTag, feeds) {
            if (selectedTag == "ALL") feeds else feeds.filter { it.sourceTag == selectedTag }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            items(filteredFeeds, key = { it.id }) { item ->
                NewsFeedCard(
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
fun FilterChipIcon(
    isSelected: Boolean,
    icon: androidx.compose.ui.graphics.vector.ImageVector,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .size(36.dp)
            .clip(CircleShape)
            .background(if (isSelected) BambooGreenSoft else InkSurface)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = if (isSelected) BambooGreen else MistGrey,
            modifier = Modifier.size(18.dp)
        )
    }
}

@Composable
fun NewsFeedCard(
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
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(14.dp)
        ) {
            // 顶端微标与时间
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    // 平台精简角标
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(6.dp))
                            .background(InkSurfaceVariant)
                            .padding(horizontal = 6.dp, vertical = 2.dp)
                    ) {
                        Text(
                            text = item.sourceTag,
                            color = BambooGreen,
                            fontSize = 10.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = item.timeAgo,
                        color = MistGrey,
                        fontSize = 11.sp
                    )
                }

                if (item.hotScore != null) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(
                            imageVector = Icons.Rounded.LocalFireDepartment,
                            contentDescription = null,
                            tint = CinnabarRed,
                            modifier = Modifier.size(13.dp)
                        )
                        Spacer(modifier = Modifier.width(2.dp))
                        Text(
                            text = item.hotScore,
                            color = CinnabarRed,
                            fontSize = 11.sp
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(10.dp))

            // 资讯标题
            Text(
                text = item.title,
                color = MoonWhite,
                fontSize = 15.sp,
                lineHeight = 22.sp
            )
        }
    }
}
