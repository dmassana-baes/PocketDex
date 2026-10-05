package com.example.pocketdex.feature.quiz.presentation

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.Image
import androidx.compose.foundation.clickable
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Surface
import androidx.compose.material3.IconButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.LiveRegionMode
import androidx.compose.ui.semantics.liveRegion
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.selected
import androidx.compose.ui.semantics.stateDescription
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.example.pocketdex.core.designsystem.components.*
import com.example.pocketdex.core.designsystem.theme.PocketDexColors
import com.example.pocketdex.core.designsystem.theme.PocketDexShapes
import com.example.pocketdex.core.domain.model.*
import com.example.pocketdex.core.domain.quiz.QuizSession
import com.example.pocketdex.feature.quiz.R
import com.example.pocketdex.core.designsystem.R as DesignR
import com.example.pocketdex.core.designsystem.model.PokemonType as VisualType

data class QuizActions(
    val start: () -> Unit, val resume: () -> Unit, val restart: () -> Unit,
    val confirmRestart: () -> Unit, val cancelRestart: () -> Unit,
    val retry: () -> Unit, val back: () -> Unit,
    val answer: (String, Int, PokemonId) -> Unit, val next: (String, Int) -> Unit,
    val hints: (String, Int) -> Unit,
)

@Composable
fun QuizScreen(state: QuizUiState, language: PokemonLanguage, actions: QuizActions,
    image: @Composable (QuizSession) -> PokemonImageState, modifier: Modifier = Modifier) {
    Column(modifier.fillMaxSize()) {
        if (state.phase != QuizPhase.Idle) QuizHeader(
            stringResource(if (state.phase == QuizPhase.Playing) R.string.quiz_question else R.string.quiz_title), actions.back)
        if (state.confirmRestart) AlertDialog(onDismissRequest = actions.cancelRestart,
            title = { Text(stringResource(R.string.quiz_restart_title)) },
            text = { Text(stringResource(R.string.quiz_restart_message)) },
            confirmButton = { PocketDexButton(stringResource(R.string.quiz_restart), actions.confirmRestart) },
            dismissButton = { PocketDexButton(stringResource(R.string.quiz_cancel), actions.cancelRestart, style = PocketDexButtonStyle.Ghost) })
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            val wide = maxWidth >= (600 * LocalDensity.current.fontScale).dp
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(24.dp),
                verticalArrangement = Arrangement.spacedBy(24.dp,
                    if (state.phase == QuizPhase.Idle) Alignment.CenterVertically else Alignment.Top)) {
                if (state.recoveryNotice) PocketDexInlineWarning(stringResource(R.string.quiz_recovery_notice))
                when (state.phase) {
                    QuizPhase.Idle -> {
                        Image(painterResource(R.drawable.quiz_intro_illustration), stringResource(R.string.quiz_intro),
                            Modifier.align(Alignment.CenterHorizontally).widthIn(max = 312.dp).fillMaxWidth()
                                .aspectRatio(624f / 458f), contentScale = ContentScale.Fit)
                        PocketDexButton(stringResource(R.string.quiz_start), actions.start, Modifier.fillMaxWidth(), enabled = state.attached)
                        Text(stringResource(R.string.quiz_rules), color = PocketDexColors.Gray400,
                            style = MaterialTheme.typography.bodySmall, textAlign = TextAlign.Center)
                    }
                    QuizPhase.Preparing -> PocketDexStateContent(PocketDexContentState.Loading, stringResource(R.string.quiz_preparing))
                    QuizPhase.Error -> {
                        PocketDexStateContent(PocketDexContentState.Error,
                            stringResource(if (state.failure == DataFailure.InsufficientLocalData) R.string.quiz_insufficient else R.string.quiz_error),
                            actionLabel = stringResource(R.string.quiz_retry), onAction = actions.retry)
                        if (state.session != null) PocketDexButton(stringResource(R.string.quiz_restart), actions.restart,
                            style = PocketDexButtonStyle.Secondary)
                    }
                    QuizPhase.Resume, QuizPhase.Results -> {
                        Text(stringResource(R.string.quiz_resume_message), style = MaterialTheme.typography.headlineSmall)
                        PocketDexButton(stringResource(R.string.quiz_continue), actions.resume, Modifier.fillMaxWidth())
                        PocketDexButton(stringResource(R.string.quiz_restart), actions.restart, Modifier.fillMaxWidth(), style = PocketDexButtonStyle.Secondary)
                    }
                    QuizPhase.Playing -> state.session?.let { session ->
                        val questionImage = image(session)
                        Text(stringResource(R.string.quiz_progress, session.index + 1), color = PocketDexColors.Gray400)
                        if (wide) Row(horizontalArrangement = Arrangement.spacedBy(24.dp), verticalAlignment = Alignment.CenterVertically) {
                            QuestionImage(session, language, questionImage, Modifier.weight(1f))
                            AnswerOptions(session, language, actions, questionImage is PokemonImageState.Ready, Modifier.weight(1f))
                        } else {
                            QuestionImage(session, language, questionImage, Modifier.fillMaxWidth())
                            AnswerOptions(session, language, actions, questionImage is PokemonImageState.Ready, Modifier.fillMaxWidth())
                        }
                        if (session.answered) {
                            val correct = session.current.correct.summary.identity.visibleName(language)
                            val chosen = session.current.options.single { it.summary.identity.id == session.answers[session.index] }
                                .summary.identity.visibleName(language)
                            Column(Modifier.semantics { liveRegion = LiveRegionMode.Polite }, verticalArrangement = Arrangement.spacedBy(8.dp)) {
                                Text(stringResource(if (session.answers[session.index] == session.current.correct.summary.identity.id)
                                    R.string.quiz_correct else R.string.quiz_incorrect), style = MaterialTheme.typography.headlineSmall)
                                Text(stringResource(R.string.quiz_answer_feedback, chosen, correct))
                            }
                        }
                        PocketDexButton(stringResource(R.string.quiz_hints), { actions.hints(session.id, session.index) },
                            Modifier.align(Alignment.CenterHorizontally), style = PocketDexButtonStyle.Ghost, enabled = !session.hintsVisible)
                        if (session.hintsVisible) Hints(session)
                        PocketDexButton(stringResource(if (session.index == 4) R.string.quiz_view_result else R.string.quiz_next),
                            { actions.next(session.id, session.index) }, Modifier.align(Alignment.CenterHorizontally),
                            style = PocketDexButtonStyle.Ghost, enabled = session.answered,
                            leadingIcon = { PocketDexIcon(PocketDexIconAsset.Refresh, null) })
                    }
                }
            }
        }
    }
}

