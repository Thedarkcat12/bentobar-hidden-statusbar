package com.meridian11.books.bentobar.items

/**
 * The Brightness item's arithmetic, pure: the screen's brightness is a number from 0 to 255 (Android's own
 * scale), shown as a percentage and set from a slider's level, never to 0 (a screen that has gone dark is hard to get back).
 */
object BrightnessRules {
    const val MAX = 255
    /** The lowest value the item sets: the screen stays lit. */
    const val MIN_SET = 1
    /** A notch of the wheel, in percent. */
    const val WHEEL_PERCENT = 5

    fun percent(value: Int): Int = Math.round(value.coerceIn(0, MAX) * 100f / MAX)

    /** How full the slider is for [value], 0 to 1. */
    fun level(value: Int): Float = value.coerceIn(0, MAX) / MAX.toFloat()

    /** The value a slider at [level] sets. */
    fun value(level: Float): Int = Math.round(level.coerceIn(0f, 1f) * MAX).coerceIn(MIN_SET, MAX)

    /** [value] moved by [notches] of the wheel (up when positive). */
    fun wheel(value: Int, notches: Int): Int = (value + notches * Math.round(MAX * WHEEL_PERCENT / 100f)).coerceIn(MIN_SET, MAX)
}
