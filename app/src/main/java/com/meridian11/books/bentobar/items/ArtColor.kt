package com.meridian11.books.bentobar.items

/**
 * The color of a cover, for Now playing's "Color from the album art": the hue most of the picture's color lies in
 * ([of]), then made light or dark enough to be seen on the bar or the popup ([forBackground]). Pure arithmetic on
 * ARGB values (no Android types), so it is unit-tested; the pixels come from a small copy of the cover.
 */
object ArtColor {
    private const val BINS = 12
    /** Colors this grey or this dark say nothing about a cover's hue. */
    private const val MIN_SATURATION = 0.18f
    private const val MIN_VALUE = 0.15f

    /**
     * The color [pixels] (ARGB) are mostly of: the hue with the most weight (a pixel weighs its saturation and its
     * strength, and nothing if it is see-through), as the average of that hue's pixels. Null when nothing has a
     * color to speak of (a grey or black-and-white cover): the caller then keeps its own.
     */
    fun of(pixels: IntArray): Int? {
        val weight = FloatArray(BINS)
        val r = FloatArray(BINS)
        val g = FloatArray(BINS)
        val b = FloatArray(BINS)
        for (p in pixels) {
            val a = (p ushr 24) and 0xFF
            if (a < 128) continue
            val red = (p shr 16) and 0xFF
            val green = (p shr 8) and 0xFF
            val blue = p and 0xFF
            val hsv = hsv(red, green, blue)
            if (hsv[1] < MIN_SATURATION || hsv[2] < MIN_VALUE) continue
            val bin = ((hsv[0] / 360f) * BINS).toInt().coerceIn(0, BINS - 1)
            val w = hsv[1] * hsv[2]
            weight[bin] += w
            r[bin] += red * w
            g[bin] += green * w
            b[bin] += blue * w
        }
        val best = weight.indices.maxByOrNull { weight[it] } ?: return null
        if (weight[best] <= 0f) return null
        val w = weight[best]
        return (0xFF shl 24) or ((r[best] / w).toInt().coerceIn(0, 255) shl 16) or ((g[best] / w).toInt().coerceIn(0, 255) shl 8) or (b[best] / w).toInt().coerceIn(0, 255)
    }

    /**
     * [argb] with its lightness moved until it reads on a [dark] or a light background: on dark, at least 0.45 relative
     * luminance (the color goes lighter, its hue kept); on light, at most 0.2 (it goes darker). A color that already
     * does is left as it is.
     */
    fun forBackground(argb: Int, dark: Boolean): Int {
        var c = argb or (0xFF shl 24)
        val hsv = hsv((c shr 16) and 0xFF, (c shr 8) and 0xFF, c and 0xFF)
        var v = hsv[2]
        var s = hsv[1]
        var steps = 0
        while (steps < 40) {
            val l = luminance(c)
            if (if (dark) l >= 0.45f else l <= 0.2f) break
            if (dark) { v = (v + 0.03f).coerceAtMost(1f); if (v >= 1f) s = (s - 0.04f).coerceAtLeast(0f) } else v = (v - 0.03f).coerceAtLeast(0f)
            c = rgb(hsv[0], s, v)
            steps++
        }
        return c
    }

    /** A tone of [argb]'s hue: its saturation held to [maxSaturation], at value [v]. */
    private fun tone(argb: Int, maxSaturation: Float, v: Float): Int {
        val h = hsv((argb shr 16) and 0xFF, (argb shr 8) and 0xFF, argb and 0xFF)
        return rgb(h[0], minOf(h[1], maxSaturation), v)
    }

    /** The cover's hue as a card's surface under the glass: deep on a dark theme, pale on a light one. */
    fun surface(argb: Int, dark: Boolean): Int = if (dark) tone(argb, 0.55f, 0.20f) else tone(argb, 0.22f, 0.97f)

    /** The cover's hue as the color of the buttons' and the tile's backgrounds: a step from [surface]. */
    fun container(argb: Int, dark: Boolean): Int = if (dark) tone(argb, 0.55f, 0.34f) else tone(argb, 0.32f, 0.92f)

    /** The cover's hue as the unfilled part of a bar: between the container and the accent. */
    fun track(argb: Int, dark: Boolean): Int = if (dark) tone(argb, 0.45f, 0.44f) else tone(argb, 0.38f, 0.80f)

    /** WCAG relative luminance of an ARGB color. */
    fun luminance(argb: Int): Float {
        fun lin(x: Int): Float { val s = x / 255f; return if (s <= 0.03928f) s / 12.92f else Math.pow(((s + 0.055f) / 1.055f).toDouble(), 2.4).toFloat() }
        return 0.2126f * lin((argb shr 16) and 0xFF) + 0.7152f * lin((argb shr 8) and 0xFF) + 0.0722f * lin(argb and 0xFF)
    }

    /** Hue (0..360), saturation and value (0..1). */
    private fun hsv(r: Int, g: Int, b: Int): FloatArray {
        val rf = r / 255f; val gf = g / 255f; val bf = b / 255f
        val max = maxOf(rf, gf, bf)
        val min = minOf(rf, gf, bf)
        val d = max - min
        val h = when {
            d == 0f -> 0f
            max == rf -> 60f * (((gf - bf) / d) % 6f)
            max == gf -> 60f * ((bf - rf) / d + 2f)
            else -> 60f * ((rf - gf) / d + 4f)
        }
        return floatArrayOf(if (h < 0) h + 360f else h, if (max == 0f) 0f else d / max, max)
    }

    private fun rgb(h: Float, s: Float, v: Float): Int {
        val c = v * s
        val x = c * (1 - Math.abs((h / 60f) % 2 - 1))
        val m = v - c
        val (r, g, b) = when {
            h < 60 -> Triple(c, x, 0f)
            h < 120 -> Triple(x, c, 0f)
            h < 180 -> Triple(0f, c, x)
            h < 240 -> Triple(0f, x, c)
            h < 300 -> Triple(x, 0f, c)
            else -> Triple(c, 0f, x)
        }
        fun ch(f: Float) = ((f + m) * 255f + 0.5f).toInt().coerceIn(0, 255)
        return (0xFF shl 24) or (ch(r) shl 16) or (ch(g) shl 8) or ch(b)
    }
}
