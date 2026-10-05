package com.example.pocketdex.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.NavigationRail
import androidx.compose.material3.NavigationRailItem
import androidx.compose.material3.NavigationRailItemDefaults
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.error
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.model.NavigationPresentation
import com.example.pocketdex.core.designsystem.theme.PocketDexColors

@Composable
fun PocketDexAppBar(
    title: String,
    modifier: Modifier = Modifier,
    onBack: (() -> Unit)? = null,
    actions: @Composable () -> Unit = {},
) {
    Row(modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(8.dp)) {
        if (onBack != null) IconButton(onClick = onBack) {
            PocketDexIcon(PocketDexIconAsset.ArrowBack, stringResource(R.string.pd_back))
        }
        Text(title, Modifier.weight(1f).semantics { heading() }, style = MaterialTheme.typography.titleLarge)
        actions()
    }
}

/** Stable IDs and visual slots, no routes or feature dependencies. */
data class PocketDexNavigationItem(val id: String, val label: String, val icon: @Composable () -> Unit)

/** Insets are deliberately zero: the host consumes them once and supplies a hinge-free region. */
@Composable
fun PocketDexNavigation(
    items: List<PocketDexNavigationItem>,
    selectedId: String,
    presentation: NavigationPresentation,
    onSelect: (String) -> Unit,
    modifier: Modifier = Modifier,
) {
    require(items.map { it.id }.distinct().size == items.size) { "Navigation IDs must be unique" }
    when (presentation) {
        NavigationPresentation.BottomBar -> NavigationBar(
            modifier, containerColor = PocketDexColors.White, tonalElevation = 0.dp,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            items.forEach { item -> NavigationBarItem(
                selected = item.id == selectedId, onClick = { onSelect(item.id) }, icon = item.icon,
                label = { Text(item.label) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = PocketDexColors.Gray500, selectedTextColor = PocketDexColors.Gray500,
                    unselectedIconColor = PocketDexColors.Gray400, unselectedTextColor = PocketDexColors.Gray400,
                    indicatorColor = PocketDexColors.Gray100,
                ),
            ) }
        }
        NavigationPresentation.SideRail -> NavigationRail(
            modifier.verticalScroll(rememberScrollState()), containerColor = PocketDexColors.White,
            windowInsets = WindowInsets(0, 0, 0, 0),
        ) {
            items.forEach { item -> NavigationRailItem(
                selected = item.id == selectedId, onClick = { onSelect(item.id) }, icon = item.icon,
                label = { Text(item.label) },
                colors = NavigationRailItemDefaults.colors(
                    selectedIconColor = PocketDexColors.Gray500, selectedTextColor = PocketDexColors.Gray500,
                    unselectedIconColor = PocketDexColors.Gray400, unselectedTextColor = PocketDexColors.Gray400,
                    indicatorColor = PocketDexColors.Gray100,
                ),
            ) }
        }
    }
}

enum class PocketDexContentState { Loading, Empty, Error }

@Composable
fun PocketDexStateContent(
    state: PocketDexContentState,
    message: String,
    modifier: Modifier = Modifier,
    actionLabel: String? = null,
    onAction: (() -> Unit)? = null,
) {
    Column(modifier.padding(24.dp).semantics {
        liveRegion = LiveRegionMode.Polite
        if (state == PocketDexContentState.Error) error(message)
    }, horizontalAlignment = Alignment.CenterHorizontally, verticalArrangement = Arrangement.spacedBy(16.dp)) {
        if (state == PocketDexContentState.Loading) CircularProgressIndicator(color = PocketDexColors.Gray500)
        Text(message, style = MaterialTheme.typography.bodyLarge)
        if (onAction != null && actionLabel != null) PocketDexButton(actionLabel, onAction)
    }
}

/** An inline warning does not replace valid screen content. */
@Composable
fun PocketDexInlineWarning(message: String, modifier: Modifier = Modifier, onRetry: (() -> Unit)? = null) {
    Surface(modifier, color = PocketDexColors.Gray100, contentColor = PocketDexColors.Gray500) {
        Column(Modifier.padding(16.dp).semantics { liveRegion = LiveRegionMode.Polite; error(message) }) {
            Text(message, style = MaterialTheme.typography.bodyMedium)
            if (onRetry != null) TextButton(onClick = onRetry,
                colors = ButtonDefaults.textButtonColors(contentColor = PocketDexColors.Gray500)) {
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
                    PocketDexIcon(PocketDexIconAsset.Refresh, null)
                    Text(stringResource(R.string.pd_retry))
                }
            }
        }
    }
}
