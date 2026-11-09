package com.meridian11.books.bentobar.items

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotNull
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class ArtColorTest {
    private val red = 0xFFD02020.toInt()
    private val blue = 0xFF2040C0.toInt()
    private val grey = 0xFF808080.toInt()

    @Test fun aCoverMostlyOfOneHueGivesThatHue() {
        val c = ArtColor.of(IntArray(100) { if (it < 70) red else blue })!!
        assertTrue(((c shr 16) and 0xFF) in 0xCE..0xD0)
        assertTrue(((c and 0xFF) < 0x40))
    }

    @Test fun aGreyCoverHasNoColor() {
        assertNull(ArtColor.of(IntArray(50) { grey }))
        assertNull(ArtColor.of(IntArray(50) { 0xFF000000.toInt() }))
        assertNull(ArtColor.of(IntArray(0)))
    }

    @Test fun seeThroughPixelsDoNotCount() {
        assertNull(ArtColor.of(IntArray(50) { 0x00FF0000 }))
    }

    @Test fun aDarkBackgroundGetsALighterColorOfTheSameHue() {
        val dark = 0xFF101040.toInt()
        val c = ArtColor.forBackground(dark, dark = true)
        assertTrue(ArtColor.luminance(c) >= 0.45f)
        assertNotNull(c)
        // Blue stays the strongest channel.
        assertTrue((c and 0xFF) >= ((c shr 16) and 0xFF))
    }

    @Test fun aLightBackgroundGetsADarkerColor() {
        val pale = 0xFFFFE08A.toInt()
        assertTrue(ArtColor.luminance(ArtColor.forBackground(pale, dark = false)) <= 0.2f)
    }

    @Test fun aColorThatAlreadyReadsIsLeftAlone() {
        val c = 0xFFFFA0A0.toInt()
        assertEquals(c, ArtColor.forBackground(c, dark = true))
    }

    @Test fun theCardTonesAreDeepOnDarkAndPaleOnLightAndKeepTheHue() {
        for (art in listOf(red, blue, 0xFFE0A030.toInt())) {
            assertTrue(ArtColor.luminance(ArtColor.surface(art, dark = true)) < 0.08f)
            assertTrue(ArtColor.luminance(ArtColor.container(art, dark = true)) < 0.2f)
            assertTrue(ArtColor.luminance(ArtColor.surface(art, dark = false)) > 0.6f)
            assertTrue(ArtColor.luminance(ArtColor.container(art, dark = false)) > 0.4f)
        }
        // Red stays the strongest channel in every tone of a red cover.
        for (c in listOf(ArtColor.surface(red, true), ArtColor.container(red, true), ArtColor.track(red, true), ArtColor.container(red, false))) {
            val r = (c shr 16) and 0xFF
            assertTrue(r >= ((c shr 8) and 0xFF) && r >= (c and 0xFF))
        }
    }

    @Test fun theTrackLiesBetweenTheContainerAndAnAccentOnDark() {
        val l = { c: Int -> ArtColor.luminance(c) }
        assertTrue(l(ArtColor.track(blue, true)) > l(ArtColor.container(blue, true)))
        assertTrue(l(ArtColor.forBackground(blue, true)) > l(ArtColor.track(blue, true)))
    }
}