@Composable
private fun QuestionImage(session: QuizSession, language: PokemonLanguage, image: PokemonImageState, modifier: Modifier) {
    Box(modifier.heightIn(min = 284.dp), contentAlignment = Alignment.Center) {
        if (session.answered) Image(painterResource(R.drawable.quiz_confetti), contentDescription = null,
            modifier = Modifier.matchParentSize(), contentScale = ContentScale.Fit, alignment = Alignment.TopCenter)
        PokemonImage(image, if (session.answered) session.current.correct.summary.identity.visibleName(language) else null,
            Modifier.size(260.dp), silhouette = !session.answered)
    }
}

@Composable
private fun QuizHeader(title: String, onBack: () -> Unit) {
    Row(Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp), verticalAlignment = Alignment.CenterVertically) {
        IconButton(onClick = onBack) { PocketDexIcon(PocketDexIconAsset.ArrowBack, stringResource(DesignR.string.pd_back)) }
        Text(title, Modifier.weight(1f).semantics { heading() }, textAlign = TextAlign.Center,
            style = MaterialTheme.typography.headlineSmall)
        Spacer(Modifier.width(48.dp))
    }
}

@Composable
private fun QuizOption(session: QuizSession, option: PokemonDetail, language: PokemonLanguage, actions: QuizActions, imageReady: Boolean) {
    val id = option.summary.identity.id
    val correct = session.answered && id == session.current.correct.summary.identity.id
    val chosen = session.answered && id == session.answers[session.index]
    val status = if (!session.answered) null else stringResource(when {
        correct -> R.string.quiz_option_correct
        chosen -> R.string.quiz_option_chosen
        else -> R.string.quiz_option_locked
    })
    Surface(Modifier.fillMaxWidth().heightIn(min = 48.dp)
        .clickable(enabled = !session.answered && imageReady, role = Role.Button) { actions.answer(session.id, session.index, id) }
        .semantics { selected = chosen; if (status != null) stateDescription = status },
        shape = PocketDexShapes.Container,
        color = if (correct) PocketDexColors.Yellow else if (chosen) PocketDexColors.Gray100 else PocketDexColors.White,
        contentColor = PocketDexColors.Gray500,
        border = if (correct) null else BorderStroke(1.dp, if (chosen) PocketDexColors.Gray400 else PocketDexColors.Gray200)) {
        Box(Modifier.padding(horizontal = 16.dp, vertical = 12.dp), contentAlignment = Alignment.Center) {
            Text(option.summary.identity.visibleName(language), textAlign = TextAlign.Center, style = MaterialTheme.typography.labelLarge)
        }
    }
}

