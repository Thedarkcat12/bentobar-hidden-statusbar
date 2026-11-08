package com.meridian11.books.bentobar.data

import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** The Desktop Mode settings apply only while the strip floats with no status bar ([DesktopMode.active]). */
class DesktopModeTest {
    private val timer = ItemConfig("t", "timer", Section.HIDDEN, whenActive = true)
    private val memory = ItemConfig("m", "memory", Section.HIDDEN)
    private val clock = ItemConfig("c", "clock", Section.SHOWN)
    private val items = listOf(timer, memory, clock)

    @After fun reset() { DesktopMode.active.value = false }

    @Test fun nothingChangesWhileDesktopModeIsNotActive() {
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, desktopShowAll = true, position = Position.LEFT, desktopAlign = DesktopAlign.RIGHT, items = items)
        assertEquals(HiddenMode.CLICK, c.hiddenModeNow)
        assertEquals(Position.LEFT, c.positionNow)
        assertFalse(c.shows(memory, active = false))
        assertEquals(listOf(memory), c.behindChevron { false }.filter { it.id == "m" })
    }

    @Test fun unhidingShowsEverythingWhileActive() {
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, desktopShowAll = true, items = items)
        DesktopMode.active.value = true
        assertEquals(HiddenMode.SHOW_ALL, c.hiddenModeNow)
        assertTrue(c.shows(memory, active = false))
        // A "Show when" item still waits for its rule, as under Show everything.
        assertFalse(c.shows(timer, active = false))
        assertTrue(c.shows(timer, active = true))
        assertTrue(c.behindChevron { false }.isEmpty())
    }

    @Test fun unhidingIsOffUnlessAskedFor() {
        val c = BarConfig(hiddenMode = HiddenMode.HOVER, items = items)
        DesktopMode.active.value = true
        assertEquals(HiddenMode.HOVER, c.hiddenModeNow)
        assertFalse(c.shows(memory, active = false))
    }

    @Test fun theSameAlignmentFollowsThePositionAndTheOthersOverrideIt() {
        DesktopMode.active.value = true
        assertEquals(Position.LEFT, BarConfig(position = Position.LEFT).positionNow)
        assertEquals(Position.CENTER, BarConfig(position = Position.LEFT, desktopAlign = DesktopAlign.CENTER).positionNow)
        assertEquals(Position.RIGHT, BarConfig(position = Position.LEFT, desktopAlign = DesktopAlign.RIGHT).positionNow)
        assertEquals(Position.LEFT, BarConfig(position = Position.RIGHT, desktopAlign = DesktopAlign.LEFT).positionNow)
    }

    @Test fun eachModeHasItsOwnOffsetAndItIsKeptOnTheScreen() {
        val c = BarConfig(offsetYDp = 4, desktopOffsetYDp = 12)
        assertEquals(4, c.offsetYNowDp)
        DesktopMode.active.value = true
        assertEquals(12, c.offsetYNowDp)
        assertEquals(MAX_OFFSET_Y_DP, BarConfig(desktopOffsetYDp = 500).offsetYNowDp)
        assertEquals(0, BarConfig(desktopOffsetYDp = -9).offsetYNowDp)
    }
}
