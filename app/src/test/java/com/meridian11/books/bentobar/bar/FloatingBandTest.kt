package com.meridian11.books.bentobar.bar

import com.meridian11.books.bentobar.bar.FloatingBand.Free
import com.meridian11.books.bentobar.data.Position
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Where the strip floats when the device has no status bar: where its alignment puts it, and clear of every caption's two ends. */
class FloatingBandTest {
    private val screenW = 2000
    private val screenH = 1200
    private val band = 100
    private val edge = 0
    private val title = 300
    private val controls = 400

    private fun place(need: Int, align: Position, vararg w: AppWindow, titleReserve: Int = title, top: Int = 0, bottom: Int = band) =
        FloatingBand.place(w.toList(), screenW, top, bottom, edge, titleReserve, controls, need, align)

    private fun gaps(vararg w: AppWindow) = FloatingBand.gaps(w.toList(), screenW, 0, band, edge, title, controls)

    // --- the stretches no caption takes

    @Test fun nothingOnTheDesktopIsTheWholeBand() {
        assertEquals(listOf(Free(0, screenW)), gaps())
    }

    @Test fun aMaximizedWindowLeavesTheMiddleOfItsCaption() {
        // Icon and name at the left, buttons at the right: the free stretch is between them.
        assertEquals(listOf(Free(title, screenW - controls)), gaps(AppWindow(1, 0, 0, screenW, screenH - 100)))
    }

    @Test fun aWindowBelowTheBandDoesNotCount() {
        assertEquals(listOf(Free(0, screenW)), gaps(AppWindow(1, 300, band + 5, 1500, 900)))
    }

    @Test fun aWindowJustInsideTheBandDoes() {
        // Window 300..1500: free are 0..300, its caption's middle 600..1100 and 1500..2000.
        assertEquals(listOf(Free(0, 300), Free(600, 1100), Free(1500, 2000)), gaps(AppWindow(1, 300, band - 1, 1500, 900)))
    }

    @Test fun aSmallWindowNearTheTopIsNotACaption() {
        // A tooltip or a menu has no title and buttons to keep clear of.
        assertEquals(listOf(Free(0, screenW)), gaps(AppWindow(1, 800, 0, 1000, 60)))
    }

    @Test fun aBandLoweredPastACaptionLeavesThatWindowOut() {
        // The strip lowered by 120 px lies under a window whose caption ends at 100: nothing of it is in the way.
        assertEquals(listOf(Free(0, screenW)), FloatingBand.gaps(listOf(AppWindow(1, 0, 0, screenW, 100)), screenW, 120, 220, edge, title, controls))
    }

    @Test fun neverNegativeOrOutsideTheScreen() {
        val g = gaps(AppWindow(1, -200, 0, screenW + 200, screenH))
        assertTrue(g.all { it.width > 0 && it.left >= 0 && it.right <= screenW })
    }

    // --- where the strip goes

    @Test fun withNoWindowsTheStripKeepsItsAlignedEdge() {
        // The whole band is the stretch: the strip's own alignment puts it flush at the screen's side.
        assertEquals(Free(0, screenW), place(500, Position.RIGHT))
        assertEquals(Free(0, screenW), place(500, Position.LEFT))
        assertEquals(Free(0, screenW), place(500, Position.CENTER))
    }

    @Test fun aWindowAwayFromTheStripDoesNotMoveIt() {
        // A freeform window 100..900 has its title and buttons far from a strip at the right: it stays there,
        // in the stretch that holds it, instead of going to whichever stretch is widest.
        val w = AppWindow(1, 100, 0, 900, 700)
        assertEquals(Free(900, screenW), place(500, Position.RIGHT, w))
        // And a strip at the left stays clear of that window's icon and name by using the stretch before it.
        assertEquals(Free(0, 100), place(80, Position.LEFT, w))
    }

    @Test fun aMaximizedWindowMovesTheStripLeftOfItsButtons() {
        val w = AppWindow(1, 0, 0, screenW, screenH - 100)
        assertEquals(Free(title, screenW - controls), place(500, Position.RIGHT, w))
    }

