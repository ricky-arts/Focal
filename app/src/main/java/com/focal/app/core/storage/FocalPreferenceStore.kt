package com.focal.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 用户自定义喜好与三方身份持久化中心
 * 1. 栏目偏好：用户自定义勾选感兴趣的品类
 * 2. 平台源偏好：头条、微博、百度、知乎、抖音、B站、番茄畅听、喜马拉雅等
 * 3. 视觉定制：主题模式、自定义背景壁纸、背景透明度 (0.0~1.0)、对比度 (0.5~1.5)
 * 4. 身份对接：豆包、Kimi、千问、元宝网页登录拦截的鉴权 Session 硬件级加密存储
 */
class FocalPreferenceStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "focal_secure_prefs",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    private val commonPrefs: SharedPreferences = context.getSharedPreferences(
        "focal_user_prefs",
        Context.MODE_PRIVATE
    )

    // --- 主题模式 ---
    fun getThemeMode(): String {
        return commonPrefs.getString("app_theme_mode", "DARK") ?: "DARK"
    }

    fun setThemeMode(mode: String) {
        commonPrefs.edit().putString("app_theme_mode", mode).apply()
    }

    // --- 自定义背景壁纸与滤镜系统 ---
    fun getWallpaperKey(): String {
        return commonPrefs.getString("wallpaper_key", "default") ?: "default"
    }

    fun setWallpaperKey(key: String) {
        commonPrefs.edit().putString("wallpaper_key", key).apply()
    }

    fun getWallpaperOpacity(): Float {
        return commonPrefs.getFloat("wallpaper_opacity", 0.45f) // 默认 45% 透明度，内敛高雅
    }

    fun setWallpaperOpacity(opacity: Float) {
        commonPrefs.edit().putFloat("wallpaper_opacity", opacity).apply()
    }

    fun getWallpaperContrast(): Float {
        return commonPrefs.getFloat("wallpaper_contrast", 1.0f) // 默认 1.0x 标准对比度
    }

    fun setWallpaperContrast(contrast: Float) {
        commonPrefs.edit().putFloat("wallpaper_contrast", contrast).apply()
    }

    // --- 资讯栏目偏好 ---
    fun isCategoryEnabled(categoryKey: String, default: Boolean = true): Boolean {
        return commonPrefs.getBoolean("cat_$categoryKey", default)
    }

    fun setCategoryEnabled(categoryKey: String, enabled: Boolean) {
        commonPrefs.edit().putBoolean("cat_$categoryKey", enabled).apply()
    }

    fun getEnabledCategories(): Set<String> {
        val allCats = listOf("general", "entertainment", "sports", "tech", "finance", "world")
        return allCats.filter { isCategoryEnabled(it, true) }.toSet()
    }

    // --- 平台源启用状态 ---
    fun isSourceEnabled(sourceKey: String, default: Boolean = true): Boolean {
        return commonPrefs.getBoolean("source_$sourceKey", default)
    }

    fun setSourceEnabled(sourceKey: String, enabled: Boolean) {
        commonPrefs.edit().putBoolean("source_$sourceKey", enabled).apply()
    }

    // --- AI 身份对接持久化（Cookie / Token / Session）---
    fun getAiAuthSession(platformKey: String): String? {
        return securePrefs.getString("auth_$platformKey", null)
    }

    fun saveAiAuthSession(platformKey: String, sessionData: String) {
        securePrefs.edit().putString("auth_$platformKey", sessionData).apply()
    }

    fun clearAiAuthSession(platformKey: String) {
        securePrefs.edit().remove("auth_$platformKey").apply()
    }

    fun isAiAuthenticated(platformKey: String): Boolean {
        val s = getAiAuthSession(platformKey)
        return !s.isNullOrBlank()
    }
}
