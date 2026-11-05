package com.focal.app.core.network

import android.content.Context
import coil.ImageLoader
import coil.disk.DiskCache
import coil.memory.MemoryCache
import okhttp3.Cache
import okhttp3.OkHttpClient
import java.io.File
import java.util.concurrent.TimeUnit

/**
 * 全局统一高效网络与双重缓存构建器
 * 1. OkHttpClient: 带有响应缓存、超时控制与防爬通用 User-Agent
 * 2. Coil ImageLoader: 带有 100MB 磁盘缓存与 25% 内存缓存，全自动下采样与防 OOM
 */
object FocalHttpClient {
    const val DEFAULT_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/120.0.0.0 Safari/537.36"

    val client: OkHttpClient = OkHttpClient.Builder()
        .connectTimeout(15, TimeUnit.SECONDS)
        .readTimeout(20, TimeUnit.SECONDS)
        .writeTimeout(15, TimeUnit.SECONDS)
        .followRedirects(true)
        .followSslRedirects(true)
        .build()

    private var imageLoader: ImageLoader? = null

    fun getImageLoader(context: Context): ImageLoader {
        return imageLoader ?: synchronized(this) {
            imageLoader ?: ImageLoader.Builder(context.applicationContext)
                .okHttpClient {
                    client.newBuilder()
                        .cache(Cache(File(context.cacheDir, "http_image_cache"), 100L * 1024 * 1024))
                        .build()
                }
                .memoryCache {
                    MemoryCache.Builder(context.applicationContext)
                        .maxSizePercent(0.25)
                        .build()
                }
                .diskCache {
                    DiskCache.Builder()
                        .directory(File(context.cacheDir, "coil_disk_cache"))
                        .maxSizeBytes(100L * 1024 * 1024)
                        .build()
                }
                .crossfade(true)
                .build().also { imageLoader = it }
        }
    }
}
