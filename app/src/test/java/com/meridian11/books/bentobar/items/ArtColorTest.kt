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
}
