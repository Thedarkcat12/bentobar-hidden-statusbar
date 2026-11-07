package com.meridian11.books.bentobar.bar

/**
 * Where the strip goes on a device with no status bar window (a tablet's PC mode: the clock and icons
 * live in a taskbar at the bottom, and every window has a caption of its own at its top edge). The
 * strip then floats along the top of the screen, in the stretch that no window's caption uses.
 *
 * A caption is not something accessibility can read: the system draws it, with the app's icon and
 * name at the window's left and its buttons at the right. So the caption is taken as two reserved
 * stretches at the window's two ends ([title] and [controls] pixels), and what lies between them,
 * and any part of the band no window reaches, is free. No Android types, so the cases are unit-tested.
 */
object FloatingBand {
    /** The stretch of the band the strip may use, in screen pixels. */
    data class Free(val left: Int, val right: Int) {
        val width: Int get() = right - left
    }

    /**
     * True when a system window along the bottom edge, wide and not tall, is up: the taskbar that
     * takes the status bar's place. It hides with a full-screen app, so while it shows nothing is
     * full screen, whatever the home screen's own window reaches (it may well span the display).
     */
    fun hasTaskbar(system: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 2): Boolean =
        system.any { it.bottom >= screenH - slack && it.right - it.left >= screenW / 2 && it.bottom - it.top in 1..screenH / 4 }

    /**
     * True when an app window covers the whole display: a full-screen app has no caption and no
     * taskbar, and the strip would sit on top of its content, so it goes away as the status bar does.
     * [slack] absorbs the pixel or two a window's bounds can fall short by.
     */
    fun coversDisplay(windows: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 2): Boolean =
        windows.any { it.left <= slack && it.top <= slack && it.right >= screenW - slack && it.bottom >= screenH - slack }

    /**
     * The widest stretch of the top band, [bandH] pixels high, that no caption's [title] or [controls]
     * reserve takes, keeping [edge] pixels from each side of the screen. Where two stretches are
     * equally wide the right one wins: that is where the status bar's own items were.
     *
     * Only windows that reach into the band count, and only those wide enough to have a caption with
     * both ends (a menu or a tooltip near the top edge has none).
     */
    fun free(windows: List<AppWindow>, screenW: Int, bandH: Int, edge: Int, title: Int, controls: Int): Free {
        val minWidth = title + controls
        val taken = ArrayList<Pair<Int, Int>>()
        for (w in windows) {
            if (w.top >= bandH || w.bottom <= 0 || w.right - w.left < minWidth) continue
            val left = w.left.coerceIn(0, screenW)
            val right = w.right.coerceIn(0, screenW)
            taken += left to minOf(right, left + title)
            taken += maxOf(left, right - controls) to right
        }
        var best = Free(edge, edge)
        var cursor = edge
        val end = screenW - edge
        for ((l, r) in taken.sortedBy { it.first }) {
            val gapEnd = minOf(l, end)
            if (gapEnd > cursor && gapEnd - cursor >= best.width) best = Free(cursor, gapEnd)
            cursor = maxOf(cursor, r)
        }
        if (end > cursor && end - cursor >= best.width) best = Free(cursor, end)
        return best
    }
}
