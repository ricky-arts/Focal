package com.focal.app.core.storage

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * 用户自定义喜好与三方身份持久化中心
 * 1. 栏目偏好：用户自定义勾选感兴趣的品类（综合、娱乐、体育、科技、财经、国际）
 * 2. 平台源偏好：头条、微博、百度、知乎、抖音、B站、番茄畅听、播客等
 * 3. 身份对接：豆包、Kimi、千问、元宝网页登录拦截的鉴权 Session 硬件级加密存储
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
