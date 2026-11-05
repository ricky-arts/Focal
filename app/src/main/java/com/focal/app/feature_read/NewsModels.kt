package com.focal.app.feature_read

import com.focal.app.core.network.FocalHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

data class NewsItem(
    val id: String,
    val title: String,
    val sourceName: String,
    val sourceTag: String, // "WB" (微博), "ZH" (知乎), "HN" (Hackernews), "RT" (科技)
    val timeAgo: String,
    val hotScore: String? = null,
    val url: String
)

/**
 * 资讯聚合轻量引擎（直连各平台公开 API，无后端）
 */
object NewsRepository {

    suspend fun fetchRealHotFeeds(): List<NewsItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<NewsItem>()

        // 1. 抓取微博实时热搜公开 API
        try {
            val weiboRequest = Request.Builder()
                .url("https://weibo.com/ajax/side/hotSearch")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .header("Referer", "https://weibo.com")
                .build()

            val response = FocalHttpClient.client.newCall(weiboRequest).execute()
            if (response.isSuccessful) {
                val body = response.body?.string() ?: ""
                val json = JSONObject(body)
                val realtime = json.optJSONObject("data")?.optJSONArray("realtime")
                if (realtime != null) {
                    val limit = minOf(realtime.length(), 15)
                    for (i in 0 until limit) {
                        val obj = realtime.getJSONObject(i)
                        val word = obj.optString("word", "")
                        val num = obj.optLong("num", 0)
                        val label = obj.optString("label_name", "")
                        if (word.isNotBlank()) {
                            val scoreStr = if (num > 10000) "${num / 10000}w" else "$num"
                            result.add(
                                NewsItem(
                                    id = "wb_$i",
                                    title = word,
                                    sourceName = "微博热搜",
                                    sourceTag = "WB",
                                    timeAgo = if (label.isNotBlank()) label else "热",
                                    hotScore = scoreStr,
                                    url = "https://s.weibo.com/weibo?q=%23${word}%23"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // 网络异常时走内置兜底流
        }

        // 2. 抓取 HackerNews 热门科技资讯公开官方 API
        try {
            val hnRequest = Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/topstories.json")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()

            val hnResponse = FocalHttpClient.client.newCall(hnRequest).execute()
            if (hnResponse.isSuccessful) {
                val body = hnResponse.body?.string() ?: "[]"
                val idsArray = org.json.JSONArray(body)
                val topIds = minOf(idsArray.length(), 6)
                for (j in 0 until topIds) {
                    val itemId = idsArray.getLong(j)
                    val itemReq = Request.Builder()
                        .url("https://hacker-news.firebaseio.com/v0/item/$itemId.json")
                        .build()
                    val itemRes = FocalHttpClient.client.newCall(itemReq).execute()
                    if (itemRes.isSuccessful) {
                        val itemObj = JSONObject(itemRes.body?.string() ?: "{}")
                        val title = itemObj.optString("title", "")
                        val score = itemObj.optInt("score", 0)
                        val link = itemObj.optString("url", "https://news.ycombinator.com/item?id=$itemId")
                        if (title.isNotBlank()) {
                            result.add(
                                NewsItem(
                                    id = "hn_$itemId",
                                    title = title,
                                    sourceName = "Hacker News",
                                    sourceTag = "HN",
                                    timeAgo = "Top",
                                    hotScore = "${score}pts",
                                    url = link
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // 容错处理
        }

        // 如果全部请求失败或无网络，回退到离线种子数据保证可用性
        if (result.isEmpty()) {
            result.addAll(getFallbackFeeds())
        }
        result
    }

    fun getFallbackFeeds(): List<NewsItem> {
        return listOf(
            NewsItem(
                id = "fb_1",
                title = "神舟载人航天最新实验成果发布，全面迈入应用与发展新阶段",
                sourceName = "微博热搜",
                sourceTag = "WB",
                timeAgo = "10m",
                hotScore = "482w",
                url = "https://s.weibo.com"
            ),
            NewsItem(
                id = "fb_2",
                title = "全球AI大模型最新对齐研究：兼顾推理速度与低显存消耗",
                sourceName = "知乎精选",
                sourceTag = "ZH",
                timeAgo = "25m",
                hotScore = "920",
                url = "https://www.zhihu.com"
            ),
            NewsItem(
                id = "fb_3",
                title = "开源大模型与边缘计算架构的技术落地与演进趋势",
                sourceName = "HackerNews",
                sourceTag = "HN",
                timeAgo = "1h",
                hotScore = "340 pts",
                url = "https://news.ycombinator.com"
            )
        )
    }
}
