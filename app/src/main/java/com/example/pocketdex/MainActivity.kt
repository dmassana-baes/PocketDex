package com.example.pocketdex

import android.annotation.SuppressLint
import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.window.layout.FoldingFeature
import androidx.window.layout.WindowInfoTracker
import androidx.window.layout.WindowLayoutInfo
import com.example.pocketdex.ui.PocketDexApp
import com.example.pocketdex.ui.theme.PocketDexTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    @SuppressLint("RestrictedApi")
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // The shared theme is always light, including when the system uses night mode.
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.light(Color.TRANSPARENT, Color.TRANSPARENT),
            navigationBarStyle = SystemBarStyle.light(Color.WHITE, 0xFF283141.toInt()),
        )
        setContent {
            PocketDexTheme {
                val layoutFlow = remember(this) { WindowInfoTracker.getOrCreate(this).windowLayoutInfo(this) }
                val layoutInfo by layoutFlow.collectAsStateWithLifecycle(initialValue = WindowLayoutInfo(emptyList()))
                PocketDexApp(layoutInfo.displayFeatures.filterIsInstance<FoldingFeature>(), taskId = taskId)
            }
        }
    }
}
