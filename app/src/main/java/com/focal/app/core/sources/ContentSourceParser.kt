package com.focal.app.core.sources

import com.focal.app.core.content.ContentType
import com.focal.app.feature_read.NewsCategory
import com.focal.app.feature_read.NewsItem

/**
 * 统一多数据源解析器接口
 * 每个数据源独立适配器，负责：
 * 1. 抓取与分页加载 (page, pageSize)；
 * 2. 提取首图、封面缩略图；
 * 3. 区分内容类型 (ARTICLE / VIDEO / GALLERY)
 */
interface ContentSourceParser {
    val sourceTag: String
    val sourceName: String

    suspend fun fetchItems(page: Int, pageSize: Int): List<NewsItem>
}
