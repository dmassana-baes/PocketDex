package com.example.pocketdex.feature.quiz.presentation

import androidx.compose.runtime.*
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil3.compose.AsyncImagePainter
import coil3.compose.rememberAsyncImagePainter
import coil3.request.ImageRequest
import com.example.pocketdex.core.designsystem.components.PokemonImageState
import com.example.pocketdex.core.domain.model.PokemonLanguage
import com.example.pocketdex.core.domain.quiz.QuizSession
import java.io.File

@Composable
fun QuizRoute(viewModel: QuizViewModel, onResults: (String) -> Unit, onHome: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val language = if (LocalConfiguration.current.locales[0].language == "es") PokemonLanguage.Spanish else PokemonLanguage.English
    DisposableEffect(viewModel) { viewModel.enter(); onDispose { viewModel.leave() } }
    QuizScreen(state, language, QuizActions(viewModel::start,
        { viewModel.continueSession()?.let(onResults) }, viewModel::restart, viewModel::confirmRestart,
        viewModel::cancelRestart, viewModel::retry, { viewModel.leave(); onHome() }, viewModel::answer,
        { id, index -> viewModel.advance(id, index)?.let(onResults) }, viewModel::showHints),
        { session -> QuizImage(session, state.files[session.current.correct.summary.identity.id], viewModel) })
}

@Composable
fun QuizResultsRoute(viewModel: QuizViewModel, sessionId: String, onBack: () -> Unit) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    QuizResultsScreen(state.session?.takeIf { it.id == sessionId }, onBack,
        { viewModel.restart(); onBack() }, restartEnabled = state.attached && state.phase != QuizPhase.Preparing)
}

@Composable
private fun QuizImage(session: QuizSession, path: String?, viewModel: QuizViewModel): PokemonImageState {
    val context = LocalContext.current
    val request = remember(context, path) { ImageRequest.Builder(context).data(path?.let(::File)).size(440)
        .memoryCachePolicy(coil3.request.CachePolicy.DISABLED).diskCachePolicy(coil3.request.CachePolicy.DISABLED).build() }
    val painter = rememberAsyncImagePainter(request)
    val state by painter.state.collectAsState()
    LaunchedEffect(session.id, session.index, path, state) {
        if (path == null || state is AsyncImagePainter.State.Error) viewModel.imageFailed(session.id, session.index)
    }
    return when (state) {
        is AsyncImagePainter.State.Success -> PokemonImageState.Ready(painter)
        is AsyncImagePainter.State.Error -> PokemonImageState.Error
        else -> PokemonImageState.Loading
    }
}
