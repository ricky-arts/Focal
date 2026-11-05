package com.focal.app

import com.focal.app.feature_ask.AiPlatform
import com.focal.app.feature_read.NewsCategory
import com.focal.app.feature_read.NewsRepository
import com.focal.app.feature_watch.VideoRepository
import com.focal.app.feature_listen.AudioRepository
import org.junit.Assert.*
import org.junit.Test

class FocalCoreLogicTest {

    @Test
    fun testNewsRepositoryMultiPlatformAggregation() {
        val news = NewsRepository.getFallbackFeeds()
        assertTrue("News feeds should not be empty", news.isNotEmpty())
        // 验证多平台存在
        val tags = news.map { it.sourceTag }.toSet()
        assertTrue("Should contain Toutiao (TT)", tags.contains("TT"))
        assertTrue("Should contain Weibo (WB)", tags.contains("WB"))
        assertTrue("Should contain Baidu (BD)", tags.contains("BD"))
        assertTrue("Should contain Zhihu (ZH)", tags.contains("ZH"))
        // 验证热度数值降序排列
        for (i in 0 until news.size - 1) {
            assertTrue("Items must be sorted by hot score descending", news[i].hotScoreNum >= news[i + 1].hotScoreNum)
        }
    }

    @Test
    fun testNewsCategoryInference() {
        assertEquals(NewsCategory.TECH, NewsRepository.inferCategory("最新AI大模型推理架构发布"))
        assertEquals(NewsCategory.SPORTS, NewsRepository.inferCategory("世界杯总决赛精彩进球集锦"))
        assertEquals(NewsCategory.ENTERTAINMENT, NewsRepository.inferCategory("年度重磅院线电影首映票房破纪录"))
        assertEquals(NewsCategory.FINANCE, NewsRepository.inferCategory("美联储宣布最新降息政策与A股行情分析"))
    }

    @Test
    fun testVideoRepositoryAggregation() {
        val videos = VideoRepository.getFallbackFeeds()
        assertTrue("Video feeds should not be empty", videos.isNotEmpty())
        assertTrue("Should contain Douyin videos", videos.any { it.platformTag == "DY" })
        assertTrue("Should contain Bilibili videos", videos.any { it.platformTag == "BILI" })
    }

    @Test
    fun testAudioRepositoryFanqieAndXimalaya() {
        val audios = AudioRepository.getFallbackAudioList()
        assertTrue("Audios should not be empty", audios.isNotEmpty())
        // 验证番茄畅听与喜马拉雅
        assertTrue("Should contain Fanqie audiobook", audios.any { it.platformTag == "番茄" })
        assertTrue("Should contain Ximalaya audiobook", audios.any { it.platformTag == "喜马" })
        assertTrue("Should contain Podcast item", audios.any { it.platformTag == "播客" })
    }

    @Test
    fun testAiPlatformsDefinition() {
        val platforms = AiPlatform.values()
        assertEquals(4, platforms.size)
        val keys = platforms.map { it.key }
        assertTrue(keys.contains("doubao"))
        assertTrue(keys.contains("kimi"))
        assertTrue(keys.contains("qwen"))
        assertTrue(keys.contains("yuanbao"))
    }
}
