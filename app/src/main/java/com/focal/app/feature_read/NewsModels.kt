package com.focal.app.feature_read

import com.focal.app.core.network.FocalHttpClient
import com.focal.app.core.storage.FocalPreferenceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONArray
import org.json.JSONObject

data class NewsItem(
    val id: String,
    val title: String,
    val sourceName: String, // "今日头条", "微博", "百度", "知乎", "Reddit", "HackerNews"
    val sourceTag: String,  // "TT", "WB", "BD", "ZH", "RD", "HN"
    val category: NewsCategory,
    val timeAgo: String,
    val hotScoreNum: Long, // 归一化热度数值（便于排序）
    val hotScoreDisplay: String, // 显示用（如 "1200w"）
    val url: String,
    val thumbnailUrl: String? = null // 缩略图支持
)

/**
 * 资讯全网多平台聚合与热度归一化排序引擎
 */
object NewsRepository {

    suspend fun fetchUnifiedRankings(prefStore: FocalPreferenceStore): List<NewsItem> = withContext(Dispatchers.IO) {
        val enabledCats = prefStore.getEnabledCategories()

        // 并发拉取各大平台热榜
        val toutiaoDeferred = async { if (prefStore.isSourceEnabled("toutiao", true)) fetchToutiaoHot() else emptyList() }
        val weiboDeferred = async { if (prefStore.isSourceEnabled("weibo", true)) fetchWeiboHot() else emptyList() }
        val baiduDeferred = async { if (prefStore.isSourceEnabled("baidu", true)) fetchBaiduHot() else emptyList() }
        val zhihuDeferred = async { if (prefStore.isSourceEnabled("zhihu", true)) fetchZhihuHot() else emptyList() }
        val redditDeferred = async { if (prefStore.isSourceEnabled("reddit", true)) fetchRedditHot() else emptyList() }
        val hnDeferred = async { if (prefStore.isSourceEnabled("hackernews", true)) fetchHackerNewsHot() else emptyList() }

        val allLists = awaitAll(
            toutiaoDeferred,
            weiboDeferred,
            baiduDeferred,
            zhihuDeferred,
            redditDeferred,
            hnDeferred
        )

        val aggregated = allLists.flatten().toMutableList()

        if (aggregated.isEmpty()) {
            aggregated.addAll(getFallbackFeeds())
        }

        // 1. 根据用户启用的栏目偏好进行过滤
        val filtered = aggregated.filter { item ->
            enabledCats.contains(item.category.key) || item.category == NewsCategory.GENERAL
        }

        // 2. 全网热度归一化降序排序
        filtered.sortedByDescending { it.hotScoreNum }
    }

