package com.focal.app

import com.focal.app.feature_ask.AiPlatform
import com.focal.app.feature_read.NewsRepository
import com.focal.app.feature_watch.VideoRepository
import com.focal.app.feature_listen.AudioRepository
import org.junit.Assert.*
import org.junit.Test

class FocalCoreLogicTest {

    @Test
    fun testNewsRepositoryAggregation() {
        val news = NewsRepository.getFallbackFeeds()
        assertTrue("News feeds should not be empty", news.isNotEmpty())
        assertTrue("Should contain multiple sources", news.any { it.sourceTag == "WB" })
    }

    @Test
    fun testVideoRepositoryAggregation() {
        val videos = VideoRepository.getFallbackFeeds()
        assertTrue("Video feeds should not be empty", videos.isNotEmpty())
        assertTrue("Should contain Douyin videos", videos.any { it.platformTag == "DY" })
        assertTrue("Should contain Bilibili videos", videos.any { it.platformTag == "BILI" })
    }

    @Test
    fun testAudioRepositoryAggregation() {
        val audios = AudioRepository.getFallbackAudioList()
        assertTrue("Audios should not be empty", audios.isNotEmpty())
        assertTrue("Should contain audiobook item", audios.any { it.categoryTag == "BOOK" })
        assertTrue("Should contain podcast item", audios.any { it.categoryTag == "CAST" })
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
