package com.focal.app.feature_detail

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.rounded.ArrowBack
import androidx.compose.material.icons.rounded.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.compose.ui.window.Dialog
import androidx.compose.ui.window.DialogProperties
import coil.compose.AsyncImage
import com.focal.app.core.model.ArticleDetailModel
import com.focal.app.core.network.FocalApiClient
import com.focal.app.core.storage.AccountHubStore
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.storage.db.ArticleEntity
import com.focal.app.core.storage.db.FocalRoomDatabase
import com.focal.app.feature_comment.CommentBottomSheet
import com.focal.app.feature_renderer.NativeArticleRenderer
import com.google.gson.Gson
import com.google.gson.reflect.TypeToken
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProductionDetailScreen(
    id: String,
    platform: String,
    title: String,
    originUrl: String,
    apiClient: FocalApiClient,
    prefStore: FocalPreferenceStore,
    accountStore: AccountHubStore,
    roomDb: FocalRoomDatabase,
    onBack: () -> Unit
) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val gson = remember { Gson() }

    var article by remember { mutableStateOf<ArticleDetailModel?>(null) }
    var isLoading by remember { mutableStateOf(true) }
    var fontDelta by remember { mutableFloatStateOf(0f) }
    var isFavorited by remember { mutableStateOf(prefStore.isFavorite(id)) }
    var showCommentSheet by remember { mutableStateOf(false) }
    var previewImageUrl by remember { mutableStateOf<String?>(null) }

    // 核心：优先从 Room 离线缓存秒开；若无则走 API
    LaunchedEffect(id) {
        scope.launch {
            val local = roomDb.articleDao().getArticleById(id)
            if (local != null) {
                val blockType = object : TypeToken<List<com.focal.app.core.model.ContentBlockModel>>() {}.type
                val blocks: List<com.focal.app.core.model.ContentBlockModel> = gson.fromJson(local.contentBlocksJson, blockType)
                article = ArticleDetailModel(
                    id = local.id,
                    platform = local.platform,
                    title = local.title,
                    authorName = local.authorName,
                    authorAvatar = local.authorAvatar,
                    publishTime = local.publishTime,
                    originUrl = local.originUrl,
                    blocks = blocks,
                    commentCount = local.commentCount
                )
                isLoading = false
            }

            // 静默拉取最新数据热替换
            try {
                val remote = apiClient.fetchArticleDetail(id, platform, originUrl)
                article = remote
                roomDb.articleDao().insertArticle(
                    ArticleEntity(
                        id = remote.id,
                        platform = remote.platform,
                        title = remote.title,
                        authorName = remote.authorName,
                        authorAvatar = remote.authorAvatar,
                        publishTime = remote.publishTime,
                        originUrl = remote.originUrl,
                        contentBlocksJson = gson.toJson(remote.blocks),
                        commentCount = remote.commentCount
                    )
                )
            } catch (_: Exception) {}
            isLoading = false
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(platform.uppercase(), style = MaterialTheme.typography.titleMedium) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Rounded.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { fontDelta = if (fontDelta >= 4f) -2f else fontDelta + 2f }) {
                        Icon(Icons.Rounded.FormatSize, contentDescription = "Font Size")
                    }
                    IconButton(onClick = {
                        val clip = ClipData.newPlainText("Focal", "${article?.title}\n${article?.originUrl}")
                        (context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager).setPrimaryClip(clip)
                        Toast.makeText(context, "链接已复制", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(Icons.Rounded.ContentCopy, contentDescription = "Copy")
                    }
                    IconButton(onClick = {
                        isFavorited = prefStore.toggleFavorite(id)
                        Toast.makeText(context, if (isFavorited) "已收藏" else "取消收藏", Toast.LENGTH_SHORT).show()
                    }) {
                        Icon(
                            if (isFavorited) Icons.Rounded.Star else Icons.Rounded.StarBorder,
                            contentDescription = "Star",
                            tint = if (isFavorited) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    }
                    IconButton(onClick = {
                        val intent = Intent(Intent.ACTION_SEND).apply {
                            type = "text/plain"
                            putExtra(Intent.EXTRA_TEXT, "${article?.title}\n${article?.originUrl}")
                        }
                        context.startActivity(Intent.createChooser(intent, "分享到"))
                    }) {
                        Icon(Icons.Rounded.Share, contentDescription = "Share")
                    }
                }
            )
        },
        bottomBar = {
            Surface(
                color = MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.95f),
                tonalElevation = 6.dp
            ) {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 10.dp),
                    horizontalArrangement = Arrangement.SpaceBetween,
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    FilledTonalButton(
                        onClick = { showCommentSheet = true },
                        modifier = Modifier.weight(1f)
                    ) {
                        Icon(Icons.Rounded.ChatBubbleOutline, contentDescription = null, modifier = Modifier.size(18.dp))
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("查看评论 (${article?.commentCount ?: 0})")
                    }
                }
            }
        }
    ) { innerPadding ->
        Box(modifier = Modifier.fillMaxSize().padding(innerPadding)) {
            if (isLoading && article == null) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    CircularProgressIndicator()
                }
            } else if (article != null) {
                NativeArticleRenderer(
                    article = article!!,
                    fontSizeDelta = fontDelta,
                    onImageClick = { url -> previewImageUrl = url }
                )
            }
        }
    }

    // 评论抽屉
    if (showCommentSheet && article != null) {
        var comments by remember { mutableStateOf<List<com.focal.app.core.model.UnifiedComment>>(emptyList()) }
        LaunchedEffect(Unit) {
            comments = apiClient.fetchComments(id, platform, originUrl)
        }

        CommentBottomSheet(
            comments = comments,
            commentCount = article?.commentCount ?: 0,
            onDismiss = { showCommentSheet = false },
            onSendComment = { text ->
                scope.launch {
                    val cookie = accountStore.getPlatformCookie(platform) ?: ""
                    val ok = apiClient.postComment(platform, id, text, cookie)
                    Toast.makeText(context, if (ok) "发表成功" else "请先绑定账号", Toast.LENGTH_SHORT).show()
                }
            }
        )
    }

    // 全屏大图
    if (previewImageUrl != null) {
        Dialog(
            onDismissRequest = { previewImageUrl = null },
            properties = DialogProperties(usePlatformDefaultWidth = false)
        ) {
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .background(MaterialTheme.colorScheme.scrim.copy(alpha = 0.95f))
                    .clickable { previewImageUrl = null },
                contentAlignment = Alignment.Center
            ) {
                AsyncImage(
                    model = previewImageUrl,
                    contentDescription = null,
                    modifier = Modifier.fillMaxWidth().padding(16.dp)
                )
            }
        }
    }
}
