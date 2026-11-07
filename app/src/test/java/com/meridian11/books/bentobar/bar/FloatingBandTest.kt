package com.meridian11.books.bentobar.bar

import com.meridian11.books.bentobar.bar.FloatingBand.Free
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Where the strip floats when the device has no status bar: the top band, minus every window caption's two ends. */
class FloatingBandTest {
    private val screenW = 2000
    private val screenH = 1200
    private val band = 100
    private val edge = 20
    private val title = 300
    private val controls = 400

    private fun free(vararg w: AppWindow) = FloatingBand.free(w.toList(), screenW, band, edge, title, controls)

    @Test fun nothingOnTheDesktopIsTheWholeBand() {
        assertEquals(Free(edge, screenW - edge), free())
    }

    @Test fun aMaximizedWindowLeavesTheMiddleOfItsCaption() {
        // Icon and name at the left, buttons at the right: the strip sits between them.
        assertEquals(Free(title, screenW - controls), free(AppWindow(1, 0, 0, screenW, screenH - 100)))
    }

    @Test fun aWiderControlsReserveMovesTheStripFurtherLeft() {
        // Room for a fourth caption button: the reserve grows, the strip's right end follows it.
        val wide = FloatingBand.free(listOf(AppWindow(1, 0, 0, screenW, screenH)), screenW, band, edge, title, controls + 150)
        assertEquals(Free(title, screenW - controls - 150), wide)
    }

    @Test fun aWindowBelowTheBandDoesNotCount() {
        assertEquals(Free(edge, screenW - edge), free(AppWindow(1, 300, band + 5, 1500, 900)))
    }

    @Test fun aWindowJustInsideTheBandDoes() {
        // A freeform window whose caption starts partway down the band still collides with a strip that tall.
        // Window 300..1500: the free stretches are 20..300 (280), its caption's middle 600..1100 (500) and 1500..1980 (480).
        assertEquals(Free(600, 1100), free(AppWindow(1, 300, band - 1, 1500, 900)))
    }

    @Test fun aFreeformWindowInTheMiddleLeavesTheWiderSide() {
        // Window 600..1400: free are 20..600 (580 wide), its caption's middle 900..1000 (100), and 1400..1980 (580: the right one wins the tie).
        assertEquals(Free(1400, screenW - edge), free(AppWindow(1, 600, 0, 1400, 700)))
    }

    @Test fun twoSnappedWindowsGiveTheWiderMiddle() {
        // Left half's middle is 300..600 (300 wide), right half's is 1300..1600 (300 wide): the right one wins the tie.
        val f = free(AppWindow(1, 0, 0, 1000, screenH), AppWindow(2, 1000, 0, 2000, screenH))
        assertEquals(Free(1300, 1600), f)
    }

    @Test fun aSmallWindowNearTheTopIsNotACaption() {
        // A tooltip or a menu has no title and buttons to keep clear of.
        assertEquals(Free(edge, screenW - edge), free(AppWindow(1, 800, 0, 1000, 60)))
    }

    @Test fun aWindowThatLeavesNoRoomGivesNoRoom() {
        // Narrower than the two reserves together is not a caption; just wide enough leaves a middle of nothing.
        val f = FloatingBand.free(listOf(AppWindow(1, 0, 0, screenW, screenH)), screenW, band, edge, 1000, 1000)
        assertEquals(0, f.width)
    }

    @Test fun neverNegativeOrOutsideTheScreen() {
        val f = free(AppWindow(1, -200, 0, screenW + 200, screenH))
        assertTrue(f.width >= 0)
        assertTrue(f.left >= 0 && f.right <= screenW)
    }

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