@Composable
private fun AnswerOptions(session: QuizSession, language: PokemonLanguage, actions: QuizActions, imageReady: Boolean, modifier: Modifier) {
    Column(modifier, verticalArrangement = Arrangement.spacedBy(12.dp)) {
        session.current.options.forEach { option ->
            QuizOption(session, option, language, actions, imageReady)
        }
    }
}

@Composable
private fun Hints(session: QuizSession) {
    Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
        Text(stringResource(R.string.quiz_hints_title), style = MaterialTheme.typography.titleLarge)
        session.current.correct.summary.types.forEach { type ->
            val visual = VisualType.valueOf(type.name)
            PokemonTypeBadge(stringResource(visual.labelRes), visual)
        }
        PokemonStat.entries.forEach { stat -> PokemonStatRow(statLabel(stat), session.current.correct.statistics[stat]) }
    }
}

@Composable
fun QuizResultsScreen(session: QuizSession?, onBack: () -> Unit, onRestart: () -> Unit, modifier: Modifier = Modifier,
    restartEnabled: Boolean = true) {
    Column(modifier.fillMaxSize()) {
        QuizHeader(stringResource(R.string.quiz_results), onBack)
        BoxWithConstraints(Modifier.weight(1f).fillMaxWidth()) {
            Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).heightIn(min = maxHeight).padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(24.dp, Alignment.CenterVertically)) {
                if (session?.completed != true) PocketDexStateContent(PocketDexContentState.Error, stringResource(R.string.quiz_result_missing))
                else {
                    Text(stringResource(R.string.quiz_finished), style = MaterialTheme.typography.displaySmall, textAlign = TextAlign.Center)
                    Text(stringResource(R.string.quiz_score, session.score), style = MaterialTheme.typography.displayMedium,
                        modifier = Modifier.semantics { liveRegion = LiveRegionMode.Polite })
                    Text(stringResource(R.string.quiz_score_explanation), textAlign = TextAlign.Center)
                    PocketDexButton(stringResource(R.string.quiz_restart), onRestart, Modifier.fillMaxWidth(), enabled = restartEnabled)
                }
            }
        }
    }
}

@Composable
private fun statLabel(stat: PokemonStat): String = stringResource(when (stat) {
    PokemonStat.Hp -> R.string.quiz_hp
    PokemonStat.Attack -> R.string.quiz_attack
    PokemonStat.Defense -> R.string.quiz_defense
    PokemonStat.SpecialAttack -> R.string.quiz_special_attack
    PokemonStat.SpecialDefense -> R.string.quiz_special_defense
    PokemonStat.Speed -> R.string.quiz_speed
})
