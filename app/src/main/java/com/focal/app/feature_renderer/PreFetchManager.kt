package com.focal.app.feature_renderer

import android.content.Context
import coil.imageLoader
import coil.request.ImageRequest
import com.focal.app.core.network.FocalApiClient
import com.focal.app.core.storage.db.ArticleEntity
import com.focal.app.core.storage.db.FocalRoomDatabase
import com.google.gson.Gson
import kotlinx.coroutines.*

class PreFetchManager(
    private val context: Context,
    private val db: FocalRoomDatabase,
    private val api: FocalApiClient
) {
    private val scope = CoroutineScope(Dispatchers.IO + SupervisorJob())
    private val preloadedIds = mutableSetOf<String>()
    private val gson = Gson()

    fun prefetchArticles(items: List<Triple<String, String, String>>) { // Triple<id, platform, originUrl>
        scope.launch {
            items.take(8).forEach { (id, platform, url) ->
                if (preloadedIds.contains(id)) return@forEach
                preloadedIds.add(id)

                val existing = db.articleDao().getArticleById(id)
                if (existing != null) return@forEach

                try {
                    val detail = api.fetchArticleDetail(id, platform, url)
                    val entity = ArticleEntity(
                        id = detail.id,
                        platform = detail.platform,
                        title = detail.title,
                        authorName = detail.authorName,
                        authorAvatar = detail.authorAvatar,
                        publishTime = detail.publishTime,
                        originUrl = detail.originUrl,
                        contentBlocksJson = gson.toJson(detail.blocks),
                        commentCount = detail.commentCount
                    )
                    db.articleDao().insertArticle(entity)

                    // 预加载前两张图片到 Coil 磁盘/内存缓存
                    detail.blocks.filterIsInstance<com.focal.app.core.model.ContentBlockModel.Image>()
                        .take(2)
                        .forEach { img ->
                            val req = ImageRequest.Builder(context)
                                .data(img.proxyUrl)
                                .build()
                            context.imageLoader.enqueue(req)
                        }
                } catch (_: Exception) {}
            }
        }
    }
}
