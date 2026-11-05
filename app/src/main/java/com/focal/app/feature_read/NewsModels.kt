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
 * 包含：今日头条、微博热搜、百度热搜、知乎热榜、Reddit
 */
object NewsRepository {

    private val toutiaoParser = ToutiaoSourceParser()
    private val weiboParser = WeiboSourceParser()
    private val baiduParser = BaiduSourceParser()
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
        val bdDeferred = async { if (prefStore.isSourceEnabled("baidu", true)) baiduParser.fetchItems(page, pageSize) else emptyList() }
        val zhDeferred = async { if (prefStore.isSourceEnabled("zhihu", true)) zhihuParser.fetchItems(page, pageSize) else emptyList() }
        val rdDeferred = async { if (prefStore.isSourceEnabled("reddit", true)) redditParser.fetchItems(page, pageSize) else emptyList() }

        val allLists = awaitAll(ttDeferred, wbDeferred, bdDeferred, zhDeferred, rdDeferred)
        val aggregated = allLists.flatten().toMutableList()

        if (aggregated.isEmpty()) {
            aggregated.addAll(getFallbackFeeds(page))
        }

        // 优先按启用的栏目过滤，若过滤后太少，则自动追加综合高热资讯，保证列表永不为空
        val filtered = aggregated.filter { item ->
            enabledCats.isEmpty() || enabledCats.contains(item.category.key) || item.category == NewsCategory.GENERAL
        }.toMutableList()

        if (filtered.isEmpty()) {
            filtered.addAll(aggregated)
        }

        filtered.sortedByDescending { it.hotScoreNum }
    }

    fun inferCategory(title: String): NewsCategory {
        val lower = title.lowercase()
        return when {
            lower.contains("剧") || lower.contains("票房") || lower.contains("明星") ||
            lower.contains("演唱会") || lower.contains("影") || lower.contains("金像奖") ||
            lower.contains("脱口秀") || lower.contains("歌手") || lower.contains("恋情") -> NewsCategory.ENTERTAINMENT

            lower.contains("足球") || lower.contains("男篮") || lower.contains("奥运") ||
            lower.contains("世界杯") || lower.contains("总决赛") || lower.contains("乒乓") ||
            lower.contains("马拉松") || lower.contains("夺冠") || lower.contains("集训") -> NewsCategory.SPORTS

            lower.contains("ai") || lower.contains("大模型") || lower.contains("芯片") ||
            lower.contains("手机") || lower.contains("发布会") || lower.contains("算力") ||
            lower.contains("半导体") || lower.contains("算法") || lower.contains("工程") ||
            lower.contains("神舟") || lower.contains("航天") -> NewsCategory.TECH

            lower.contains("股市") || lower.contains("a股") || lower.contains("降息") ||
            lower.contains("财报") || lower.contains("基金") || lower.contains("央行") ||
            lower.contains("美联储") || lower.contains("房贷") || lower.contains("投资") ||
            lower.contains("促消费") || lower.contains("交通") -> NewsCategory.FINANCE

            lower.contains("联合国") || lower.contains("白宫") || lower.contains("欧洲") ||
            lower.contains("巴以") || lower.contains("外交") || lower.contains("海外") ||
            lower.contains("大选") || lower.contains("气候") || lower.contains("energy") -> NewsCategory.WORLD

            else -> NewsCategory.GENERAL
        }
    }

    fun getFallbackFeeds(page: Int = 1): List<NewsItem> {
        val base = 9000000L - page * 500000L
        return listOf(
            NewsItem("tt_s_$page", "今日头条精选：全国新一轮扩内需促消费政策落地，文旅产业迎全面繁荣", "今日头条", "TT", NewsCategory.GENERAL, "热议", base + 800000L, "980w", "https://www.toutiao.com", "https://picsum.photos/seed/tt_s$page/300/200"),
            NewsItem("wb_s_$page", "神舟载人航天最新实验成果发布：太空生命科学取得里程碑式突破", "微博热搜", "WB", NewsCategory.TECH, "爆", base + 600000L, "960w", "https://s.weibo.com", "https://picsum.photos/seed/wb_s$page/300/200"),
            NewsItem("bd_s_$page", "全网聚焦：高科技制造业前三季度投资增长强劲，高端装备制造加快布局", "百度热搜", "BD", NewsCategory.FINANCE, "热榜", base + 500000L, "950w", "https://www.baidu.com", "https://picsum.photos/seed/bd_s$page/300/200"),
            NewsItem("zh_s_$page", "如何评价当下大模型轻量化端侧部署演进趋势？千亿参数本地跑通", "知乎", "ZH", NewsCategory.TECH, "深度", base + 400000L, "940w", "https://www.zhihu.com", "https://picsum.photos/seed/zh_s$page/300/200"),
            NewsItem("rd_s_$page", "Global Clean Energy Milestone: Solar and Wind Cross Generation Record", "Reddit", "RD", NewsCategory.WORLD, "World", base + 200000L, "5.2kup", "https://reddit.com", "https://picsum.photos/seed/rd_s$page/300/200"),
            NewsItem("tt_s2_$page", "新赛季羽毛球大师赛中国队全线告捷，国羽双打强势会师冠亚军决赛", "今日头条", "TT", NewsCategory.SPORTS, "体育", base + 100000L, "910w", "https://www.toutiao.com", "https://picsum.photos/seed/tt_s2$page/300/200"),
            NewsItem("wb_s2_$page", "多部高口碑院线大片正式官宣密钥延期，全网总票房已突破百亿", "微博热搜", "WB", NewsCategory.ENTERTAINMENT, "文娱", base, "900w", "https://s.weibo.com", "https://picsum.photos/seed/wb_s2$page/300/200")
        )
    }
}
