package com.example.pocketdex.feature.compare.presentation

import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.IconButton
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.clearAndSetSemantics
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.model.formatPokemonNumber
import com.example.pocketdex.core.designsystem.model.statFraction
import com.example.pocketdex.core.designsystem.theme.PocketDexColors
import com.example.pocketdex.core.designsystem.theme.PocketDexShapes
import com.example.pocketdex.core.domain.compare.StatWinner
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.feature.compare.R
import com.example.pocketdex.core.designsystem.R as DesignR
import com.example.pocketdex.core.designsystem.model.PokemonType as VisualType

data class CompareActions(val choose: (Int) -> Unit, val remove: (Int) -> Unit, val retry: (Int) -> Unit, val compare: () -> Unit)

@Composable
fun CompareScreen(state: CompareUiState, language: PokemonLanguage, actions: CompareActions,
    image: @Composable (PokemonSummary?) -> PokemonImageState, modifier: Modifier = Modifier) {
    BoxWithConstraints(modifier.fillMaxSize()) {
        val wide = maxWidth >= (600 * LocalDensity.current.fontScale).dp
        Column(Modifier.fillMaxSize().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(stringResource(R.string.compare_title), Modifier.semantics { heading() },
                    style = MaterialTheme.typography.displaySmall)
                Text(stringResource(R.string.compare_intro), color = PocketDexColors.Gray400,
                    style = MaterialTheme.typography.bodyLarge)
            }
            if (wide) Row(horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                state.slots.forEachIndexed { index, slot -> Slot(index, slot, language, actions, image, Modifier.weight(1f)) }
            } else Column(verticalArrangement = Arrangement.spacedBy(24.dp)) {
                state.slots.forEachIndexed { index, slot -> Slot(index, slot, language, actions, image, Modifier.fillMaxWidth()) }
            }
            if (state.duplicate) PocketDexInlineWarning(stringResource(R.string.compare_duplicate))
            PocketDexButton(stringResource(R.string.compare_action), actions.compare,
                Modifier.fillMaxWidth(), enabled = state.canCompare)
            if (!state.canCompare) Text(stringResource(R.string.compare_requirement),
                color = PocketDexColors.Gray400, style = MaterialTheme.typography.bodySmall)
        }
    }
}

@Composable
private fun Slot(index: Int, slot: CompareSlot, language: PokemonLanguage, actions: CompareActions,
    image: @Composable (PokemonSummary?) -> PokemonImageState, modifier: Modifier) {
    Surface(modifier, shape = PocketDexShapes.Container, color = PocketDexColors.Gray100) {
        val summary = slot.detail?.summary
        val name = summary?.identity?.visibleName(language) ?: stringResource(R.string.compare_unknown)
        Column {
            Box(Modifier.fillMaxWidth().heightIn(min = 190.dp), contentAlignment = Alignment.Center) {
                if (slot.id == null) {
                    PocketDexButton(stringResource(R.string.compare_choose, index + 1), { actions.choose(index) },
                        Modifier.padding(16.dp), style = PocketDexButtonStyle.Secondary)
                } else {
                    PokemonImage(image(summary), name, Modifier.size(128.dp))
                    Row(Modifier.align(Alignment.BottomCenter).fillMaxWidth().padding(horizontal = 8.dp),
                        horizontalArrangement = Arrangement.SpaceBetween) {
                        IconButton(onClick = { actions.choose(index) }) {
                            PocketDexIcon(PocketDexIconAsset.Refresh, stringResource(R.string.compare_replace, index + 1))
                        }
                        IconButton(onClick = { actions.remove(index) }) {
                            PocketDexIcon(PocketDexIconAsset.Close, stringResource(R.string.compare_remove, index + 1, name))
                        }
                    }
                }
            }
            if (slot.id != null) {
                if (slot.loading) Text(stringResource(R.string.compare_loading), style = MaterialTheme.typography.bodySmall)
                slot.failure?.let { PocketDexInlineWarning(stringResource(R.string.compare_data_error), onRetry = { actions.retry(index) }) }
            }
        }
    }
}

