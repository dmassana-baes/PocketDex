package com.example.pocketdex.ui.theme

import androidx.compose.runtime.Composable

/** Application entry delegates to the shared fixed light theme. */
@Composable
fun PocketDexTheme(content: @Composable () -> Unit) {
    com.example.pocketdex.core.designsystem.theme.PocketDexTheme(content)
}