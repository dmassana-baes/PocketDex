package com.example.pocketdex.navigation

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.Saver
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.pocketdex.core.domain.model.PokemonId

class AppNavigator(initial: NavigationSnapshot = NavigationSnapshot(), recoveryNotice: Boolean = false) {
    var snapshot by mutableStateOf(initial)
        private set
    var needsRecoveryNotice by mutableStateOf(recoveryNotice)
        private set

    fun select(destination: TopLevel) { snapshot = snapshot.select(destination) }
    fun exploreHome() { snapshot = snapshot.exploreHome() }
    fun exploreHomeFromFavorites() {
        if (snapshot.current == AppRoute.Area(TopLevel.Favorites)) exploreHome()
    }
    fun exploreHomeFromQuiz() {
        if (snapshot.current == AppRoute.Area(TopLevel.Quiz)) exploreHome()
    }
    fun back() { snapshot = snapshot.back() }
    /** UI callbacks belong to a particular entry; outgoing entries cannot pop a newer destination. */
    fun backFrom(route: AppRoute) {
        if (snapshot.current == route) back()
    }
    fun dismissRecoveryNotice() { needsRecoveryNotice = false }

    fun openDetail(id: PokemonId, origin: TopLevel = snapshot.selected) {
        if (snapshot.current != AppRoute.Area(origin)) return
        snapshot = snapshot.push(AppRoute.Detail(id.value, origin))
    }

    fun openCompareSelector(slot: Int, excluded: PokemonId?) {
        if (snapshot.current != AppRoute.Area(TopLevel.Comparator)) return
        snapshot = snapshot.push(AppRoute.CompareSelector(slot, excluded?.value))
    }

    fun showQuizResults(sessionId: String) {
        if (snapshot.current != AppRoute.Area(TopLevel.Quiz)) return
        snapshot = snapshot.push(AppRoute.QuizResults(sessionId))
    }

    fun showComparisonResults(left: PokemonId, right: PokemonId) {
        if (snapshot.current != AppRoute.Area(TopLevel.Comparator)) return
        snapshot = snapshot.push(AppRoute.ComparisonResults(left.value, right.value))
    }

    companion object {
        val StateSaver = Saver<AppNavigator, List<String>>(
            save = { listOf(NavigationSnapshotCodec.encode(it.snapshot), it.needsRecoveryNotice.toString()) },
            restore = {
                val restored = NavigationSnapshotCodec.restore(it.firstOrNull().orEmpty())
                AppNavigator(restored.snapshot, restored.needsRecoveryNotice || it.getOrNull(1) == "true")
            },
        )
    }
}

@Composable
fun rememberAppNavigator(): AppNavigator = rememberSaveable(saver = AppNavigator.StateSaver) { AppNavigator() }