@Composable
fun CompareResultsScreen(state: CompareUiState, language: PokemonLanguage, onBack: () -> Unit,
    retry: (Int) -> Unit, modifier: Modifier = Modifier,
    image: @Composable (PokemonSummary?) -> PokemonImageState = { PokemonImageState.Error }) {
    Column(modifier.fillMaxSize()) {
        Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) { PocketDexIcon(PocketDexIconAsset.ArrowBack, stringResource(DesignR.string.pd_back)) }
            Text(stringResource(R.string.compare_title), Modifier.weight(1f).semantics { heading() },
                textAlign = TextAlign.Center, style = MaterialTheme.typography.headlineSmall)
            Spacer(Modifier.width(48.dp))
        }
        Column(Modifier.weight(1f).fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)) {
            val result = state.result
            if (result == null) {
                PocketDexStateContent(if (state.slots.any { it.loading }) PocketDexContentState.Loading else PocketDexContentState.Error,
                    stringResource(R.string.compare_results_missing))
                state.slots.forEachIndexed { index, slot -> if (slot.failure != null)
                    PocketDexButton(stringResource(R.string.compare_retry_slot, index + 1), { retry(index) }) }
            } else {
                val left = state.slots[0].detail!!.summary.identity.visibleName(language)
                val right = state.slots[1].detail!!.summary.identity.visibleName(language)
                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(24.dp)) {
                    state.slots.forEach { slot ->
                        val summary = slot.detail!!.summary
                        Column(Modifier.weight(1f), horizontalAlignment = Alignment.CenterHorizontally,
                            verticalArrangement = Arrangement.spacedBy(12.dp)) {
                            Surface(Modifier.fillMaxWidth(), shape = PocketDexShapes.Container, color = PocketDexColors.Gray100) {
                                Box(Modifier.heightIn(min = 96.dp), contentAlignment = Alignment.Center) {
                                    PokemonImage(image(summary), null, Modifier.size(80.dp))
                                }
                            }
                            Text(summary.identity.visibleName(language), textAlign = TextAlign.Center,
                                style = MaterialTheme.typography.titleMedium)
                        }
                    }
                }
                result.rows.forEach { row ->
                    CompactStatRow(statLabel(row.stat), row.left, row.right, row.winner, left, right)
                }
            }
        }
    }
}

@Composable
private fun CompactStatRow(label: String, left: Int, right: Int, winner: StatWinner, leftName: String, rightName: String) {
    Column(verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(label, style = MaterialTheme.typography.bodyMedium, color = PocketDexColors.Gray400)
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            StatValue(label, leftName, left, winner == StatWinner.Left, winner == StatWinner.Tie, Modifier.weight(1f))
            StatValue(label, rightName, right, winner == StatWinner.Right, winner == StatWinner.Tie, Modifier.weight(1f))
        }
        Text(if (winner == StatWinner.Tie) stringResource(DesignR.string.pd_tie)
            else stringResource(R.string.compare_higher, if (winner == StatWinner.Left) leftName else rightName, label),
            style = MaterialTheme.typography.bodySmall, color = PocketDexColors.Gray400)
    }
}

@Composable
private fun StatValue(label: String, name: String, value: Int, higher: Boolean, tie: Boolean, modifier: Modifier) {
    Column(modifier.clearAndSetSemantics { contentDescription = "$label, $name: $value" },
        verticalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(value.toString(), style = MaterialTheme.typography.bodyMedium)
        Box(Modifier.fillMaxWidth().height(8.dp).clip(PocketDexShapes.Pill).background(PocketDexColors.Gray200)) {
            val fraction = statFraction(value)!!
            if (fraction > 0f) Box(Modifier.fillMaxWidth(fraction).fillMaxHeight()
                .background(if (tie) PocketDexColors.Gray300 else if (higher) PocketDexColors.Yellow else PocketDexColors.Gray500))
        }
    }
}

