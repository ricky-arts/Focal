package com.focal.app.feature_read

import com.focal.app.core.sources.*
import com.focal.app.core.storage.FocalPreferenceStore
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.withContext

data class NewsItem(
    val id: String,
    val title: String,
    val sourceName: String,
    val sourceTag: String,
    val category: NewsCategory,
    val timeAgo: String,
    val hotScoreNum: Long,
    val hotScoreDisplay: String,
    val url: String,
    val thumbnailUrl: String? = null
)

/**
 * 资讯全网多源调度与分页聚合仓库
 */
object NewsRepository {

    private val toutiaoParser = ToutiaoSourceParser()
    private val weiboParser = WeiboSourceParser()
    private val zhihuParser = ZhihuSourceParser()
    private val redditParser = RedditSourceParser()

    suspend fun fetchUnifiedRankings(
        prefStore: FocalPreferenceStore,
        page: Int = 1,
        pageSize: Int = 10
    ): List<NewsItem> = withContext(Dispatchers.IO) {
        val enabledCats = prefStore.getEnabledCategories()

        val ttDeferred = async { if (prefStore.isSourceEnabled("toutiao", true)) toutiaoParser.fetchItems(page, pageSize) else emptyList() }
        val wbDeferred = async { if (prefStore.isSourceEnabled("weibo", true)) weiboParser.fetchItems(page, pageSize) else emptyList() }
        val zhDeferred = async { if (prefStore.isSourceEnabled("zhihu", true)) zhihuParser.fetchItems(page, pageSize) else emptyList() }
        val rdDeferred = async { if (prefStore.isSourceEnabled("reddit", true)) redditParser.fetchItems(page, pageSize) else emptyList() }

        val allLists = awaitAll(ttDeferred, wbDeferred, zhDeferred, rdDeferred)
        val aggregated = allLists.flatten().toMutableList()

        if (aggregated.isEmpty()) {
            aggregated.addAll(getFallbackFeeds(page))
        }

        // 按栏目偏好过滤并按热度降序
        val filtered = aggregated.filter { item ->
            enabledCats.contains(item.category.key) || item.category == NewsCategory.GENERAL
        }

        filtered.sortedByDescending { it.hotScoreNum }
    }

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

    fun getFallbackFeeds(page: Int = 1): List<NewsItem> {
        val base = 7000000L - page * 600000L
        return listOf(
            NewsItem("tt_s_$page", "今日头条热点：全国新一轮促消费政策落地，文旅消费热度持续攀升", "今日头条", "TT", NewsCategory.GENERAL, "热议", base + 300000L, "730w", "https://www.toutiao.com", "https://picsum.photos/seed/tt_s$page/300/200"),
            NewsItem("wb_s_$page", "神舟载人航天最新实验成果发布，空间科学研究获多项重大突破", "微博热搜", "WB", NewsCategory.TECH, "爆", base + 200000L, "720w", "https://s.weibo.com", "https://picsum.photos/seed/wb_s$page/300/200"),
            NewsItem("zh_s_$page", "如何评价当下大模型混合架构技术路线与生产级基准测评？", "知乎", "ZH", NewsCategory.TECH, "深度", base + 100000L, "710w", "https://www.zhihu.com", "https://picsum.photos/seed/zh_s$page/300/200")
        )
    }
}
