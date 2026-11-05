package com.focal.app.feature_set

import android.annotation.SuppressLint
import android.webkit.CookieManager
import android.webkit.WebView
import android.webkit.WebViewClient
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CheckCircle
import androidx.compose.material.icons.rounded.Close
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.FocalDimens

/**
 * 身份对接浏览器窗口 (Auth Bridge Dialog) M3 重构
 */
@SuppressLint("SetJavaScriptEnabled")
@Composable
fun AuthBridgeDialog(
    platformKey: String,
    prefStore: FocalPreferenceStore,
    onDismiss: () -> Unit,
    onSuccess: () -> Unit
) {
    val platformUrl = remember(platformKey) {
        when (platformKey) {
            "doubao" -> "https://www.doubao.com/chat/"
            "kimi" -> "https://kimi.moonshot.cn/"
            "qwen" -> "https://tongyi.aliyun.com/qianwen/"
            "yuanbao" -> "https://yuanbao.tencent.com/"
            else -> "https://www.doubao.com"
        }
    }

    var interceptedCookies by remember { mutableStateOf("") }
    var hasValidSession by remember { mutableStateOf(false) }

    Dialog(
        onDismissRequest = onDismiss,
        properties = DialogProperties(usePlatformDefaultWidth = false)
    ) {
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(FocalDimens.spaceMedium),
            shape = FocalDimens.shapeCard,
            color = MaterialTheme.colorScheme.surface,
            tonalElevation = FocalDimens.elevationFloat
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶栏：平台信息与一键捕获/完成
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = FocalDimens.spaceMedium, vertical = FocalDimens.spaceSmall),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(8.dp)
                                .clip(CircleShape)
                                .background(if (hasValidSession) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.outline)
                        )
                        Spacer(modifier = Modifier.width(FocalDimens.spaceSmall))
                        Text(
                            text = "身份对接 · $platformKey",
                            style = MaterialTheme.typography.titleMedium,
                            color = MaterialTheme.colorScheme.onSurface
                        )
                    }

                    Row {
                        if (hasValidSession) {
                            IconButton(onClick = {
                                prefStore.saveAiAuthSession(platformKey, interceptedCookies)
                                onSuccess()
                            }) {
                                Icon(
                                    imageVector = Icons.Rounded.CheckCircle,
                                    contentDescription = "Confirm",
                                    tint = MaterialTheme.colorScheme.primary,
                                    modifier = Modifier.size(FocalDimens.iconSizeStandard)
                                )
                            }
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
                }

                HorizontalDivider(color = MaterialTheme.colorScheme.outlineVariant.copy(alpha = 0.4f))

                // 核心内置 Web 鉴权沙箱
                AndroidView(
                    modifier = Modifier.fillMaxSize(),
                    factory = { context ->
                        WebView(context).apply {
                            settings.javaScriptEnabled = true
                            settings.domStorageEnabled = true
                            settings.databaseEnabled = true

                            webViewClient = object : WebViewClient() {
                                override fun onPageFinished(view: WebView?, url: String?) {
                                    super.onPageFinished(view, url)
                                    val cookieManager = CookieManager.getInstance()
                                    val cookies = cookieManager.getCookie(url) ?: ""
                                    if (cookies.contains("session") ||
                                        cookies.contains("token") ||
                                        cookies.contains("uid") ||
                                        cookies.length > 50) {
                                        interceptedCookies = cookies
                                        hasValidSession = true
                                    }
                                }
                            }
                            loadUrl(platformUrl)
                        }
                    }
                )
            }
        }
    }
}
