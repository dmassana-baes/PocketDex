package com.example.pocketdex.core.designsystem.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.R
import com.example.pocketdex.core.designsystem.model.ComparisonResult
import com.example.pocketdex.core.designsystem.model.statFraction
import com.example.pocketdex.core.designsystem.theme.PocketDexColors

@Composable
fun PokemonStatRow(label: String, value: Int?, modifier: Modifier = Modifier, accessibleLabel: String? = null) {
    val displayedValue = value?.toString() ?: stringResource(R.string.pd_missing)
    val semanticsModifier = if (accessibleLabel == null) Modifier.semantics(mergeDescendants = true) {}
    else Modifier.clearAndSetSemantics { contentDescription = "$accessibleLabel: $displayedValue" }
    Column(modifier.then(semanticsModifier), verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Text(label, Modifier.weight(1f), style = MaterialTheme.typography.bodyMedium)
            Text(displayedValue, style = MaterialTheme.typography.bodyMedium)
        }
        statFraction(value)?.let { fraction ->
            // The real number is announced above; do not announce 100% as a catalog maximum.
            LinearProgressIndicator(
                progress = { fraction }, modifier = Modifier.fillMaxWidth().clearAndSetSemantics {},
                color = PocketDexColors.Yellow, trackColor = PocketDexColors.Gray200,
            )
        }
    }
}

/** Result is calculated outside this module; missing values never become zero. */
@Composable
fun PokemonComparisonRow(
    label: String,
    left: Int?,
    right: Int?,
    result: ComparisonResult,
    modifier: Modifier = Modifier,
    leftLabel: String? = null,
    rightLabel: String? = null,
) {
    val leftDescription = "$label, ${leftLabel ?: stringResource(R.string.pd_left)}"
    val rightDescription = "$label, ${rightLabel ?: stringResource(R.string.pd_right)}"
    Column(modifier, verticalArrangement = Arrangement.spacedBy(8.dp)) {
        Text(label, style = MaterialTheme.typography.titleMedium)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(16.dp)) {
            PokemonStatRow(label = "", value = left, modifier = Modifier.weight(1f), accessibleLabel = leftDescription)
            PokemonStatRow(label = "", value = right, modifier = Modifier.weight(1f), accessibleLabel = rightDescription)
        }
        val resultLabel = if (left == null || right == null) R.string.pd_missing else when (result) {
            ComparisonResult.LeftHigher -> R.string.pd_left_higher
            ComparisonResult.RightHigher -> R.string.pd_right_higher
            ComparisonResult.Tie -> R.string.pd_tie
            ComparisonResult.Unavailable -> R.string.pd_missing
        }
        Text(stringResource(resultLabel), style = MaterialTheme.typography.bodyMedium)
    }
}
