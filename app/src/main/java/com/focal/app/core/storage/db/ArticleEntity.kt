package com.focal.app.core.storage.db

import androidx.room.*

@Entity(tableName = "cached_articles")
data class ArticleEntity(
    @PrimaryKey val id: String,
    val platform: String,
    val title: String,
    val authorName: String?,
    val authorAvatar: String?,
    val publishTime: String,
    val originUrl: String,
    val contentBlocksJson: String,
    val commentCount: Int,
    val cachedAt: Long = System.currentTimeMillis()
)

@Dao
interface ArticleDao {
    @Query("SELECT * FROM cached_articles WHERE id = :id LIMIT 1")
    suspend fun getArticleById(id: String): ArticleEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertArticle(article: ArticleEntity)

    @Query("DELETE FROM cached_articles WHERE cachedAt < :expireTimestamp")
    suspend fun deleteExpiredArticles(expireTimestamp: Long)
}

@Database(entities = [ArticleEntity::class], version = 1, exportSchema = false)
abstract class FocalRoomDatabase : RoomDatabase() {
    abstract fun articleDao(): ArticleDao
}
