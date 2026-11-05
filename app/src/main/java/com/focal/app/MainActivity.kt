package com.focal.app

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.focal.app.core.navigation.FocalSection
import com.focal.app.core.storage.FocalPreferenceStore
import com.focal.app.core.theme.BambooGreen
import com.focal.app.core.theme.FocalTheme
import com.focal.app.core.theme.InkBlack
import com.focal.app.core.theme.InkBorder
import com.focal.app.core.theme.InkSurface
import com.focal.app.core.theme.MistGrey
import com.focal.app.feature_ask.AskScreen
import com.focal.app.feature_listen.ListenScreen
import com.focal.app.feature_read.ReadScreen
import com.focal.app.feature_set.AuthBridgeDialog
import com.focal.app.feature_set.SettingsDrawerContent
import com.focal.app.feature_watch.WatchScreen
import kotlinx.coroutines.launch

/**
 * 主界面架构：
 * 1. 底部纯图标导航（阅、视、听、问）
 * 2. 侧边独立展开/隐藏抽屉设置（不在主界面占用多余空间）
 * 3. 身份对接 WebView 弹窗沙箱
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val prefStore = FocalPreferenceStore(this)

        setContent {
            FocalTheme {
                val drawerState = rememberDrawerState(initialValue = DrawerValue.Closed)
                val scope = rememberCoroutineScope()

                var currentSection by remember { mutableStateOf(FocalSection.READ) }
                var authBridgePlatform by remember { mutableStateOf<String?>(null) }

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
                            }
                        )
                    }
                ) {
                    Scaffold(
                        containerColor = InkBlack,
                        bottomBar = {
                            NavigationBar(
                                containerColor = InkSurface,
                                tonalElevation = 8.dp
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
                                                modifier = Modifier.size(24.dp)
                                            )
                                        },
                                        colors = NavigationBarItemDefaults.colors(
                                            selectedIconColor = BambooGreen,
                                            unselectedIconColor = MistGrey,
                                            indicatorColor = InkSurface
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
                            when (currentSection) {
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
