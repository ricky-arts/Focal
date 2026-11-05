package com.focal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.*
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focal.app.core.navigation.FocalSection
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.*
import com.focal.app.feature_ask.AskScreen
import com.focal.app.feature_listen.ListenScreen
import com.focal.app.feature_read.ReadScreen
import com.focal.app.feature_set.AuthBridgeDialog
import com.focal.app.feature_set.SettingsDrawerContent
import com.focal.app.feature_watch.WatchScreen
import kotlinx.coroutines.launch

/**
 * Material 3 架构主界面：
 * 1. 动态挂载主题配色系统与动态背景壁纸/透明度/对比度滤镜容器
 * 2. 侧边独立展开的设置抽屉 (ModalNavigationDrawer)
 * 3. 底部符合 M3 标准的纯矢量图标导航栏，跟随主题自适应色彩与水波纹按压反馈
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefStore = FocalPreferenceStore(this)

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

                var currentSection by remember { mutableStateOf(FocalSection.READ) }
                var authBridgePlatform by remember { mutableStateOf<String?>(null) }

                // 响应式壁纸状态
                var wallpaperKey by remember { mutableStateOf(prefStore.getWallpaperKey()) }
                var opacity by remember { mutableFloatStateOf(prefStore.getWallpaperOpacity()) }
                var contrast by remember { mutableFloatStateOf(prefStore.getWallpaperContrast()) }

                // 外层：壁纸与对比度滤镜容器
                WallpaperFilterContainer(
                    wallpaperKey = wallpaperKey,
                    opacity = opacity,
                    contrast = contrast
                ) {
                    ModalNavigationDrawer(
                        drawerState = drawerState,
                        drawerContent = {
                            SettingsDrawerContent(
                                prefStore = prefStore,
                                onOpenAuthBridge = { platformKey ->
                                    scope.launch { drawerState.close() }
                                    authBridgePlatform = platformKey
                                },
                                onCloseDrawer = {
                                    scope.launch { drawerState.close() }
                                    // 抽屉关闭时刷新壁纸配置
                                    wallpaperKey = prefStore.getWallpaperKey()
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
                                    containerColor = MaterialTheme.colorScheme.surface.copy(alpha = 0.90f),
                                    tonalElevation = FocalDimens.elevationCard
                                ) {
                                    FocalSection.values().forEach { section ->
                                        val isSelected = currentSection == section
                                        NavigationBarItem(
                                            selected = isSelected,
                                            onClick = { currentSection = section },
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
                                AnimatedContent(
                                    targetState = currentSection,
                                    transitionSpec = { fadeIn() togetherWith fadeOut() },
                                    label = "sectionTransition"
                                ) { section ->
                                    when (section) {
                                        FocalSection.READ -> ReadScreen(
                                            prefStore = prefStore,
                                            onOpenDrawer = { scope.launch { drawerState.open() } }
                                        )
                                        FocalSection.WATCH -> WatchScreen(
                                            prefStore = prefStore,
                                            onOpenDrawer = { scope.launch { drawerState.open() } }
                                        )
                                        FocalSection.LISTEN -> ListenScreen(
                                            prefStore = prefStore,
                                            onOpenDrawer = { scope.launch { drawerState.open() } }
                                        )
                                        FocalSection.ASK -> AskScreen(
                                            prefStore = prefStore,
                                            onOpenDrawer = { scope.launch { drawerState.open() } }
                                        )
                                    }
                                }
                            }
                        }
                    }

                    // AI 身份对接沙箱窗口
                    if (authBridgePlatform != null) {
                        AuthBridgeDialog(
                            platformKey = authBridgePlatform!!,
                            prefStore = prefStore,
                            onDismiss = { authBridgePlatform = null },
                            onSuccess = { authBridgePlatform = null }
                        )
                    }
                }
            }
        }
    }
}
