package com.meridian11.books.bentobar.items

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaOptionsTest {
    @Test fun albumArtAndTheWaveAreOffUntilChosen() {
        assertFalse(MediaText.art(emptyMap()))
        assertFalse(MediaText.wave(emptyMap()))
        assertTrue(MediaText.art(mapOf(MediaText.OPTION_ART to "true")))
        assertTrue(MediaText.wave(mapOf(MediaText.OPTION_WAVE to "true")))
        assertFalse(MediaText.art(mapOf(MediaText.OPTION_ART to "false")))
    }
}
