package com.meridian11.books.bentobar.items

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class MediaOptionsTest {
    @Test fun albumArtAndTheWaveAreOffUntilChosen() {
        assertFalse(MediaText.art(emptyMap()))
        assertFalse(MediaText.progress(emptyMap()))
        assertFalse(MediaText.artColor(emptyMap()))
        assertTrue(MediaText.art(mapOf(MediaText.OPTION_ART to "true")))
        assertTrue(MediaText.progress(mapOf(MediaText.OPTION_PROGRESS to "true")))
        assertTrue(MediaText.artColor(mapOf(MediaText.OPTION_ART_COLOR to "true")))
        assertFalse(MediaText.art(mapOf(MediaText.OPTION_ART to "false")))
    }
}
