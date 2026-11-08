package com.meridian11.books.bentobar.data

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Items with a "Show when" rule are listed in the bar's card, so that the place they pop in at can be set. */
class PopupOrderTest {
    private val a = ItemConfig("a", "clock", Section.SHOWN)
    private val b = ItemConfig("b", "clock", Section.SHOWN)
    private val p = ItemConfig("p", "timer", Section.HIDDEN, whenActive = true)
    private val hidden = ItemConfig("h", "memory", Section.HIDDEN)
    private val items = listOf(a, p, b, hidden)

    @Test fun aPopUpIsListedInTheBarsCardAndOtherHiddenItemsAreNot() {
        assertEquals(Section.SHOWN, p.cardIn(false))
        assertEquals(Section.HIDDEN, hidden.cardIn(false))
        assertTrue(p.popsUpIn(false))
        assertFalse(hidden.popsUpIn(false))
    }

    @Test fun droppedAmongTheBarsItemsItKeepsItsRuleAndChangesPlace() {
        val moved = items.moved("p", Section.SHOWN, 0)
        assertEquals(listOf("p", "a", "b", "h"), moved.map { it.id })
        assertEquals(p, moved.first())
        assertEquals(listOf("a", "p", "b", "h"), items.moved("p", Section.SHOWN, 1).map { it.id })
        assertEquals(listOf("a", "b", "p", "h"), items.moved("p", Section.SHOWN, 2).map { it.id })
    }

    @Test fun droppedOnHiddenItIsNoPopUpAnyMore() {
        val m = items.moved("p", Section.HIDDEN, 0).first { it.id == "p" }
        assertEquals(Section.HIDDEN, m.section)
        assertFalse(m.whenActive)
    }

    @Test fun droppedOnOffItIsOff() {
        assertEquals(Section.OFF, items.moved("p", Section.OFF, 0).first { it.id == "p" }.section)
    }

    @Test fun anOrdinaryItemDroppedInTheBarIsShown() {
        assertEquals(Section.SHOWN, items.moved("h", Section.SHOWN, 0).first { it.id == "h" }.section)
    }

    @Test fun theDesktopModeViewListsByTheDesktopPlace() {
        val x = ItemConfig("x", "clock", Section.HIDDEN, desktop = DesktopWhere.SHOWN)
        assertEquals(Section.HIDDEN, x.cardIn(false))
        assertEquals(Section.SHOWN, x.cardIn(true))
        val y = ItemConfig("y", "timer", Section.SHOWN, whenActive = true, desktop = DesktopWhere.HIDDEN)
        assertEquals(Section.SHOWN, y.cardIn(false))
        assertEquals(Section.SHOWN, y.cardIn(true))
        assertTrue(y.popsUpIn(true))
    }
}
