package com.example.pocketdex.core.designsystem.model

/** A display format, never a persistence identifier. ASCII digits keep the agreed format in every locale. */
fun formatPokemonNumber(number: Int): String {
    require(number > 0) { "A national number must be positive" }
    return "#" + number.toString().padStart(3, '0')
}

/** Null is missing data, not zero. 255 is a shared visual reference, not a catalog maximum. */
fun statFraction(value: Int?): Float? = value?.let { (it / 255f).coerceIn(0f, 1f) }

enum class ComparisonResult { LeftHigher, RightHigher, Tie, Unavailable }

enum class NavigationPresentation { BottomBar, SideRail }

/** The host supplies the measured window width and places navigation outside occluded regions. */
fun navigationPresentation(windowWidthDp: Int): NavigationPresentation {
    require(windowWidthDp >= 0)
    return if (windowWidthDp < 600) NavigationPresentation.BottomBar else NavigationPresentation.SideRail
}
