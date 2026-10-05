package com.example.pocketdex.navigation

import com.example.pocketdex.core.domain.model.PokemonId
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class NavigationSnapshotTest {
    @Test fun newTaskStartsAtHomeAndLetsSystemExit() {
        val fresh = NavigationSnapshot()
        assertEquals(AppRoute.Area(TopLevel.Home), fresh.current)
        assertFalse(fresh.canGoBack)
        assertEquals(fresh, fresh.back())
    }

    @Test fun switchingAndReselectingKeepEachStackWithoutDuplicates() {
        val detail = AppRoute.Detail(25, TopLevel.Home)
        val home = NavigationSnapshot().push(detail)
        val favorites = home.select(TopLevel.Favorites)
        assertEquals(favorites, favorites.select(TopLevel.Favorites))
        assertEquals(detail, favorites.select(TopLevel.Home).current)
        assertEquals(2, home.push(detail).stack().size)
    }

    @Test fun detailBackReturnsToItsActualOrigin() {
        TopLevel.entries.filter { it == TopLevel.Home || it == TopLevel.Favorites }.forEach {
            val opened = NavigationSnapshot().select(it).push(AppRoute.Detail(25, it))
            assertEquals(AppRoute.Area(it), opened.back().current)
            assertEquals(it, opened.back().selected)
        }
    }

    @Test fun principalBackGoesHomeAndExplorePopsOnlyHomeSecondaryRoutes() {
        val snapshot = NavigationSnapshot().push(AppRoute.Detail(25, TopLevel.Home))
            .select(TopLevel.Comparator).push(AppRoute.CompareSelector(0, 1))
            .select(TopLevel.Favorites)
        val explored = snapshot.exploreHome()
        assertEquals(AppRoute.Area(TopLevel.Home), snapshot.back().current)
        assertEquals(AppRoute.Area(TopLevel.Home), explored.current)
        assertEquals(snapshot.stack(TopLevel.Comparator), explored.stack(TopLevel.Comparator))
    }

    @Test fun recoveryRestoresExactRoutesSlotsAndSessionButFreshTaskDoesNot() {
        val snapshot = NavigationSnapshot().push(AppRoute.Detail(25, TopLevel.Home))
            .select(TopLevel.Comparator).push(AppRoute.CompareSelector(1, 25))
            .select(TopLevel.Quiz).push(AppRoute.QuizResults("session-42"))
        val restored = NavigationSnapshotCodec.restore(NavigationSnapshotCodec.encode(snapshot))
        assertEquals(snapshot, restored.snapshot)
        assertFalse(restored.needsRecoveryNotice)
        assertEquals(AppRoute.Area(TopLevel.Home), NavigationSnapshot().current)
    }

    @Test fun unsupportedOrCorruptSnapshotRequiresVisibleRecovery() {
        listOf("{", "{} bad", "{\"version\":99}", "{\"stacks\":[]}").forEach { encoded ->
            val restored = NavigationSnapshotCodec.restore(encoded)
            assertTrue(restored.needsRecoveryNotice)
            assertEquals(AppRoute.Area(TopLevel.Home), restored.snapshot.current)
        }
    }

    @Test fun outgoingOrRepeatedCallbacksCannotNavigateFromWrongOrigin() {
        val navigator = AppNavigator()
        navigator.openDetail(PokemonId(25), TopLevel.Home)
        navigator.openDetail(PokemonId(25), TopLevel.Home)
        assertEquals(2, navigator.snapshot.stack().size)
        navigator.select(TopLevel.Favorites)
        navigator.openDetail(PokemonId(1), TopLevel.Home)
        assertEquals(AppRoute.Area(TopLevel.Favorites), navigator.snapshot.current)
        navigator.showQuizResults("session-42")
        assertEquals(AppRoute.Area(TopLevel.Favorites), navigator.snapshot.current)
    }

    @Test(expected = IllegalArgumentException::class)
    fun invalidRestoredOriginIsRejected() {
        NavigationSnapshot().push(AppRoute.Detail(25, TopLevel.Favorites))
    }

    @Test fun repeatedBackFromDetailCannotLeaveItsOrigin() {
        val navigator = AppNavigator()
        navigator.select(TopLevel.Favorites)
        navigator.openDetail(PokemonId(25))
        val detail = navigator.snapshot.current
        navigator.backFrom(detail)
        navigator.backFrom(detail)
        assertEquals(AppRoute.Area(TopLevel.Favorites), navigator.snapshot.current)
    }

    @Test fun lateBackFromAnotherAreaCannotPopCurrentStack() {
        val navigator = AppNavigator()
        navigator.openDetail(PokemonId(25))
        val outgoing = navigator.snapshot.current
        navigator.select(TopLevel.Comparator)
        navigator.openCompareSelector(1, PokemonId(25))
        val before = navigator.snapshot
        navigator.backFrom(outgoing)
        assertEquals(before, navigator.snapshot)
    }

    @Test fun outgoingFavoritesExploreCannotInterruptAnotherArea() {
        val navigator = AppNavigator()
        navigator.select(TopLevel.Comparator)
        navigator.openCompareSelector(0, null)
        val before = navigator.snapshot
        navigator.exploreHomeFromFavorites()
        assertEquals(before, navigator.snapshot)
    }

    @Test fun comparisonResultsRestoreExactIdsAndBackReturnsToEdit() {
        val navigator = AppNavigator()
        navigator.select(TopLevel.Comparator)
        navigator.showComparisonResults(PokemonId(1), PokemonId(25))
        navigator.showComparisonResults(PokemonId(1), PokemonId(25))
        val results = AppRoute.ComparisonResults(1, 25)
        assertEquals(results, navigator.snapshot.current)
        assertEquals(2, navigator.snapshot.stack().size)
        val restored = NavigationSnapshotCodec.restore(NavigationSnapshotCodec.encode(navigator.snapshot))
        assertEquals(results, restored.snapshot.current)
        navigator.backFrom(results)
        navigator.backFrom(results)
        assertEquals(AppRoute.Area(TopLevel.Comparator), navigator.snapshot.current)
    }

    @Test(expected = IllegalArgumentException::class)
    fun comparisonResultsCannotRestoreDuplicateIds() {
        AppRoute.ComparisonResults(25, 25)
    }
}
