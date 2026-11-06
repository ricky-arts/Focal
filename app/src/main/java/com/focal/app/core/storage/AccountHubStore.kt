package com.focal.app.core.storage

import android.content.Context
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

class AccountHubStore(context: Context) {
    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val securePrefs = EncryptedSharedPreferences.create(
        context,
        "focal_account_hub_secure",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    fun savePlatformCookie(platformKey: String, cookie: String) {
        securePrefs.edit().putString("cookie_$platformKey", cookie).apply()
    }

    fun getPlatformCookie(platformKey: String): String? {
        return securePrefs.getString("cookie_$platformKey", null)
    }

    fun isBound(platformKey: String): Boolean = !getPlatformCookie(platformKey).isNullOrBlank()

    fun removePlatform(platformKey: String) {
        securePrefs.edit().remove("cookie_$platformKey").apply()
    }
}
