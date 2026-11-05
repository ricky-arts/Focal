package com.focal.app

import com.focal.app.core.content.ContentType
import com.focal.app.core.content.NativeContentExtractor
import com.focal.app.core.sources.ToutiaoSourceParser
import com.focal.app.core.sources.WeiboSourceParser
import com.focal.app.core.sources.ZhihuSourceParser
import com.focal.app.feature_ask.AiPlatform
import com.focal.app.feature_read.NewsCategory
import com.focal.app.feature_read.NewsRepository
import com.focal.app.feature_watch.VideoRepository
import com.focal.app.feature_listen.AudioRepository
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
        // 1. 头条解析与缩略图提取测试
        val ttParser = ToutiaoSourceParser()
        val ttItems = ttParser.fetchItems(page = 1, pageSize = 5)
        assertTrue(ttItems.isNotEmpty())
        assertTrue("Toutiao items should contain thumbnails", ttItems.any { !it.thumbnailUrl.isNullOrBlank() })

        // 2. 微博解析与缩略图测试
        val wbParser = WeiboSourceParser()
        val wbItems = wbParser.fetchItems(page = 1, pageSize = 5)
        assertTrue(wbItems.isNotEmpty())
        assertTrue("Weibo items should contain thumbnails", wbItems.any { !it.thumbnailUrl.isNullOrBlank() })

        // 3. 知乎解析与缩略图测试
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

    @Test
    fun testNativeContentExtractorMultiType() = runBlocking {
        // 1. 图文提取
        val articleRes = NativeContentExtractor.extractContent(
            id = "a_1",
            title = "最新科技突破测试报告",
            sourceName = "科技观察",
            sourceTag = "TECH",
            targetUrl = "https://example.com/article",
            preferredType = ContentType.ARTICLE
        )
        assertTrue(articleRes.isSuccess)
        val article = articleRes.getOrNull()!!
        assertEquals(ContentType.ARTICLE, article.contentType)
        assertTrue(article.blocks.isNotEmpty())

        // 2. 视频提取
        val videoRes = NativeContentExtractor.extractContent(
            id = "v_1",
            title = "B站精选视频：大模型架构演进",
            sourceName = "Bilibili",
            sourceTag = "BILI",
            targetUrl = "https://www.bilibili.com/video/BV123",
            preferredType = ContentType.VIDEO
        )
        assertTrue(videoRes.isSuccess)
        val video = videoRes.getOrNull()!!
        assertEquals(ContentType.VIDEO, video.contentType)
        assertNotNull(video.videoUrl)

        // 3. 图集提取
        val galleryRes = NativeContentExtractor.extractContent(
            id = "g_1",
            title = "川西风光精选图集",
            sourceName = "摄影专栏",
            sourceTag = "PHOTO",
            targetUrl = "https://example.com/gallery",
            preferredType = ContentType.GALLERY
        )
        assertTrue(galleryRes.isSuccess)
        val gallery = galleryRes.getOrNull()!!
        assertEquals(ContentType.GALLERY, gallery.contentType)
        assertTrue(gallery.galleryImages.isNotEmpty())
    }

    @Test
    fun testVideoRepositoryPaginationAndThumbnails() = runBlocking {
        val videos = VideoRepository.fetchRealVideoFeeds(page = 1)
        assertTrue("Video feeds should not be empty", videos.isNotEmpty())
        assertTrue("Videos should contain thumbnails", videos.any { !it.thumbnailUrl.isNullOrBlank() })
        assertTrue("Should contain Douyin videos", videos.any { it.platformTag == "DY" })
        assertTrue("Should contain Bilibili videos", videos.any { it.platformTag == "BILI" })
    }

    @Test
    fun testAudioRepositoryPaginationAndThumbnails() = runBlocking {
        val audios = AudioRepository.fetchRealAudioList(page = 1)
        assertTrue("Audios should not be empty", audios.isNotEmpty())
        assertTrue("Audios should contain thumbnails", audios.any { !it.thumbnailUrl.isNullOrBlank() })
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
