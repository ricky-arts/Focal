package com.focal.app.feature_set

import android.annotation.SuppressLint
import android.graphics.Bitmap
import android.webkit.CookieManager
import android.webkit.WebResourceRequest
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
import androidx.compose.ui.unit.sp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*

/**
 * 身份对接浏览器窗口 (Auth Bridge Dialog)
 * 在隔离沙箱中打开目标 AI 平台的 Web 登录页，自动拦截身份 Cookie 并通过 FocalPreferenceStore 持久化。
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
                .padding(16.dp),
            shape = MaterialTheme.shapes.extraLarge,
            color = InkSurface
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 顶栏：平台信息与一键捕获/完成
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier
                                .size(10.dp)
                                .clip(CircleShape)
                                .background(if (hasValidSession) BambooGreen else MistGrey)
                        )
                        Spacer(modifier = Modifier.width(8.dp))
                        Text(
                            text = "身份对接 · $platformKey",
                            color = MoonWhite,
                            fontSize = 14.sp
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
                                    contentDescription = null,
                                    tint = BambooGreen
                                )
                            }
                        }
                        IconButton(onClick = onDismiss) {
                            Icon(
                                imageVector = Icons.Rounded.Close,
                                contentDescription = null,
                                tint = MistGrey
                            )
                        }
                    }
                }

                Divider(color = InkBorder)

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
                                    // 检查关键身份凭据（如 token, session, uid 等）
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
