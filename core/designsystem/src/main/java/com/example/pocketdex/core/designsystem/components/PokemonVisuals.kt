package com.example.pocketdex.core.designsystem.components

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.offset
import androidx.compose.ui.draw.alpha
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.ColorFilter
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.model.PokemonType
import com.example.pocketdex.core.designsystem.theme.PocketDexColors
import com.example.pocketdex.core.designsystem.theme.PocketDexShapes

/** Source resolution and cache policy belong to the host; no network or IO occurs here. */
sealed interface PokemonImageState {
    data object Loading : PokemonImageState
    data object Error : PokemonImageState
    data class Ready(val painter: Painter) : PokemonImageState
}

@Composable
fun PokemonImage(
    state: PokemonImageState,
    description: String?,
    modifier: Modifier = Modifier,
    silhouette: Boolean = false,
) {
    val status = stringResource(when {
        state == PokemonImageState.Loading -> R.string.pd_image_loading
        state == PokemonImageState.Error -> R.string.pd_image_error
        else -> R.string.pd_silhouette
    })
    Box(modifier, contentAlignment = Alignment.Center) {
        when (state) {
            is PokemonImageState.Ready -> Image(
                painter = state.painter, contentDescription = if (silhouette) status else description,
                modifier = Modifier.fillMaxSize(), contentScale = ContentScale.Fit,
                colorFilter = if (silhouette) ColorFilter.tint(PocketDexColors.Gray500) else null,
            )
            PokemonImageState.Loading -> Box(
                Modifier.fillMaxSize().background(PocketDexColors.Gray100, PocketDexShapes.Container)
                    .clearAndSetSemantics { contentDescription = status },
                contentAlignment = Alignment.Center,
            ) { CircularProgressIndicator(Modifier.size(24.dp), color = PocketDexColors.Gray500) }
            PokemonImageState.Error -> Box(
                Modifier.fillMaxSize().background(PocketDexColors.Gray100, PocketDexShapes.Container)
                    .clearAndSetSemantics { contentDescription = status },
                contentAlignment = Alignment.Center,
            ) { Text(stringResource(R.string.pd_image_error_marker), style = MaterialTheme.typography.titleMedium) }
        }
    }
}

/** Uses the supplied Figma vector locally; an explicit painter can override the known style. */
@Composable
fun PokemonTypeBadge(
    label: String,
    type: PokemonType?,
    modifier: Modifier = Modifier,
    icon: Painter? = null,
) {
    val resolvedIcon = icon ?: type?.let { painterResource(it.iconRes) }
    Surface(
        modifier = modifier, shape = PocketDexShapes.Pill,
        color = type?.accent ?: PocketDexColors.Gray100,
        contentColor = type?.onAccent ?: PocketDexColors.Gray500,
    ) {
        Row(Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
            if (resolvedIcon != null) Image(resolvedIcon, contentDescription = null, modifier = Modifier.size(16.dp))
            Text(label, style = MaterialTheme.typography.labelSmall)
        }
    }
}

@Composable
fun PokemonTypeChip(
    label: String,
    type: PokemonType?,
    selected: Boolean,
    onSelectedChange: (Boolean) -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    icon: Painter? = null,
) {
    val resolvedIcon = icon ?: type?.let { painterResource(it.iconRes) }
    val background = if (selected) type?.accent ?: PocketDexColors.Gray200 else PocketDexColors.Gray100
    val foreground = if (selected) type?.onAccent ?: PocketDexColors.Gray500 else PocketDexColors.Gray500
    Row(
        modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .background(background, PocketDexShapes.Chip)
            .toggleable(value = selected, enabled = enabled, role = Role.Checkbox, onValueChange = onSelectedChange)
            .padding(12.dp),
        horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically,
    ) {
        if (resolvedIcon != null) Image(resolvedIcon, null, Modifier.size(24.dp))
        Text(label, color = foreground, style = MaterialTheme.typography.labelLarge)
    }
}

/** Ordered visual types. Unknown style retains the caller's label without masquerading as another type. */
data class PokemonTypeVisual(val label: String, val type: PokemonType?, val icon: Painter? = null)

/** The host orders types by remote slot. Opening and favorite are separate sibling click targets. */
@Composable
fun PokemonCard(
    name: String,
    number: String,
    types: List<PokemonTypeVisual>,
    image: PokemonImageState,
    favorite: Boolean,
    onOpen: () -> Unit,
    onFavoriteClick: () -> Unit,
    modifier: Modifier = Modifier,
    favoriteEnabled: Boolean = true,
    favoriteBusy: Boolean = false,
) {
    Surface(modifier, shape = PocketDexShapes.Container,
        color = types.firstOrNull()?.type?.accent ?: PocketDexColors.Gray100,
        contentColor = types.firstOrNull()?.type?.onAccent ?: PocketDexColors.Gray500) {
        Column {
            Box(Modifier.fillMaxWidth()) {
                // Figma 18:2069: 88 dp, right -11/bottom -20; vector fills are white at 12%.
                Image(painterResource(R.drawable.pd_pokeball_decoration), contentDescription = null,
                    modifier = Modifier.align(Alignment.BottomEnd).offset(x = 11.dp, y = 20.dp)
                        .size(88.dp).alpha(.12f))
                Column(
                    Modifier.fillMaxWidth().clickable(role = Role.Button, onClick = onOpen).padding(12.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp),
                ) {
                    Text(name, style = MaterialTheme.typography.titleMedium)
                    Text(number, style = MaterialTheme.typography.labelSmall)
                    // No universal fixed height; labels can wrap at high font scales.
                    types.forEach { PokemonTypeBadge(it.label, it.type, icon = it.icon) }
                    PokemonImage(image, description = null, modifier = Modifier.size(56.dp).align(Alignment.End))
                }
            }
            // Neutral surface keeps the favorite action legible for every type color.
            Box(Modifier.fillMaxWidth().background(PocketDexColors.White)) {
                FavoriteButton(favorite, onFavoriteClick, enabled = favoriteEnabled, busy = favoriteBusy)
            }
        }
    }
}

/** Figma stops at 0, .5 and 1; labels should use an opaque neutral region when necessary. */
fun pokemonHeaderGradient(type: PokemonType?): Brush = Brush.verticalGradient(
    0f to (type?.accent ?: PocketDexColors.Gray100),
    .5f to (type?.accent ?: PocketDexColors.Gray100),
    1f to Color.White.copy(alpha = 0f),
)
