package com.focal.app.feature_ask

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.border
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

/**
 * 「问」AI 聚合对话与一键同问对比流：
 * 1. 顶部提供各免费 AI 工具多选勾选（豆包、Kimi、千问、元宝）
 * 2. 一键向所选 AI 并发提问
 * 3. 结果并排/卡片展示，极大方便多维度回答对比
 */
@Composable
fun AskScreen(
    prefStore: FocalPreferenceStore,
    onOpenDrawer: () -> Unit
) {
    val coroutineScope = rememberCoroutineScope()
    val engine = remember { AiAggregatorEngine(prefStore) }

    var inputText by remember { mutableStateOf("") }
    val selectedPlatforms = remember {
        mutableStateListOf(AiPlatform.DOUBAO, AiPlatform.KIMI, AiPlatform.QWEN)
    }

    var answers by remember {
        mutableStateOf<Map<AiPlatform, String>>(emptyMap())
    }
    var isSubmitting by remember { mutableStateOf(false) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(InkBlack)
            .statusBarsPadding()
    ) {
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

            // 清屏 / 重置提问纯图标
            if (answers.isNotEmpty()) {
                IconButton(onClick = { answers = emptyMap() }) {
                    Icon(
                        imageVector = Icons.Rounded.CleaningServices,
                        contentDescription = null,
                        tint = MistGrey
                    )
                }
            }
        }

        // 目标 AI 选择器栏（极简单字符徽标 + 勾选状态）
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp)
        ) {
            AiPlatform.values().forEach { platform ->
                val isSelected = selectedPlatforms.contains(platform)
                val isAuthed = prefStore.isAiAuthenticated(platform.key)

                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(12.dp))
                        .background(if (isSelected) BambooGreenSoft else InkSurface)
                        .border(
                            width = 1.dp,
                            color = if (isSelected) BambooGreen else InkBorder,
                            shape = RoundedCornerShape(12.dp)
                        )
                        .clickable {
                            if (isSelected) {
                                if (selectedPlatforms.size > 1) {
                                    selectedPlatforms.remove(platform)
                                }
                            } else {
                                selectedPlatforms.add(platform)
                            }
                        }
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                    contentAlignment = Alignment.Center
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        // 身份保活微圆点
                        Box(
                            modifier = Modifier
                                .size(6.dp)
                                .clip(CircleShape)
                                .background(if (isAuthed) BambooGreen else MistGrey)
                        )
                        Spacer(modifier = Modifier.width(6.dp))
                        Text(
                            text = platform.displayName,
                            color = if (isSelected) MoonWhite else MistGrey,
                            fontSize = 12.sp
                        )
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(10.dp))

        // 结果展示与比对区
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (answers.isEmpty() && !isSubmitting) {
                // 极简空状态（无多余废话，仅留白与微图标）
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(32.dp),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = InkBorder,
                        modifier = Modifier.size(56.dp)
                    )
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(
                        text = "一键同问，多模态实时对比",
                        color = MistGrey,
                        fontSize = 13.sp
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
                    verticalArrangement = Arrangement.spacedBy(14.dp)
                ) {
                    items(selectedPlatforms.toList()) { platform ->
                        val text = answers[platform] ?: if (isSubmitting) "思考中..." else ""
                        AnswerCompareCard(
                            platform = platform,
                            content = text
                        )
                    }
                }
            }
        }

        // 底部提问输入栏（纯图标发送）
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            color = InkSurface,
            tonalElevation = 8.dp
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp, vertical = 10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "向所选 AI 同时提问...",
                            color = MistGrey,
                            fontSize = 14.sp
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = Color.Transparent,
                        unfocusedContainerColor = Color.Transparent,
                        focusedIndicatorColor = Color.Transparent,
                        unfocusedIndicatorColor = Color.Transparent,
                        focusedTextColor = MoonWhite,
                        unfocusedTextColor = MoonWhite
                    ),
                    maxLines = 3
                )

                IconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSubmitting) {
                            val prompt = inputText
                            inputText = ""
                            isSubmitting = true
                            answers = emptyMap()

                            // 并发同时向选中的所有 AI 发起问答流
                            selectedPlatforms.forEach { platform ->
                                coroutineScope.launch {
                                    val sb = StringBuilder()
                                    engine.askStream(platform, prompt).collect { chunk ->
                                        sb.append(chunk)
                                        answers = answers.toMutableMap().apply {
                                            put(platform, sb.toString())
                                        }
                                    }
                                }
                            }
                            isSubmitting = false
                        }
                    },
                    modifier = Modifier
                        .size(42.dp)
                        .clip(CircleShape)
                        .background(BambooGreenSoft)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = null,
                        tint = BambooGreen,
                        modifier = Modifier.size(22.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun AnswerCompareCard(
    platform: AiPlatform,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = InkSurface),
        elevation = CardDefaults.cardElevation(defaultElevation = 2.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            // 顶端品牌徽标与名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        modifier = Modifier
                            .size(24.dp)
                            .clip(RoundedCornerShape(6.dp))
                            .background(BambooGreenSoft),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = platform.badgeChar,
                            color = BambooGreen,
                            fontSize = 12.sp
                        )
                    }
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = platform.displayName,
                        color = MoonWhite,
                        fontSize = 14.sp
                    )
                }

                // 复制纯图标
                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = null,
                    tint = MistGrey,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(10.dp))
            Divider(color = InkBorder)
            Spacer(modifier = Modifier.height(10.dp))

            // 回答正文
            Text(
                text = content,
                color = MoonWhite,
                fontSize = 14.sp,
                lineHeight = 22.sp
            )
        }
    }
}
