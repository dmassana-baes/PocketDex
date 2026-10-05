package com.example.pocketdex.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.IconButtonDefaults
import androidx.compose.material3.IconToggleButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.theme.PocketDexColors
import com.example.pocketdex.core.designsystem.theme.PocketDexShapes

enum class PocketDexButtonStyle { Primary, Secondary, Ghost }

@Composable
fun PocketDexButton(
    label: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    style: PocketDexButtonStyle = PocketDexButtonStyle.Primary,
    enabled: Boolean = true,
    busy: Boolean = false,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    val busyLabel = stringResource(R.string.pd_busy)
    val background = when (style) {
        PocketDexButtonStyle.Primary -> PocketDexColors.Yellow
        PocketDexButtonStyle.Secondary -> PocketDexColors.White
        PocketDexButtonStyle.Ghost -> androidx.compose.ui.graphics.Color.Transparent
    }
    Button(
        onClick = { if (enabled && !busy) onClick() },
        enabled = enabled && !busy,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp)
            .semantics { if (busy) stateDescription = busyLabel },
        shape = PocketDexShapes.Container,
        border = if (style == PocketDexButtonStyle.Secondary) BorderStroke(1.dp, PocketDexColors.Gray400) else null,
        colors = ButtonDefaults.buttonColors(
            containerColor = background, contentColor = PocketDexColors.Gray500,
            disabledContainerColor = PocketDexColors.Gray100,
            disabledContentColor = PocketDexColors.Gray400,
        ),
    ) {
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp), verticalAlignment = Alignment.CenterVertically) {
            // Reserve the same space in every state; a busy action keeps its label and width.
            Box(Modifier.size(if (leadingIcon != null) 24.dp else 16.dp), contentAlignment = Alignment.Center) {
                if (busy) CircularProgressIndicator(Modifier.size(16.dp), color = PocketDexColors.Gray500, strokeWidth = 2.dp)
                else leadingIcon?.invoke()
            }
            Text(label, style = MaterialTheme.typography.labelLarge)
        }
    }
}

/** Controlled state; filled/outlined heart and accessible action reflect the supplied favorite. */
@Composable
fun FavoriteButton(
    favorite: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    busy: Boolean = false,
    icon: (@Composable () -> Unit)? = null,
) {
    val label = stringResource(if (favorite) R.string.pd_remove_favorite else R.string.pd_add_favorite)
    val description = stringResource(if (favorite) R.string.pd_favorite else R.string.pd_not_favorite)
    val busyLabel = stringResource(R.string.pd_busy)
    IconToggleButton(
        checked = favorite,
        onCheckedChange = { if (enabled && !busy) onClick() }, enabled = enabled && !busy,
        modifier = modifier.defaultMinSize(minWidth = 48.dp, minHeight = 48.dp).semantics {
            selected = favorite
            role = Role.Button
            contentDescription = label
            stateDescription = if (busy) "$description, $busyLabel" else description
        },
        colors = IconButtonDefaults.iconToggleButtonColors(
            contentColor = PocketDexColors.Gray500, disabledContentColor = PocketDexColors.Gray400,
            checkedContentColor = PocketDexColors.Gray500,
        ),
    ) {
        Box(Modifier.size(24.dp), contentAlignment = Alignment.Center) {
            if (busy) CircularProgressIndicator(Modifier.size(16.dp), color = PocketDexColors.Gray500, strokeWidth = 2.dp)
            else if (icon != null) icon()
            else PocketDexIcon(if (favorite) PocketDexIconAsset.HeartFilled else PocketDexIconAsset.Heart, null)
        }
    }
}

/** No debounce, search rules or repositories; the host owns the text and keyboard action. */
@Composable
fun PokemonSearchField(
    value: String,
    onValueChange: (String) -> Unit,
    onSearch: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    leadingIcon: (@Composable () -> Unit)? = null,
) {
    OutlinedTextField(
        value = value, onValueChange = onValueChange, modifier = modifier,
        enabled = enabled, singleLine = true, shape = PocketDexShapes.Container,
        textStyle = MaterialTheme.typography.bodyLarge,
        label = { Text(stringResource(R.string.pd_search)) },
        leadingIcon = leadingIcon ?: { PocketDexIcon(PocketDexIconAsset.Search, null) },
        trailingIcon = if (value.isNotEmpty()) {
            { IconButton(onClick = { onValueChange("") }, enabled = enabled) {
                PocketDexIcon(PocketDexIconAsset.Close, stringResource(R.string.pd_clear_search))
            } }
        } else null,
        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
        keyboardActions = KeyboardActions(onSearch = { if (enabled) onSearch() }),
        colors = OutlinedTextFieldDefaults.colors(
            focusedBorderColor = PocketDexColors.Gray400, unfocusedBorderColor = PocketDexColors.Gray400,
            focusedLabelColor = PocketDexColors.Gray400, unfocusedLabelColor = PocketDexColors.Gray400,
            disabledTextColor = PocketDexColors.Gray400, disabledLabelColor = PocketDexColors.Gray400,
        ),
    )
}
