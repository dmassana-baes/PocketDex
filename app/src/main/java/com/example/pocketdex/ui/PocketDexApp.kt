package com.example.pocketdex.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.DisposableEffect
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.compose.LocalLifecycleOwner
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.platform.LocalWindowInfo
import androidx.compose.ui.res.stringResource
import androidx.window.layout.FoldingFeature
import com.example.pocketdex.R
import com.example.pocketdex.core.designsystem.components.PocketDexButton
import com.example.pocketdex.core.designsystem.components.PocketDexIcon
import com.example.pocketdex.core.designsystem.components.PocketDexIconAsset
import com.example.pocketdex.core.designsystem.components.PocketDexInlineWarning
import com.example.pocketdex.core.designsystem.components.PocketDexNavigation
import com.example.pocketdex.core.designsystem.components.PocketDexNavigationItem
import com.example.pocketdex.core.designsystem.model.navigationPresentation
import com.example.pocketdex.navigation.FeatureEntries
import com.example.pocketdex.navigation.PocketDexNavDisplay
import com.example.pocketdex.navigation.TopLevel
import com.example.pocketdex.navigation.rememberAppNavigator
import com.example.pocketdex.ui.adaptive.AdaptiveHost
import com.example.pocketdex.feature.home.presentation.HomeRoute
import com.example.pocketdex.feature.detail.presentation.DetailRoute
import com.example.pocketdex.feature.favorites.presentation.FavoritesRoute
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import com.example.pocketdex.feature.compare.presentation.CompareViewModel
import com.example.pocketdex.feature.compare.presentation.CompareRoute
import com.example.pocketdex.feature.compare.presentation.SelectorRoute
import com.example.pocketdex.feature.compare.presentation.CompareResultsRoute
import com.example.pocketdex.feature.quiz.presentation.QuizViewModel
import com.example.pocketdex.feature.quiz.presentation.QuizRoute
import com.example.pocketdex.feature.quiz.presentation.QuizResultsRoute

@Composable
fun PocketDexApp(foldingFeatures: List<FoldingFeature>, features: FeatureEntries? = null, taskId: Int = 0) {
    val entries = features ?: availableFeatures(taskId)
    val navigator = rememberAppNavigator()
    val selected = navigator.snapshot.selected
    val focusManager = LocalFocusManager.current
    val keyboard = LocalSoftwareKeyboardController.current
    val widthDp = with(LocalDensity.current) { LocalWindowInfo.current.containerSize.width.toDp().value.toInt() }
    val presentation = navigationPresentation(widthDp)
    val labels = TopLevel.entries.associateWith { stringResource(it.labelResource) }
    val items = TopLevel.entries.map { destination ->
        PocketDexNavigationItem(destination.name, labels.getValue(destination)) {
            PocketDexIcon(destination.icon(selected == destination), contentDescription = null)
        }
    }
    Surface(Modifier.fillMaxSize()) {
        AdaptiveHost(
            presentation = presentation,
            foldingFeatures = foldingFeatures,
            navigation = {
                PocketDexNavigation(items, selected.name, presentation, {
                    val destination = TopLevel.valueOf(it)
                    if (destination != navigator.snapshot.selected) {
                        focusManager.clearFocus(force = true)
                        keyboard?.hide()
                    }
                    navigator.select(destination)
                })
            },
            content = {
                Column(Modifier.fillMaxSize()) {
                    if (navigator.needsRecoveryNotice) {
                        Column {
                            PocketDexInlineWarning(stringResource(R.string.navigation_recovery_notice))
                            PocketDexButton(stringResource(R.string.dismiss), navigator::dismissRecoveryNotice)
                        }
                    }
                    PocketDexNavDisplay(navigator, entries, Modifier.weight(1f))
                }
            },
        )
    }
}

private val TopLevel.labelResource: Int get() = when (this) {
    TopLevel.Home -> R.string.destination_home
    TopLevel.Comparator -> R.string.destination_comparator
    TopLevel.Quiz -> R.string.destination_quiz
    TopLevel.Favorites -> R.string.destination_favorites
}

private fun TopLevel.icon(selected: Boolean): PocketDexIconAsset = when (this) {
    TopLevel.Home -> if (selected) PocketDexIconAsset.HomeFilled else PocketDexIconAsset.Home
    TopLevel.Comparator -> PocketDexIconAsset.Comparator
    TopLevel.Quiz -> if (selected) PocketDexIconAsset.QuizFilled else PocketDexIconAsset.Quiz
    TopLevel.Favorites -> if (selected) PocketDexIconAsset.HeartFilled else PocketDexIconAsset.Heart
}

/** App retains feature owners while secondary entries select IDs or display results. */
@Composable
private fun availableFeatures(taskId: Int): FeatureEntries {
    val compare: CompareViewModel = hiltViewModel()
    val quiz: QuizViewModel = hiltViewModel()
    val lifecycle = LocalLifecycleOwner.current.lifecycle
    DisposableEffect(quiz, lifecycle) {
        val observer = LifecycleEventObserver { _, event ->
            if (event == Lifecycle.Event.ON_RESUME) quiz.setForeground(true)
            if (event == Lifecycle.Event.ON_PAUSE) quiz.setForeground(false)
        }
        lifecycle.addObserver(observer)
        quiz.setForeground(lifecycle.currentState.isAtLeast(Lifecycle.State.RESUMED))
        onDispose { lifecycle.removeObserver(observer); quiz.setForeground(false) }
    }
    LaunchedEffect(quiz, taskId) { quiz.attach(taskId) }
    return FeatureEntries(
        home = { onDetail -> HomeRoute(onDetail) },
        favorites = { onDetail, onExplore -> FavoritesRoute(onDetail, onExplore) },
        detail = { id, onBack -> DetailRoute(id, onBack) },
        comparator = { onSelect, onResults -> CompareRoute(compare, onSelect, onResults) },
        compareSelector = { slot, excluded, onSelected, onBack -> SelectorRoute(slot, excluded, onSelected, onBack, compare) },
        onCompareSelection = { slot, id -> compare.select(slot, id) },
        comparisonResults = { left, right, onBack -> CompareResultsRoute(compare, left, right, onBack) },
        quiz = { onResults, onHome -> QuizRoute(quiz, onResults, onHome) },
        quizResults = { id, onBack -> QuizResultsRoute(quiz, id, onBack) },
    )
}
