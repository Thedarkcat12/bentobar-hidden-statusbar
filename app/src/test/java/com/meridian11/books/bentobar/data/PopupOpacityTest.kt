package com.meridian11.books.bentobar.data

import org.junit.Assert.assertEquals
import org.junit.Test

class PopupOpacityTest {
    @Test fun fiftyIsTheLookThePopupsWereMadeWith() {
        assertEquals(0.56f, popupAlpha(0.56f, 50), 1e-6f)
        assertEquals(1f, popupAlpha(1f, 50), 1e-6f)
    }

    @Test fun aHundredIsSolid() {
        assertEquals(1f, popupAlpha(0.56f, 100), 1e-6f)
        assertEquals(1f, popupAlpha(0.66f, 100), 1e-6f)
    }

    @Test fun inBetweenIsInBetween() {
        assertEquals(0.78f, popupAlpha(0.56f, 75), 1e-6f)
        assertEquals(0.28f, popupAlpha(0.56f, 25), 1e-6f)
    }

    @Test fun outOfRangeIsKeptInRange() {
        assertEquals(popupAlpha(0.56f, MIN_POPUP_OPAQUE), popupAlpha(0.56f, 0), 1e-6f)
        assertEquals(1f, popupAlpha(0.56f, 400), 1e-6f)
    }
}
