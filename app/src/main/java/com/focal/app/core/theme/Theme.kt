package com.focal.app.core.theme

import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.Composable

@Composable
fun FocalTheme(content: @Composable () -> Unit) {
    MaterialTheme(
        colorScheme = FocalColorScheme,
        content = content
    )
}
