package com.example.pocketdex.ui.adaptive

import org.junit.Assert.assertEquals
import org.junit.Test

class UsableRegionTest {
    @Test fun noHingeUsesWholeRegion() {
        assertEquals(PixelRect(0, 0, 800, 600), usableRegion(800, 600, emptyList()))
    }

    @Test fun bookHingeUsesLargestPaneAndLeftPaneOnTie() {
        assertEquals(PixelRect(0, 0, 390, 600), usableRegion(800, 600,
            listOf(WindowObstruction(PixelRect(390, 0, 410, 600), true))))
        assertEquals(PixelRect(310, 0, 800, 600), usableRegion(800, 600,
            listOf(WindowObstruction(PixelRect(290, 0, 310, 600), true))))
    }

    @Test fun zeroWidthSeparatingFoldStillSplits() {
        assertEquals(PixelRect(0, 0, 400, 600), usableRegion(800, 600,
            listOf(WindowObstruction(PixelRect(400, 0, 400, 600), true))))
    }

    @Test fun tabletopExcludesOcclusionAndHingeOutsideInsetsHasNoEffect() {
        assertEquals(PixelRect(0, 310, 400, 800), usableRegion(400, 800,
            listOf(WindowObstruction(PixelRect(0, 290, 400, 310), false))))
        assertEquals(PixelRect(0, 0, 400, 800), usableRegion(400, 800,
            listOf(WindowObstruction(PixelRect(-20, 0, -10, 800), true))))
    }
}
