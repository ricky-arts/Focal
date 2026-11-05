package com.focal.app.feature_listen

import com.focal.app.core.network.FocalHttpClient
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import okhttp3.Request
import org.json.JSONObject

data class AudioItem(
    val id: String,
    val title: String,
    val narratorOrHost: String,
    val categoryTag: String, // "BOOK" (听书), "CAST" (播客)
    val duration: String,
    val episodeCount: String,
    val streamUrl: String
)

object AudioRepository {

    suspend fun fetchRealAudioList(): List<AudioItem> = withContext(Dispatchers.IO) {
        val result = mutableListOf<AudioItem>()

        // 1. 请求官方公开 Podcast API (Apple/iTunes 开放接口，无鉴权限制)
        try {
            val req = Request.Builder()
                .url("https://itunes.apple.com/search?term=technology&media=podcast&limit=6")
                .header("User-Agent", FocalHttpClient.DEFAULT_USER_AGENT)
                .build()

            val res = FocalHttpClient.client.newCall(req).execute()
            if (res.isSuccessful) {
                val body = res.body?.string() ?: ""
                val json = JSONObject(body)
                val resultsArr = json.optJSONArray("results")
                if (resultsArr != null) {
                    for (i in 0 until resultsArr.length()) {
                        val obj = resultsArr.getJSONObject(i)
                        val trackName = obj.optString("trackName", "")
                        val artistName = obj.optString("artistName", "播客主")
                        val feedUrl = obj.optString("feedUrl", "")
                        val trackCount = obj.optInt("trackCount", 1)

                        if (trackName.isNotBlank()) {
                            result.add(
                                AudioItem(
                                    id = "cast_$i",
                                    title = trackName,
                                    narratorOrHost = artistName,
                                    categoryTag = "CAST",
                                    duration = "更新中",
                                    episodeCount = "共${trackCount}期",
                                    streamUrl = feedUrl
                                )
                            )
                        }
                    }
                }
            }
        } catch (_: Exception) {
            // 异常兜底
        }

        // 2. 结合精选经典有声书
        result.addAll(getFallbackBooks())

        if (result.isEmpty()) {
            result.addAll(getFallbackAudioList())
        }
        result
    }

    private fun getFallbackBooks(): List<AudioItem> {
        return listOf(
            AudioItem(
                id = "book_1",
                title = "三体 · 全景声广播剧第一季（精编解说）",
                narratorOrHost = "科幻有声工坊",
                categoryTag = "BOOK",
                duration = "45:00",
                episodeCount = "共18集",
                streamUrl = "https://example.com/audio/three_body.mp3"
            ),
            AudioItem(
                id = "book_2",
                title = "明朝那些事儿 · 洪武大帝篇",
                narratorOrHost = "经典名著演播",
                categoryTag = "BOOK",
                duration = "32:15",
                episodeCount = "第12回",
                streamUrl = "https://example.com/audio/ming.mp3"
            )
        )
    }

    fun getFallbackAudioList(): List<AudioItem> {
        return listOf(
            AudioItem(
                id = "fb_a1",
                title = "津津乐道 · 从零打造属于你的个人知识库与信息流",
                narratorOrHost = "津津乐道播客",
                categoryTag = "CAST",
                duration = "1:12:30",
                episodeCount = "Vol.248",
                streamUrl = "https://example.com/audio/jjld.mp3"
            ),
            AudioItem(
                id = "fb_a2",
                title = "声东击西 · 观察科技革命浪潮下的个体思考",
                narratorOrHost = "声东击西",
                categoryTag = "CAST",
                duration = "58:40",
                episodeCount = "EP310",
                streamUrl = "https://example.com/audio/sdjx.mp3"
            )
        ) + getFallbackBooks()
    }
}
