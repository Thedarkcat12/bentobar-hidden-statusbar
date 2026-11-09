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
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, position = Position.LEFT, desktopAlign = DesktopAlign.RIGHT, items = items)
        assertEquals(Position.LEFT, c.positionNow)
        assertFalse(c.shows(memory, active = false))
        assertEquals(listOf(memory), c.behindChevron { false }.filter { it.id == "m" })
    }

    @Test fun anItemWithItsOwnPlaceMovesWhileActive() {
        val hiddenHere = ItemConfig("m", "memory", Section.HIDDEN, desktop = DesktopWhere.SHOWN)
        val shownHere = ItemConfig("c", "clock", Section.SHOWN, desktop = DesktopWhere.OFF)
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, items = listOf(hiddenHere, shownHere))
        assertFalse(c.shows(hiddenHere, active = false))
        assertTrue(c.shows(shownHere, active = false))
        DesktopMode.active.value = true
        assertTrue(c.shows(hiddenHere, active = false))
        assertFalse(c.shows(shownHere, active = false))
        assertEquals(Section.OFF, shownHere.sectionNow)
        assertTrue(c.behindChevron { false }.isEmpty())
    }

    @Test fun sameAsUsualChangesNothing() {
        val item = ItemConfig("m", "memory", Section.HIDDEN)
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, items = listOf(item))
        DesktopMode.active.value = true
        assertEquals(Section.HIDDEN, item.sectionNow)
        assertEquals(listOf(item), c.behindChevron { false })
    }

    @Test fun anItemHiddenOnlyInDesktopModeWaitsBehindTheChevron() {
        val item = ItemConfig("c", "clock", Section.SHOWN, desktop = DesktopWhere.HIDDEN)
        val c = BarConfig(hiddenMode = HiddenMode.CLICK, items = listOf(item))
        assertTrue(c.shows(item, active = false))
        DesktopMode.active.value = true
        assertFalse(c.shows(item, active = false))
        assertEquals(listOf(item), c.behindChevron { false })
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

    @Test fun theChevronStandsAtTheEndNextToTheMiddleAndAtTheChosenSideWhenCentred() {
        val c = BarConfig()
        assertTrue(c.chevronOnLeft(Position.RIGHT))
        assertFalse(c.chevronOnLeft(Position.LEFT))
        assertTrue(c.chevronOnLeft(Position.CENTER))
        assertFalse(c.copy(chevronCenter = ChevronSide.RIGHT).chevronOnLeft(Position.CENTER))
        // The side chosen for a centred strip changes nothing for the others.
        assertTrue(c.copy(chevronCenter = ChevronSide.RIGHT).chevronOnLeft(Position.RIGHT))
        assertFalse(c.copy(chevronCenter = ChevronSide.RIGHT).chevronOnLeft(Position.LEFT))
    }

    @Test fun anItemsDesktopSettingsStartAsTheRegularOnesAndThenStandApart() {
        val c = ItemConfig("m", "media", Section.SHOWN, options = mapOf("art" to "true"))
        // A change made for Desktop Mode leaves the regular settings alone.
        val d = c.changedInDesktop { it.with("progress", "true") }
        assertEquals(mapOf("art" to "true"), d.options)
        assertEquals(mapOf("art" to "true", "progress" to "true"), d.deskOptions)
        // A change made for the regular layout afterwards does not reach Desktop Mode.
        val r = d.with("art", "false")
        assertEquals(mapOf("art" to "true", "progress" to "true"), r.deskView().options)
        // Without a setting of its own, Desktop Mode follows the regular ones.
        assertEquals(c.options, c.deskView().options)
        // A change that touches no display or option makes none of its own.
        val moved = c.changedInDesktop { it.copy(whenActive = true) }
        assertEquals(null, moved.deskOptions)
        assertTrue(moved.whenActive)
    }

    @Test fun anItemIsAsItIsInDesktopModeOnlyWhileThatLayoutIsActive() {
        val c = ItemConfig("m", "media", Section.SHOWN, options = mapOf("a" to "1"), deskOptions = mapOf("a" to "2"), deskDisplay = Display.ICON)
        DesktopMode.active.value = false
        assertEquals("1", c.now.opt("a", ""))
        DesktopMode.active.value = true
        try {
            assertEquals("2", c.now.opt("a", ""))
            assertEquals(Display.ICON, c.now.display)
            assertEquals(TextSize.LARGE, BarConfig(textSize = TextSize.SMALL, desktopTextSize = TextSize.LARGE).textSizeNow)
            assertEquals(TextSize.SMALL, BarConfig(textSize = TextSize.SMALL).textSizeNow)
            assertEquals(Pill.SOLID, BarConfig(pill = Pill.NONE, desktopPill = Pill.SOLID).pillNow)
        } finally { DesktopMode.active.value = false }
    }
}
