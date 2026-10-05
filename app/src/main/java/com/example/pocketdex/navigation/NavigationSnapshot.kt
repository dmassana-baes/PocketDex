package com.example.pocketdex.navigation

import kotlinx.serialization.Serializable
import kotlinx.serialization.SerializationException
import kotlinx.serialization.json.Json

@Serializable
data class DestinationStack(val destination: TopLevel, val entries: List<AppRoute>)

/** Task state only. Never written to preferences, database or a file. */
@Serializable
data class NavigationSnapshot(
    val version: Int = 1,
    val selected: TopLevel = TopLevel.Home,
    val stacks: List<DestinationStack> = TopLevel.entries.map { DestinationStack(it, listOf(AppRoute.Area(it))) },
) {
    init {
        require(version == 1)
        require(stacks.map { it.destination }.toSet() == TopLevel.entries.toSet())
        require(stacks.size == TopLevel.entries.size)
        stacks.forEach { stack ->
            require(stack.entries.firstOrNull() == AppRoute.Area(stack.destination))
            require(stack.entries.distinct().size == stack.entries.size)
            require(stack.entries.drop(1).all { route ->
                when (route) {
                    is AppRoute.Area -> false
                    is AppRoute.Detail -> route.origin == stack.destination
                    is AppRoute.CompareSelector -> stack.destination == TopLevel.Comparator
                    is AppRoute.ComparisonResults -> stack.destination == TopLevel.Comparator
                    is AppRoute.QuizResults -> stack.destination == TopLevel.Quiz
                }
            })
        }
    }

    fun stack(destination: TopLevel = selected): List<AppRoute> = stacks.single { it.destination == destination }.entries
    val current: AppRoute get() = stack().last()
    val canGoBack: Boolean get() = selected != TopLevel.Home || stack().size > 1

    fun select(destination: TopLevel): NavigationSnapshot = copy(selected = destination)

    fun push(route: AppRoute): NavigationSnapshot {
        require(route !is AppRoute.Area)
        val currentStack = stack()
        // Rapid clicks or repeated callbacks cannot create duplicate entries.
        if (route in currentStack) return this
        return replaceStack(selected, currentStack + route)
    }

    fun exploreHome(): NavigationSnapshot = replaceStack(TopLevel.Home, listOf(AppRoute.Area(TopLevel.Home)))
        .copy(selected = TopLevel.Home)

    fun back(): NavigationSnapshot = when {
        stack().size > 1 -> replaceStack(selected, stack().dropLast(1))
        selected != TopLevel.Home -> exploreHome()
        else -> this
    }

    private fun replaceStack(destination: TopLevel, entries: List<AppRoute>): NavigationSnapshot = copy(
        stacks = stacks.map { if (it.destination == destination) it.copy(entries = entries) else it },
    )
}

data class RestoredNavigation(val snapshot: NavigationSnapshot, val needsRecoveryNotice: Boolean)

object NavigationSnapshotCodec {
    fun encode(snapshot: NavigationSnapshot): String = Json.encodeToString(NavigationSnapshot.serializer(), snapshot)

    fun restore(encoded: String): RestoredNavigation = try {
        RestoredNavigation(Json.decodeFromString(NavigationSnapshot.serializer(), encoded), false)
    } catch (_: SerializationException) {
        RestoredNavigation(NavigationSnapshot(), true)
    } catch (_: IllegalArgumentException) {
        RestoredNavigation(NavigationSnapshot(), true)
    }
}
