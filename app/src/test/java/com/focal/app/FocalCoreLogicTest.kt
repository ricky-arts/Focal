package com.focal.app

import com.focal.app.core.sources.ToutiaoSourceParser
import com.focal.app.core.sources.WeiboSourceParser
import com.focal.app.core.sources.ZhihuSourceParser
import com.focal.app.feature_read.NewsCategory
import com.focal.app.feature_read.NewsRepository
import kotlinx.coroutines.runBlocking
import org.junit.Assert.*
import org.junit.Test

class FocalCoreLogicTest {

    @Test
    fun testNewsRepositoryMultiPlatformAggregation() {
        val news = NewsRepository.getFallbackFeeds()
        assertTrue("News feeds should not be empty", news.isNotEmpty())
        val tags = news.map { it.sourceTag }.toSet()
        assertTrue("Should contain Toutiao (TT)", tags.contains("TT"))
        assertTrue("Should contain Weibo (WB)", tags.contains("WB"))
        assertTrue("Should contain Zhihu (ZH)", tags.contains("ZH"))
        for (i in 0 until news.size - 1) {
            assertTrue("Items must be sorted by hot score descending", news[i].hotScoreNum >= news[i + 1].hotScoreNum)
        }
    }

    @Test
    fun testIndividualSourceParsersWithThumbnails() = runBlocking {
        val ttParser = ToutiaoSourceParser()
        val ttItems = ttParser.fetchItems(page = 1, pageSize = 5)
        assertTrue(ttItems.isNotEmpty())
        assertTrue("Toutiao items should contain thumbnails", ttItems.any { !it.thumbnailUrl.isNullOrBlank() })

        val wbParser = WeiboSourceParser()
        val wbItems = wbParser.fetchItems(page = 1, pageSize = 5)
        assertTrue(wbItems.isNotEmpty())
        assertTrue("Weibo items should contain thumbnails", wbItems.any { !it.thumbnailUrl.isNullOrBlank() })

        val zhParser = ZhihuSourceParser()
        val zhItems = zhParser.fetchItems(page = 1, pageSize = 5)
        assertTrue(zhItems.isNotEmpty())
        assertTrue("Zhihu items should contain thumbnails", zhItems.any { !it.thumbnailUrl.isNullOrBlank() })
    }

    @Test
    fun testNewsCategoryInference() {
        assertEquals(NewsCategory.TECH, NewsRepository.inferCategory("最新AI大模型推理架构发布"))
        assertEquals(NewsCategory.SPORTS, NewsRepository.inferCategory("世界杯总决赛精彩进球集锦"))
        assertEquals(NewsCategory.ENTERTAINMENT, NewsRepository.inferCategory("年度重磅院线电影首映票房破纪录"))
        assertEquals(NewsCategory.FINANCE, NewsRepository.inferCategory("美联储宣布最新降息政策与A股行情分析"))
    }
}
