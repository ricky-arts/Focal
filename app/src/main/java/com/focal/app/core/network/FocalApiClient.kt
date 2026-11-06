package com.focal.app.core.network

import com.focal.app.core.model.ArticleDetailModel
import com.focal.app.core.model.ContentBlockModel
import com.focal.app.core.model.UnifiedComment
import com.focal.app.feature_read.NewsCategory
import com.focal.app.feature_read.NewsItem
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.json.JSONArray
import org.json.JSONObject

class FocalApiClient(val baseUrl: String = "http://127.0.0.1:8080") {

    suspend fun fetchHotList(platform: String): List<NewsItem> = withContext(Dispatchers.IO) {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("$baseUrl/api/v1/hotlist?platform=$platform")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val data = json.optJSONArray("data")
                if (data != null) {
                    for (i in 0 until data.length()) {
                        val obj = data.getJSONObject(i)
                        val id = obj.optString("id", "")
                        val title = obj.optString("title", "")
                        val summary = obj.optString("summary", "")
                        val cover = obj.optString("coverImage", null)
                        val fullCover = if (cover != null && cover.startsWith("/")) "$baseUrl$cover" else cover
                        val scoreNum = obj.optLong("hotScore", 0L)
                        val scoreDisp = obj.optString("hotScoreDisplay", "")
                        val originUrl = obj.optString("originUrl", "")

                        if (title.isNotBlank()) {
                            list.add(
                                NewsItem(
                                    id = id,
                                    title = title,
                                    sourceName = when(platform) {
                                        "zhihu" -> "知乎"
                                        "bilibili" -> "B站热门"
                                        "weibo" -> "微博热搜"
                                        "ithome" -> "IT之家"
                                        "36kr" -> "36氪"
                                        "sspai" -> "少数派"
                                        else -> platform
                                    },
                                    sourceTag = platform.uppercase(),
                                    category = when(platform) {
                                        "zhihu", "sspai" -> NewsCategory.TECH
                                        "36kr" -> NewsCategory.FINANCE
                                        "weibo" -> NewsCategory.ENTERTAINMENT
                                        else -> NewsCategory.GENERAL
                                    },
                                    timeAgo = "实时",
                                    hotScoreNum = scoreNum,
                                    hotScoreDisplay = scoreDisp,
                                    url = originUrl,
                                    thumbnailUrl = fullCover
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun fetchArticleDetail(id: String, platform: String, originUrl: String): ArticleDetailModel = withContext(Dispatchers.IO) {
        val req = Request.Builder()
            .url("$baseUrl/api/v1/article/detail?platform=$platform&id=$id&url=${encode(originUrl)}")
            .build()
        val res = FocalHttpClient.client.newCall(req).execute()
        if (!res.isSuccessful) throw Exception("BFF request failed: ${res.code}")

        val json = JSONObject(res.body?.string() ?: "{}")
        val data = json.optJSONObject("data") ?: throw Exception("Empty article data")

        val blocks = mutableListOf<ContentBlockModel>()
        val blocksArr = data.optJSONArray("contentBlocks")
        if (blocksArr != null) {
            for (i in 0 until blocksArr.length()) {
                val b = blocksArr.getJSONObject(i)
                when (b.optString("type")) {
                    "heading" -> blocks.add(ContentBlockModel.Heading(b.optInt("level", 2), b.optString("text", "")))
                    "paragraph" -> blocks.add(ContentBlockModel.Paragraph(b.optString("text", "")))
                    "image" -> {
                        val pUrl = b.optString("proxyUrl", "")
                        val fullProxy = if (pUrl.startsWith("/")) "$baseUrl$pUrl" else pUrl
                        blocks.add(ContentBlockModel.Image(b.optString("originalUrl", ""), fullProxy, b.optString("caption", null)))
                    }
                    "video" -> {
                        blocks.add(ContentBlockModel.Video(
                            bvid = b.optString("bvid", null),
                            streamUrl = b.optString("streamUrl", null),
                            videoTrackUrl = b.optString("videoTrackUrl", null),
                            audioTrackUrl = b.optString("audioTrackUrl", null),
                            coverUrl = b.optString("coverUrl", null)
                        ))
                    }
                    "quote" -> blocks.add(ContentBlockModel.Quote(b.optString("text", "")))
                    "code" -> blocks.add(ContentBlockModel.Code(b.optString("code", ""), b.optString("language", null)))
                    "divider" -> blocks.add(ContentBlockModel.Divider)
                }
            }
        }

        ArticleDetailModel(
            id = id,
            platform = platform,
            title = data.optString("title", ""),
            authorName = data.optJSONObject("author")?.optString("name", "Focal 原生阅读"),
            authorAvatar = data.optJSONObject("author")?.optString("avatar", null),
            publishTime = data.optString("publishTime", "今日"),
            originUrl = originUrl,
            blocks = blocks,
            commentCount = data.optInt("commentCount", 0),
            likeCount = data.optInt("likeCount", 0)
        )
    }

    suspend fun fetchComments(id: String, platform: String, originUrl: String, page: Int = 1): List<UnifiedComment> = withContext(Dispatchers.IO) {
        val list = mutableListOf<UnifiedComment>()
        try {
            val req = Request.Builder()
                .url("$baseUrl/api/v1/article/comments?platform=$platform&id=$id&url=${encode(originUrl)}&page=$page")
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val data = json.optJSONArray("data")
                if (data != null) {
                    for (i in 0 until data.length()) {
                        val c = data.getJSONObject(i)
                        val subList = mutableListOf<UnifiedComment>()
                        val subArr = c.optJSONArray("subComments")
                        if (subArr != null) {
                            for (j in 0 until subArr.length()) {
                                val s = subArr.getJSONObject(j)
                                subList.add(
                                    UnifiedComment(
                                        id = s.optString("id"),
                                        authorName = s.optString("authorName"),
                                        authorAvatar = s.optString("authorAvatar"),
                                        content = s.optString("content"),
                                        publishTime = s.optString("publishTime"),
                                        likeCount = s.optInt("likeCount"),
                                        location = s.optString("location", null)
                                    )
                                )
                            }
                        }

                        list.add(
                            UnifiedComment(
                                id = c.optString("id"),
                                authorName = c.optString("authorName"),
                                authorAvatar = c.optString("authorAvatar"),
                                content = c.optString("content"),
                                publishTime = c.optString("publishTime"),
                                likeCount = c.optInt("likeCount"),
                                location = c.optString("location", null),
                                subComments = subList
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}
        list
    }

    suspend fun postComment(platformKey: String, articleId: String, text: String, cookie: String): Boolean = withContext(Dispatchers.IO) {
        try {
            val bodyJson = JSONObject().apply {
                put("platformKey", platformKey)
                put("articleId", articleId)
                put("commentText", text)
                put("userCookie", cookie)
            }
            val req = Request.Builder()
                .url("$baseUrl/api/v1/comment/post")
                .post(bodyJson.toString().toRequestBody("application/json".toMediaType()))
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            res.isSuccessful
        } catch (_: Exception) {
            false
        }
    }

    private fun encode(str: String): String = java.net.URLEncoder.encode(str, "UTF-8")
}
