package com.meridian11.books.bentobar.bar

import com.meridian11.books.bentobar.data.Position

/**
 * Where the strip goes on a device with no status bar window (Desktop Mode: the clock and icons live in a
 * taskbar at the bottom, and every window has a caption of its own at its top edge). The strip then floats
 * along the top of the screen, and keeps clear of what a caption shows.
 *
 * A caption is not something accessibility can read: the system draws it, with the app's icon and name at
 * the window's left and its buttons at the right. So a caption is taken as two reserved stretches at the
 * window's two ends ([title] and [controls] pixels), and what lies between them, and any part of the band no
 * window reaches, is free. No Android types, so the cases are unit-tested.
 *
 * The strip stays where its alignment puts it, at the screen's right, centre or left, and moves only when a
 * reserved stretch would be under it: then to the nearest free stretch it fits in, or, where none is wide
 * enough, the widest one (it then drops what doesn't fit, into the ‹ menu).
 */
object FloatingBand {
    /** A stretch of the band, in screen pixels. */
    data class Free(val left: Int, val right: Int) {
        val width: Int get() = right - left
    }

    /**
     * True when a system window along the bottom edge, wide and not tall, is up: the taskbar that
     * takes the status bar's place. It hides with a full-screen app, so while it shows nothing is
     * full screen, whatever the home screen's own window reaches (it may well span the display).
     */
    fun hasTaskbar(system: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 2): Boolean = taskbarTop(system, screenW, screenH, slack) != null

    /** The top edge of the taskbar (see [hasTaskbar]), or null when none shows. */
    fun taskbarTop(system: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 2): Int? =
        system.filter { it.bottom >= screenH - slack && it.right - it.left >= screenW / 2 && it.bottom - it.top in 1..screenH / 4 }.minOfOrNull { it.top }

    /**
     * The windows that fill the screen from its top edge: across its width (give or take [slack] pixels, since a window's
     * bounds can fall short) and down most of its height (a window under the taskbar, or ending at it, both count).
     * Maximized apps do, and so does the desktop itself, which is the lowest of them. [windows] keep their order.
     */
    fun fills(windows: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 12): List<AppWindow> =
        windows.filter { it.left <= slack && it.right >= screenW - slack && it.top <= slack && it.bottom >= screenH * 7 / 10 }

    /**
     * The windows snapped to one side or a part of the screen (split screen): from the top edge down, but not across the
     * whole width. They have no caption, only a handle at the middle of their top, which the strip keeps clear of
     * ([handles]). A window's own width is what it is: one narrower than [minWidth] has no room for a handle.
     */
    fun snapped(windows: List<AppWindow>, screenW: Int, screenH: Int, slack: Int = 12, minWidth: Int = 0): List<AppWindow> =
        windows.filter { it.top <= slack && it.bottom >= screenH * 7 / 10 && it.right - it.left < screenW - 2 * slack && it.right - it.left >= minWidth }

    /** The spans around the middles of [snapped] windows, [half] either side, for [gaps]'s `blocked`. */
    fun handles(snapped: List<AppWindow>, half: Int): List<Pair<Int, Int>> =
        snapped.map { val mid = (it.left + it.right) / 2; (mid - half) to (mid + half) }

    /**
     * The maximized app among [fills], top-most first: an app has no caption of its own to sit in then, so its own top bar lies
     * under the strip. The desktop's window fills the screen as well, and reaches the display's bottom, under the taskbar, where
     * an app maximized by the system stops above it. So a window that ends above the bottom is an app; of windows that reach
     * it, the top one is when there are two or more (the desktop is under it), and a lone one is the desktop, or an app that is
     * full screen and has no strip to show anyway.
     */
    fun maximizedApp(fills: List<AppWindow>, screenH: Int, slack: Int = 12): AppWindow? =
        fills.firstOrNull { it.bottom < screenH - slack } ?: fills.takeIf { it.size >= 2 }?.first()

    /**
     * The stretch the strip may use, when it wants [need] pixels (0 when not known yet) and is aligned by
     * [align]: the stretch that holds the strip where its alignment puts it, if there is one; else the
     * nearest stretch wide enough for it; else the widest. Nothing at all gives an empty stretch.
     */
    fun place(
        windows: List<AppWindow>, screenW: Int, bandTop: Int, bandBottom: Int, edge: Int, title: Int, controls: Int,
        need: Int, align: Position, blocked: List<Pair<Int, Int>> = emptyList(),
    ): Free {
        val gaps = gaps(windows, screenW, bandTop, bandBottom, edge, title, controls, blocked)
        if (gaps.isEmpty()) return Free(edge, edge)
        val whole = Free(edge, screenW - edge)
        val wanted = sits(whole, need, align, screenW)
        gaps.firstOrNull { it.left <= wanted.left && it.right >= wanted.right }?.let { return it }
        fun distance(g: Free) = Math.abs(centre(sits(g, need, align, screenW)) - centre(wanted))
        val fitting = gaps.filter { it.width >= need }
        if (fitting.isNotEmpty()) return fitting.minBy { distance(it) }
        val widest = gaps.maxOf { it.width }
        return gaps.filter { it.width == widest }.minBy { distance(it) }
    }

    /** Where a strip [need] wide sits in [g] under [align]: against its right or left end, or as near the screen's middle as it can. */
    private fun sits(g: Free, need: Int, align: Position, screenW: Int): Free = when (align) {
        Position.RIGHT -> Free(maxOf(g.left, g.right - need), g.right)
        Position.LEFT -> Free(g.left, minOf(g.right, g.left + need))
        Position.CENTER -> {
            val w = minOf(need, g.width)
            val left = (screenW / 2 - w / 2).coerceIn(g.left, maxOf(g.left, g.right - w))
            Free(left, left + w)
        }
    }

    private fun centre(f: Free) = (f.left + f.right) / 2
}
