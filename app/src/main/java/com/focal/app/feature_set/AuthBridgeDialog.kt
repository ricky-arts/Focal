package com.focal.app.feature_set

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens

/**
 * 纯原生 Compose 身份对接凭据绑定弹窗
 * 彻底移除 WebView 与网页内置加载，杜绝任何 WebView 漏洞与开销。
 */
@Composable
fun AuthBridgeDialog(
    platformKey: String,
    prefStore: FocalPreferenceStore,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    var tokenInput by remember { mutableStateOf(prefStore.getAiAuthSession(platformKey) ?: "") }
    var isSaved by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxWidth(0.92f)
                .wrapContentHeight()
                .padding(FocalDimens.spaceMedium),
            shape = FocalDimens.shapeCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = FocalDimens.elevationFloat
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(FocalDimens.spaceLarge)
            ) {
                // 顶栏：平台信息与关闭
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Surface(
                            shape = FocalDimens.shapeTag,
                            color = MaterialTheme.colorScheme.primaryContainer,
                            modifier = Modifier.size(32.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(
                                    imageVector = Icons.Rounded.VpnKey,
                                    contentDescription = null,
                                    tint = MaterialTheme.colorScheme.onPrimaryContainer,
                                    modifier = Modifier.size(FocalDimens.iconSizeSmall)
                                )
                            }
                        }
                        Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                        Text(
                            text = "原生凭据对接 · $platformKey",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    IconButton(onClick = onDismiss) {
                        Icon(
                            imageVector = Icons.Rounded.Close,
                            contentDescription = "Close",
                            tint = MaterialTheme.colorScheme.onSurfaceVariant,
                            modifier = Modifier.size(FocalDimens.iconSizeStandard)
                        )
                    }
                }

                Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))
                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))
                Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))

                Text(
                    text = "纯原生安全对接通道：已彻底摒弃内置 WebView 网页加载，避免广告弹窗与隐私泄露。请输入或粘贴您在该平台的用户 Session 凭证以实现端侧持久化同步。",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    lineHeight = MaterialTheme.typography.bodySmall.lineHeight
                )

                Spacer(modifier = Modifier.height(FocalDimens.spaceMedium))

                OutlinedTextField(
                    value = tokenInput,
                    onValueChange = { tokenInput = it },
                    label = { Text("平台 Session / Token 凭证") },
                    placeholder = { Text("粘贴 sessionid 或 auth token...") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = FocalDimens.shapeButton,
                    maxLines = 4,
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = MaterialTheme.colorScheme.primary,
                        unfocusedBorderColor = MaterialTheme.colorScheme.outline
                    )
                )

                Spacer(modifier = Modifier.height(FocalDimens.spaceLarge))

                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    TextButton(
                        onClick = {
                            prefStore.clearAiAuthSession(platformKey)
                            tokenInput = ""
                            onDismiss()
                        }
                    ) {
                        Text("清空解绑")
                    }

                    Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))

                    Button(
                        onClick = {
                            if (tokenInput.isNotBlank()) {
                                prefStore.saveAiAuthSession(platformKey, tokenInput.trim())
                                onSuccess()
                            }
                        },
                        shape = FocalDimens.shapeButton,
                        colors = ButtonDefaults.buttonColors(
                            containerColor = MaterialTheme.colorScheme.primary
                        )
                    ) {
                        Icon(
                            imageVector = Icons.Rounded.Check,
                            contentDescription = null,
                            modifier = Modifier.size(FocalDimens.iconSizeSmall)
                        )
                        Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                        Text("保存并持久化")
                    }
                }
            }
        }
    }
}