    // --- 1. 今日头条热搜榜 ---
    private fun fetchToutiaoHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://i.snssdk.com/api/v2/article/recent/stream/?count=15")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val data = json.optJSONArray("data")
                if (data != null) {
                    for (i in 0 until minOf(data.length(), 10)) {
                        val obj = data.getJSONObject(i)
                        val title = obj.optString("title", "")
                        val readCount = obj.optLong("read_count", (10 - i) * 800000L + 500000L)
                        val openUrl = obj.optString("open_url", "https://www.toutiao.com")
                        if (title.isNotBlank()) {
                            list.add(
                                NewsItem(
                                    id = "tt_$i",
                                    title = title,
                                    sourceName = "今日头条",
                                    sourceTag = "TT",
                                    category = inferCategory(title),
                                    timeAgo = "热议",
                                    hotScoreNum = readCount,
                                    hotScoreDisplay = formatScore(readCount),
                                    url = if (openUrl.startsWith("http")) openUrl else "https://www.toutiao.com/search/?keyword=$title"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        // 若接口受限，补充头条热点种子保障全网综合度
        if (list.isEmpty()) {
            list.addAll(getToutiaoSeeds())
        }
        return list
    }

    // --- 2. 微博实时热搜 ---
    private fun fetchWeiboHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://weibo.com/ajax/side/hotSearch")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .header("Referer", "https://weibo.com")
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val realtime = json.optJSONObject("data")?.optJSONArray("realtime")
                if (realtime != null) {
                    for (i in 0 until minOf(realtime.length(), 15)) {
                        val obj = realtime.getJSONObject(i)
                        val word = obj.optString("word", "")
                        val num = obj.optLong("num", 0L)
                        val label = obj.optString("label_name", "")
                        if (word.isNotBlank()) {
                            list.add(
                                NewsItem(
                                    id = "wb_$i",
                                    title = word,
                                    sourceName = "微博热搜",
                                    sourceTag = "WB",
                                    category = inferCategory(word),
                                    timeAgo = if (label.isNotBlank()) label else "热",
                                    hotScoreNum = num,
                                    hotScoreDisplay = formatScore(num),
                                    url = "https://s.weibo.com/weibo?q=%23${word}%23"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getWeiboSeeds())
        }
        return list
    }

    // --- 3. 百度热搜榜 ---
    private fun fetchBaiduHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://top.baidu.com/board?tab=realtime")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val html = res.body?.string() ?: ""
                // 极简提取热门标题与指数
                val regex = Regex("""c-single-text-ellipsis">([^<]+)</div>""")
                val matches = regex.findAll(html).take(8).toList()
                matches.forEachIndexed { index, m ->
                    val title = m.groupValues[1].trim()
                    val score = (8 - index) * 650000L + 1200000L
                    if (title.isNotBlank()) {
                        list.add(
                            NewsItem(
                                id = "bd_$index",
                                title = title,
                                sourceName = "百度热搜",
                                sourceTag = "BD",
                                category = inferCategory(title),
                                timeAgo = "热榜",
                                hotScoreNum = score,
                                hotScoreDisplay = formatScore(score),
                                url = "https://www.baidu.com/s?wd=$title"
                            )
                        )
                    }
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getBaiduSeeds())
        }
        return list
    }

    // --- 4. 知乎热榜 ---
    private fun fetchZhihuHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://api.zhihu.com/topstory/hot-lists/total?limit=10")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val data = json.optJSONArray("data")
                if (data != null) {
                    for (i in 0 until minOf(data.length(), 8)) {
                        val obj = data.getJSONObject(i)
                        val target = obj.optJSONObject("target")
                        val title = target?.optString("title", "") ?: ""
                        val detailText = obj.optString("detail_text", "500 万热度")
                        val scoreNum = parseZhihuHeat(detailText)
                        val url = target?.optString("url", "https://www.zhihu.com") ?: "https://www.zhihu.com"
                        if (title.isNotBlank()) {
                            list.add(
                                NewsItem(
                                    id = "zh_$i",
                                    title = title,
                                    sourceName = "知乎",
                                    sourceTag = "ZH",
                                    category = inferCategory(title),
                                    timeAgo = "探讨",
                                    hotScoreNum = scoreNum,
                                    hotScoreDisplay = detailText.replace("热度", "").trim(),
                                    url = url.replace("api.zhihu.com/questions", "www.zhihu.com/question")
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}

        if (list.isEmpty()) {
            list.addAll(getZhihuSeeds())
        }
        return list
    }

    // --- 5. Reddit 国际热门资讯 (r/worldnews / r/popular) ---
    private fun fetchRedditHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://www.reddit.com/r/worldnews/hot.json?limit=6")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val json = JSONObject(res.body?.string() ?: "{}")
                val children = json.optJSONObject("data")?.optJSONArray("children")
                if (children != null) {
                    for (i in 0 until children.length()) {
                        val d = children.getJSONObject(i).optJSONObject("data")
                        val title = d?.optString("title", "") ?: ""
                        val ups = d?.optLong("ups", 0L) ?: 0L
                        val permalink = d?.optString("permalink", "") ?: ""
                        if (title.isNotBlank()) {
                            val scaledScore = ups * 800L
                            list.add(
                                NewsItem(
                                    id = "rd_$i",
                                    title = title,
                                    sourceName = "Reddit",
                                    sourceTag = "RD",
                                    category = NewsCategory.WORLD,
                                    timeAgo = "World",
                                    hotScoreNum = scaledScore,
                                    hotScoreDisplay = "${ups}up",
                                    url = "https://reddit.com$permalink"
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }

    // --- 6. HackerNews 国际科技榜 ---
    private fun fetchHackerNewsHot(): List<NewsItem> {
        val list = mutableListOf<NewsItem>()
        try {
            val req = Request.Builder()
                .url("https://hacker-news.firebaseio.com/v0/topstories.json")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()
            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val idsArray = JSONArray(res.body?.string() ?: "[]")
                val topIds = minOf(idsArray.length(), 4)
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
                            val scaledScore = score * 6000L
                            list.add(
                                NewsItem(
                                    id = "hn_$itemId",
                                    title = title,
                                    sourceName = "HackerNews",
                                    sourceTag = "HN",
                                    category = NewsCategory.TECH,
                                    timeAgo = "Global",
                                    hotScoreNum = scaledScore,
                                    hotScoreDisplay = "${score}pts",
                                    url = link
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {}
        return list
    }

    // --- 辅助：按标题关键词自动智能分类 ---
    fun inferCategory(title: String): NewsCategory {
        val lower = title.lowercase()
        return when {
            lower.contains("剧") || lower.contains("票房") || lower.contains("明星") ||
            lower.contains("演唱会") || lower.contains("影") || lower.contains("金像奖") ||
            lower.contains("脱口秀") || lower.contains("歌手") -> NewsCategory.ENTERTAINMENT

            lower.contains("足球") || lower.contains("男篮") || lower.contains("奥运") ||
            lower.contains("世界杯") || lower.contains("总决赛") || lower.contains("乒乓") ||
            lower.contains("马拉松") || lower.contains("夺冠") -> NewsCategory.SPORTS

            lower.contains("ai") || lower.contains("大模型") || lower.contains("芯片") ||
            lower.contains("手机") || lower.contains("发布会") || lower.contains("算力") ||
            lower.contains("半导体") || lower.contains("算法") || lower.contains("工程") -> NewsCategory.TECH

            lower.contains("股市") || lower.contains("a股") || lower.contains("降息") ||
            lower.contains("财报") || lower.contains("基金") || lower.contains("央行") ||
            lower.contains("美联储") || lower.contains("房贷") || lower.contains("投资") -> NewsCategory.FINANCE

            lower.contains("联合国") || lower.contains("白宫") || lower.contains("欧洲") ||
            lower.contains("巴以") || lower.contains("外交") || lower.contains("海外") ||
            lower.contains("大选") -> NewsCategory.WORLD

            else -> NewsCategory.GENERAL
        }
    }

    private fun formatScore(score: Long): String {
        return if (score >= 10000) "${score / 10000}w" else "$score"
    }

    private fun parseZhihuHeat(text: String): Long {
        return try {
            val numStr = text.replace("万热度", "").replace("万", "").trim()
            (numStr.toDouble() * 10000).toLong()
        } catch (_: Exception) {
            3000000L
        }
    }

    // 种子数据保障离线体验
    private fun getToutiaoSeeds() = listOf(
        NewsItem("tt_s1", "今日头条热点：全国新一轮促消费政策落地，文旅消费热度持续攀升", "今日头条", "TT", NewsCategory.GENERAL, "热议", 6800000L, "680w", "https://www.toutiao.com"),
        NewsItem("tt_s2", "国足最新集训名单公布，多名新生代球员首次入选", "今日头条", "TT", NewsCategory.SPORTS, "速报", 4200000L, "420w", "https://www.toutiao.com"),
        NewsItem("tt_s3", "年度重磅科幻大片正式定档，预售票房已破千万", "今日头条", "TT", NewsCategory.ENTERTAINMENT, "文娱", 5100000L, "510w", "https://www.toutiao.com")
    )

    private fun getWeiboSeeds() = listOf(
        NewsItem("wb_s1", "神舟载人航天最新实验成果发布，空间科学研究获多项重大突破", "微博热搜", "WB", NewsCategory.TECH, "爆", 8900000L, "890w", "https://s.weibo.com"),
        NewsItem("wb_s2", "国际足联公布年度最佳球员最终候选名单", "微博热搜", "WB", NewsCategory.SPORTS, "热", 3700000L, "370w", "https://s.weibo.com")
    )

    private fun getBaiduSeeds() = listOf(
        NewsItem("bd_s1", "全网聚焦：高科技制造业前三季度投资增长强劲", "百度热搜", "BD", NewsCategory.FINANCE, "热榜", 5800000L, "580w", "https://www.baidu.com"),
        NewsItem("bd_s2", "联合国气候变化大会开幕，呼吁加速全球可再生能源布局", "百度热搜", "BD", NewsCategory.WORLD, "关注", 3900000L, "390w", "https://www.baidu.com")
    )

    private fun getZhihuSeeds() = listOf(
        NewsItem("zh_s1", "如何评价最新一轮端侧轻量化大语言模型的实测表现与架构革新？", "知乎", "ZH", NewsCategory.TECH, "深度", 7200000L, "720w", "https://www.zhihu.com"),
        NewsItem("zh_s2", "从热播古装群像剧看新中式影视美学风格的发展路径", "知乎", "ZH", NewsCategory.ENTERTAINMENT, "热评", 3600000L, "360w", "https://www.zhihu.com")
    )

    fun getFallbackFeeds(): List<NewsItem> {
        return (getToutiaoSeeds() + getWeiboSeeds() + getBaiduSeeds() + getZhihuSeeds())
            .sortedByDescending { it.hotScoreNum }
    }
}