@Composable
fun SelectorScreen(state: SelectorUiState, language: PokemonLanguage, list: LazyGridState, onBack: () -> Unit,
    onText: (String) -> Unit, search: () -> Unit, retry: () -> Unit, more: () -> Unit, previous: () -> Unit, select: (PokemonId) -> Unit,
    image: @Composable (PokemonSummary?) -> PokemonImageState, modifier: Modifier = Modifier) {
    Box(modifier.fillMaxSize().background(PocketDexColors.Gray200), contentAlignment = Alignment.BottomCenter) {
        Surface(Modifier.padding(top = 24.dp).widthIn(max = 640.dp).fillMaxWidth().fillMaxHeight(),
            shape = PocketDexShapes.Sheet, color = PocketDexColors.White) {
            Column(Modifier.imePadding()) {
                Box(Modifier.fillMaxWidth().padding(top = 14.dp, bottom = 8.dp), contentAlignment = Alignment.Center) {
                    Box(Modifier.size(32.dp, 4.dp).background(PocketDexColors.Gray300, PocketDexShapes.Pill))
                }
                Row(Modifier.fillMaxWidth().padding(horizontal = 24.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(stringResource(R.string.compare_selector), Modifier.weight(1f).semantics { heading() },
                        style = MaterialTheme.typography.headlineSmall)
                    IconButton(onClick = onBack) { PocketDexIcon(PocketDexIconAsset.Close, stringResource(DesignR.string.pd_back)) }
                }
                PokemonSearchField(state.text, onText, search, Modifier.fillMaxWidth().padding(horizontal = 24.dp))
                LazyVerticalGrid(columns = GridCells.Adaptive((144 * LocalDensity.current.fontScale).dp),
                    state = list, modifier = Modifier.weight(1f), contentPadding = PaddingValues(24.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp), verticalArrangement = Arrangement.spacedBy(12.dp)) {
                    item(key = "selector-status", span = { GridItemSpan(maxLineSpan) }) {
                        Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                            if (state.duplicate) PocketDexInlineWarning(stringResource(R.string.compare_duplicate))
                            state.failure?.let { PocketDexInlineWarning(stringResource(R.string.compare_catalog_error), onRetry = retry) }
                            if (state.coverage == CatalogCoverage.Partial && state.items.isNotEmpty()) Text(stringResource(R.string.compare_partial_catalog))
                            if (state.loading) PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.compare_loading))
                            if (!state.loading && state.failure == null && state.items.isEmpty())
                                PocketDexStateContent(PocketDexContentState.Empty, stringResource(R.string.compare_no_matches))
                        }
                    }
                    if (state.previousCursor != null) item(key = "selector-previous", span = { GridItemSpan(maxLineSpan) }) {
                        PocketDexButton(stringResource(R.string.compare_previous), previous, enabled = !state.loading)
                    }
                    items(state.items, key = { it.identity.id.value }) { summary ->
                        CandidateCard(summary, language, image(summary)) { select(summary.identity.id) }
                    }
                    if (state.nextCursor != null) item(key = "selector-more", span = { GridItemSpan(maxLineSpan) }) {
                        PocketDexButton(stringResource(R.string.compare_more), more, enabled = !state.loading)
                    }
                }
            }
        }
    }
}

@Composable
private fun CandidateCard(summary: PokemonSummary, language: PokemonLanguage, image: PokemonImageState, onClick: () -> Unit) {
    val primary = summary.types.firstOrNull()?.let { VisualType.valueOf(it.name) }
    Surface(Modifier.fillMaxWidth(), shape = PocketDexShapes.Container,
        color = primary?.accent ?: PocketDexColors.Gray100, contentColor = primary?.onAccent ?: PocketDexColors.Gray500) {
        Box(Modifier.clickable(role = Role.Button, onClick = onClick).heightIn(min = 100.dp)) {
            Image(painterResource(DesignR.drawable.pd_pokeball_decoration), null,
                Modifier.align(Alignment.BottomEnd).offset(x = 11.dp, y = 20.dp).size(88.dp).alpha(.12f))
            Column(Modifier.padding(12.dp), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                Row(horizontalArrangement = Arrangement.spacedBy(4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Text(summary.identity.visibleName(language), Modifier.weight(1f), style = MaterialTheme.typography.titleMedium)
                    Text(formatPokemonNumber(summary.identity.nationalNumber), style = MaterialTheme.typography.labelSmall)
                }
                Row(verticalAlignment = Alignment.Bottom, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                    Column(Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(4.dp)) {
                        summary.types.forEach { type -> val visual = VisualType.valueOf(type.name)
                            PokemonTypeBadge(stringResource(visual.labelRes), visual) }
                    }
                    PokemonImage(image, null, Modifier.size(56.dp))
                }
            }
        }
    }
}

@Composable
private fun statLabel(stat: PokemonStat): String = stringResource(when (stat) {
    PokemonStat.Hp -> R.string.compare_hp
    PokemonStat.Attack -> R.string.compare_attack
    PokemonStat.Defense -> R.string.compare_defense
    PokemonStat.SpecialAttack -> R.string.compare_special_attack
    PokemonStat.SpecialDefense -> R.string.compare_special_defense
    PokemonStat.Speed -> R.string.compare_speed
})
