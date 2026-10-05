package com.example.pocketdex.ui.adaptive

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.ime
import androidx.compose.foundation.layout.safeDrawing
import androidx.compose.foundation.layout.union
import androidx.compose.foundation.layout.windowInsetsPadding
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.layout.Layout
import androidx.compose.ui.layout.onGloballyPositioned
import androidx.compose.ui.layout.positionInWindow
import androidx.compose.ui.unit.Constraints
import androidx.window.layout.FoldingFeature
import com.example.pocketdex.core.designsystem.model.NavigationPresentation
import kotlin.math.roundToInt

@Composable
fun AdaptiveHost(
    presentation: NavigationPresentation,
    foldingFeatures: List<FoldingFeature>,
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit,
    modifier: Modifier = Modifier,
) {
    // Consume insets here only. Hinge bounds are window coordinates; the inner origin includes insets.
    Box(modifier.fillMaxSize().windowInsetsPadding(WindowInsets.safeDrawing.union(WindowInsets.ime))) {
        var origin by remember { mutableStateOf(Offset.Zero) }
        val obstructions = foldingFeatures.filter {
            it.isSeparating || it.occlusionType == FoldingFeature.OcclusionType.FULL
        }.map {
            WindowObstruction(
                PixelRect(
                    it.bounds.left - origin.x.roundToInt(), it.bounds.top - origin.y.roundToInt(),
                    it.bounds.right - origin.x.roundToInt(), it.bounds.bottom - origin.y.roundToInt(),
                ),
                it.orientation == FoldingFeature.Orientation.VERTICAL,
            )
        }
        Layout(
            modifier = Modifier.fillMaxSize().onGloballyPositioned { origin = it.positionInWindow() },
            content = {
                AdaptiveChrome(presentation, navigation, content)
            },
        ) { measurables, constraints ->
            val region = usableRegion(constraints.maxWidth, constraints.maxHeight, obstructions)
            val child = measurables.single().measure(Constraints.fixed(region.width, region.height))
            layout(constraints.maxWidth, constraints.maxHeight) { child.place(region.left, region.top) }
        }
    }
}

/** One composition location for content in both patterns: resizing must not discard destination state. */
@Composable
private fun AdaptiveChrome(
    presentation: NavigationPresentation,
    navigation: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Layout(content = {
        Box { navigation() }
        Box(Modifier.fillMaxSize()) { content() }
    }) { measurables, constraints ->
        val width = constraints.maxWidth
        val height = constraints.maxHeight
        val nav = measurables[0].measure(Constraints(maxWidth = width, maxHeight = height))
        val rail = presentation == NavigationPresentation.SideRail
        val body = measurables[1].measure(Constraints.fixed(
            if (rail) (width - nav.width).coerceAtLeast(0) else width,
            if (rail) height else (height - nav.height).coerceAtLeast(0),
        ))
        layout(width, height) {
            if (rail) {
                nav.placeRelative(0, 0)
                body.placeRelative(nav.width, 0)
            } else {
                body.placeRelative(0, 0)
                nav.placeRelative(0, height - nav.height)
            }
        }
    }
}