    @Test fun aStripTooWideForTheCaptionTakesTheWidestStretch() {
        // 1500 px don't fit between 300 and 1600: the widest stretch it is, and the strip drops what doesn't fit.
        val w = AppWindow(1, 0, 0, screenW, screenH - 100)
        assertEquals(Free(title, screenW - controls), place(1500, Position.RIGHT, w))
    }

    @Test fun twoSnappedWindowsGiveTheNearestStretchToTheAlignment() {
        // Free: 300..600 (left window's caption middle) and 1300..1600 (the right one's).
        val l = AppWindow(1, 0, 0, 1000, screenH)
        val r = AppWindow(2, 1000, 0, 2000, screenH)
        assertEquals(Free(1300, 1600), place(250, Position.RIGHT, l, r))
        assertEquals(Free(300, 600), place(250, Position.LEFT, l, r))
    }

    @Test fun aCentredStripStaysPutWhereTheCentreIsFree() {
        // The middle of a maximized window's caption is free: centre alignment keeps its stretch.
        assertEquals(Free(title, screenW - controls), place(500, Position.CENTER, AppWindow(1, 0, 0, screenW, screenH - 100)))
    }

    @Test fun coveringTheTitleLeavesRoomForAWiderStrip() {
        val w = AppWindow(1, 0, 0, screenW, screenH - 100)
        // 1000 px fit in the caption's middle either way (300..1600 is 1300 wide), so the title is left alone...
        assertEquals(Free(title, screenW - controls), place(1000, Position.RIGHT, w))
        // ...but 1500 px fit only where the title isn't reserved: the stretch then starts at the window's left edge.
        assertEquals(Free(title, screenW - controls), place(1500, Position.RIGHT, w))
        assertEquals(Free(0, screenW - controls), place(1500, Position.RIGHT, w, titleReserve = 0))
    }

    @Test fun nothingFreeGivesNothing() {
        // Narrower than the two reserves together isn't a caption; just wide enough leaves no middle at all.
        val f = FloatingBand.place(listOf(AppWindow(1, 0, 0, screenW, screenH)), screenW, 0, band, edge, 1000, 1000, 100, Position.RIGHT)
        assertEquals(0, f.width)
    }

    @Test fun anUnknownWidthStillPlacesTheStripAtItsAlignedSide() {
        // Before the strip has measured itself (need 0): the stretch holding its side, or the nearest one.
        assertEquals(Free(900, screenW), place(0, Position.RIGHT, AppWindow(1, 100, 0, 900, 700)))
        assertEquals(Free(title, screenW - controls), place(0, Position.RIGHT, AppWindow(1, 0, 0, screenW, screenH - 100)))
    }

    // --- full screen and the taskbar

    @Test fun aWindowCoveringTheWholeDisplayIsFullScreen() {
        assertTrue(FloatingBand.coversDisplay(listOf(AppWindow(1, 0, 0, screenW, screenH)), screenW, screenH))
        assertTrue(FloatingBand.coversDisplay(listOf(AppWindow(1, 0, 0, screenW, screenH - 1)), screenW, screenH))
    }

    @Test fun aWideShortWindowAlongTheBottomIsATaskbar() {
        assertTrue(FloatingBand.hasTaskbar(listOf(AppWindow(9, 0, screenH - 108, screenW, screenH)), screenW, screenH))
    }

    @Test fun otherSystemWindowsAreNotATaskbar() {
        // A notification popup (narrow), a status bar at the top, or a shade as tall as the screen: none is the taskbar.
        assertFalse(FloatingBand.hasTaskbar(listOf(AppWindow(9, 1400, 700, screenW, screenH)), screenW, screenH))
        assertFalse(FloatingBand.hasTaskbar(listOf(AppWindow(9, 0, 0, screenW, 100)), screenW, screenH))
        assertFalse(FloatingBand.hasTaskbar(listOf(AppWindow(9, 0, 0, screenW, screenH)), screenW, screenH))
        assertFalse(FloatingBand.hasTaskbar(emptyList(), screenW, screenH))
    }

    @Test fun aMaximizedWindowAboveTheTaskbarIsNotFullScreen() {
        assertFalse(FloatingBand.coversDisplay(listOf(AppWindow(1, 0, 0, screenW, screenH - 108)), screenW, screenH))
        assertFalse(FloatingBand.coversDisplay(emptyList(), screenW, screenH))
    }
}
