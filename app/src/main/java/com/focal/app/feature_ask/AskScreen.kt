package com.focal.app.feature_ask

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
import androidx.compose.ui.unit.dp
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens
import kotlinx.coroutines.launch

/**
 * Material 3 官方规范「问」界面重构：
 * 1. 严格使用 MaterialTheme.colorScheme 与 FocalTypography
 * 2. M3 FilterChip 规范多选栏（豆包/Kimi/千问/元宝）
 * 3. 结果对比卡片统一 12dp 圆角，1~2dp 微阴影，规范 24dp 矢量图标
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
                    text = "问",
                    style = MaterialTheme.typography.titleLarge,
                    color = MaterialTheme.colorScheme.onSurface
                )
            }

            if (answers.isNotEmpty()) {
                IconButton(onClick = { answers = emptyMap() }) {
                    Icon(
                        imageVector = Icons.Rounded.CleaningServices,
                        contentDescription = "Clear",
                        tint = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                    )
                }
            }
        }

        // M3 AI 模型勾选条
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = FocalDimens.pageHorizontalPadding, vertical = FocalDimens.spaceSmall),
            horizontalArrangement = Arrangement.spacedBy(FocalDimens.spaceSmall)
        ) {
            AiPlatform.values().forEach { platform ->
                val isSelected = selectedPlatforms.contains(platform)
                val isAuthed = prefStore.isAiAuthenticated(platform.key)

                FilterChip(
                    selected = isSelected,
                    onClick = {
                        if (isSelected) {
                            if (selectedPlatforms.size > 1) {
                                selectedPlatforms.remove(platform)
                            }
                        } else {
                            selectedPlatforms.add(platform)
                        }
                    },
                    shape = FocalDimens.shapePill,
                    leadingIcon = {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(
                                    if (isAuthed) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline
                                )
                        )
                    },
                    label = {
                        Text(
                            text = platform.displayName,
                            style = MaterialTheme.typography.bodySmall
                        )
                    },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer,
                        selectedLabelColor = MaterialTheme.colorScheme.onPrimaryContainer,
                        containerColor = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.5f),
                        labelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    ),
                    border = FilterChipDefaults.filterChipBorder(
                        enabled = true,
                        selected = isSelected,
                        borderColor = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.5f),
                        selectedBorderColor = MaterialTheme.colorScheme.primary.copy(alpha = 0.4f)
                    )
                )
            }
        }

        Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

        // 对比结果展示区
        Box(
            modifier = Modifier
                .weight(1f)
                .fillMaxWidth()
        ) {
            if (answers.isEmpty() && !isSubmitting) {
                Column(
                    modifier = Modifier
                        .fillMaxSize()
                        .padding(FocalDimens.spaceExtraLarge),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    Icon(
                        imageVector = Icons.Rounded.AutoAwesome,
                        contentDescription = null,
                        tint = MaterialTheme.colorScheme.outlineVariant,
                        modifier = Modifier.size(48.dp)
                    )
                    Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                    Text(
                        text = "一键同问，多模型流式实时对比",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            } else {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(
                        start = FocalDimens.pageHorizontalPadding,
                        end = FocalDimens.pageHorizontalPadding,
                        top = FocalDimens.spaceSmall,
                        bottom = FocalDimens.spaceLarge
                    ),
                    verticalArrangement = Arrangement.spacedBy(FocalDimens.spaceMedium)
                ) {
                    items(selectedPlatforms.toList()) { platform ->
                        val text = answers[platform] ?: if (isSubmitting) "思考中..." else ""
                        M3AnswerCard(
                            platform = platform,
                            content = text
                        )
                    }
                }
            }
        }

        // 底部提问输入栏
        Surface(
            modifier = Modifier
                .fillMaxWidth()
                .imePadding(),
            color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
            tonalElevation = FocalDimens.elevationFloat
        ) {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = FocalDimens.pageHorizontalPadding, vertical = FocalDimens.spaceSmall),
                verticalAlignment = Alignment.CenterVertically
            ) {
                TextField(
                    value = inputText,
                    onValueChange = { inputText = it },
                    placeholder = {
                        Text(
                            text = "向所选 AI 同时提问...",
                            style = MaterialTheme.typography.bodyMedium,
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    },
                    modifier = Modifier.weight(1f),
                    colors = TextFieldDefaults.colors(
                        focusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedContainerColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        unfocusedIndicatorColor = androidx.compose.ui.graphics.Color.Transparent,
                        focusedTextColor = MaterialTheme.colorScheme.onSurface,
                        unfocusedTextColor = MaterialTheme.colorScheme.onSurface
                    ),
                    maxLines = 3
                )

                FilledIconButton(
                    onClick = {
                        if (inputText.isNotBlank() && !isSubmitting) {
                            val prompt = inputText
                            inputText = ""
                            isSubmitting = true
                            answers = emptyMap()

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
                    shape = CircleShape,
                    colors = IconButtonDefaults.filledIconButtonColors(
                        containerColor = MaterialTheme.colorScheme.primary,
                        contentColor = MaterialTheme.colorScheme.onPrimary
                    ),
                    modifier = Modifier.size(40.dp)
                ) {
                    Icon(
                        imageVector = Icons.Rounded.ArrowUpward,
                        contentDescription = "Send",
                        modifier = Modifier.size(20.dp)
                    )
                }
            }
        }
    }
}

@Composable
fun M3AnswerCard(
    platform: AiPlatform,
    content: String
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = FocalDimens.shapeCard,
        elevation = CardDefaults.cardElevation(defaultElevation = FocalDimens.elevationCard),
        colors = CardDefaults.cardColors(
            containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.85f)
        )
    ) {
        Column(modifier = Modifier.padding(FocalDimens.spaceMedium)) {
            // 顶端品牌徽标与名称
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(26.dp),
                        shape = FocalDimens.shapeTag,
                        color = MaterialTheme.colorScheme.primaryContainer
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Text(
                                text = platform.badgeChar,
                                style = MaterialTheme.typography.labelSmall,
                                color = MaterialTheme.colorScheme.onPrimaryContainer
                            )
                        }
                    }
                    Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                    Text(
                        text = platform.displayName,
                        style = MaterialTheme.typography.titleMedium,
                        color = MaterialTheme.colorScheme.onSurface
                    )
                }

                Icon(
                    imageVector = Icons.Rounded.ContentCopy,
                    contentDescription = "Copy",
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(16.dp)
                )
            }

            Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))
            HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(FocalDimens.spaceSmall))

            Text(
                text = content,
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.onSurface,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight
            )
        }
    }
}
