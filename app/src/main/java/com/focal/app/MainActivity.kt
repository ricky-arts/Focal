package com.focal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.BackHandler
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.room.Room
import com.focal.app.core.navigation.FocalSection
import com.focal.app.core.network.FocalApiClient
import com.focal.app.core.storage.AccountHubStore
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.storage.db.FocalRoomDatabase
import com.focal.app.core.theme.*
import com.focal.app.feature_detail.ProductionDetailScreen
import com.focal.app.feature_read.ReadScreen
import com.focal.app.feature_set.AuthBridgeModal
import com.focal.app.feature_set.SettingsDrawerContent
import kotlinx.coroutines.launch

data class ActiveArticleTarget(
    val id: String,
    val platform: String,
    val title: String,
    val originUrl: String
)

class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefStore = FocalPreferenceStore(this)
        val accountStore = AccountHubStore(this)
        val apiClient = FocalApiClient()
        val roomDb = Room.databaseBuilder(
            applicationContext,
            FocalRoomDatabase::class.java,
            "focal_offline_v2.db"
        ).fallbackToDestructiveMigration().build()

        setContent {
            val themeModeStr = remember { prefStore.getThemeMode() }
            val themeMode = remember(themeModeStr) {
                when (themeModeStr) {
                    "LIGHT" -> AppThemeMode.LIGHT
                    "EMERALD" -> AppThemeMode.EMERALD
                    else -> AppThemeMode.DARK
                }
            }

            FocalTheme(themeMode = themeMode) {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var currentSection by remember { mutableStateOf(FocalSection.HOT) }
                var authBridgePlatform by remember { mutableStateOf<String?>(null) }
                var activeDetail by remember { mutableStateOf<ActiveArticleTarget?>(null) }

                var wallpaperKey by remember { mutableStateOf(prefStore.getWallpaperKey()) }
                var customImagePath by remember { mutableStateOf(prefStore.getCustomWallpaperPath()) }
                var opacity by remember { mutableFloatStateOf(prefStore.getWallpaperOpacity()) }
                var contrast by remember { mutableFloatStateOf(prefStore.getWallpaperContrast()) }

                BackHandler(enabled = activeDetail != null) {
                    activeDetail = null
                }

                WallpaperFilterContainer(
                    wallpaperKey = wallpaperKey,
                    customImagePath = customImagePath,
                    opacity = opacity,
                    contrast = contrast
                ) {
                    if (activeDetail != null) {
                        ProductionDetailScreen(
                            id = activeDetail!!.id,
                            platform = activeDetail!!.platform,
                            title = activeDetail!!.title,
                            originUrl = activeDetail!!.originUrl,
                            apiClient = apiClient,
                            prefStore = prefStore,
                            accountStore = accountStore,
                            roomDb = roomDb,
                            onBack = { activeDetail = null }
                        )
                    } else {
                        ModalNavigationDrawer(
                            drawerState = drawerState,
                            drawerContent = {
                                SettingsDrawerContent(
                                    prefStore = prefStore,
                                    onOpenAuthBridge = { key ->
                                        scope.launch { drawerState.close() }
                                        authBridgePlatform = key
                                    },
                                    onCloseDrawer = {
                                        scope.launch { drawerState.close() }
                                        wallpaperKey = prefStore.getWallpaperKey()
                                        customImagePath = prefStore.getCustomWallpaperPath()
                                        opacity = prefStore.getWallpaperOpacity()
                                        contrast = prefStore.getWallpaperContrast()
                                    }
                                )
                            }
                        ) {
                            Scaffold(
                                containerColor = androidx.compose.ui.graphics.Color.Transparent,
                                bottomBar = {
                                    NavigationBar(
                                        containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.92f),
                                        tonalElevation = FocalDimens.elevationCard
                                    ) {
                                        FocalSection.values().forEach { section ->
                                            val isSelected = currentSection == section
                                            NavigationBarItem(
                                                selected = isSelected,
                                                onClick = {
                                                    if (section == FocalSection.SETTINGS) {
                                                        scope.launch { drawerState.open() }
                                                    } else {
                                                        currentSection = section
                                                    }
                                                },
                                                icon = {
                                                    Icon(
                                                        imageVector = section.icon,
                                                        contentDescription = section.token,
                                                        modifier = Modifier.size(FocalDimens.iconSizeStandard)
                                                    )
                                                },
                                                colors = NavigationBarItemDefaults.colors(
                                                    selectedIconColor = MaterialTheme.colorScheme.primary,
                                                    unselectedIconColor = MaterialTheme.colorScheme.onSurfaceVariant,
                                                    indicatorColor = MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
                                                )
                                            )
                                        }
                                    }
                                }
                            ) { innerPadding ->
                                Box(
                                    modifier = Modifier
                                        .fillMaxSize()
                                        .padding(bottom = innerPadding.calculateBottomPadding())
                                ) {
                                    ReadScreen(
                                        prefStore = prefStore,
                                        onOpenDrawer = { scope.launch { drawerState.open() } },
                                        onOpenDetail = { item ->
                                            activeDetail = ActiveArticleTarget(
                                                id = item.id,
                                                platform = item.sourceTag.lowercase(),
                                                title = item.title,
                                                originUrl = item.url
                                            )
                                        }
                                    )
                                }
                            }
                        }
                    }

                    // 账号绑定隔离沙箱
                    if (authBridgePlatform != null) {
                        val loginUrl = when (authBridgePlatform) {
                            "zhihu" -> "https://www.zhihu.com/signin"
                            "bilibili" -> "https://passport.bilibili.com/login"
                            "weibo" -> "https://passport.weibo.com/visitor/visitor"
                            else -> "https://m.ithome.com"
                        }
                        AuthBridgeModal(
                            platformKey = authBridgePlatform!!,
                            loginUrl = loginUrl,
                            accountStore = accountStore,
                            onDismiss = { authBridgePlatform = null },
                            onSuccess = { authBridgePlatform = null }
                        )
                    }
                }
            }
        }
    }
}
