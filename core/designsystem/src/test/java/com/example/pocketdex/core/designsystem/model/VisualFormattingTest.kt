package com.example.pocketdex.core.designsystem.model

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class VisualFormattingTest {
    @Test
    fun nationalNumberKeepsAllDigits() {
        assertEquals("#001", formatPokemonNumber(1))
        assertEquals("#025", formatPokemonNumber(25))
        assertEquals("#1000", formatPokemonNumber(1000))
    }

    @Test
    fun statScaleClampsOnlyTheDrawingAndDistinguishesMissing() {
        assertEquals(0f, statFraction(0)!!, 0f)
        assertEquals(100f / 255f, statFraction(100)!!, .00001f)
        assertEquals(1f, statFraction(255)!!, 0f)
        assertEquals(1f, statFraction(300)!!, 0f)
        assertNull(statFraction(null))
    }

    @Test
    fun navigationThresholdUsesWindowWidthRatherThanOrientation() {
        assertEquals(NavigationPresentation.BottomBar, navigationPresentation(599))
        assertEquals(NavigationPresentation.SideRail, navigationPresentation(600))
        assertEquals(NavigationPresentation.SideRail, navigationPresentation(840))
    }
}
