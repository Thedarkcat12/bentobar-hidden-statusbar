package com.meridian11.books.bentobar.items

import org.junit.Assert.assertEquals
import org.junit.Test

class BrightnessRulesTest {
    @Test fun valuesAreShownAsPercentages() {
        assertEquals(0, BrightnessRules.percent(0))
        assertEquals(50, BrightnessRules.percent(128))
        assertEquals(100, BrightnessRules.percent(255))
        assertEquals(100, BrightnessRules.percent(900))
        assertEquals(0, BrightnessRules.percent(-4))
    }

    @Test fun aSliderNeverSetsTheScreenDark() {
        assertEquals(1, BrightnessRules.value(0f))
        assertEquals(1, BrightnessRules.value(-1f))
        assertEquals(255, BrightnessRules.value(1f))
        assertEquals(255, BrightnessRules.value(7f))
        assertEquals(128, BrightnessRules.value(0.5f))
    }

    @Test fun theLevelFollowsTheValue() {
        assertEquals(0f, BrightnessRules.level(0), 0f)
        assertEquals(1f, BrightnessRules.level(255), 0f)
        assertEquals(0.5f, BrightnessRules.level(128), 0.005f)
    }

    @Test fun aNotchOfTheWheelIsAboutFivePercentAndStaysInRange() {
        assertEquals(128 + 13, BrightnessRules.wheel(128, 1))
        assertEquals(128 - 26, BrightnessRules.wheel(128, -2))
        assertEquals(255, BrightnessRules.wheel(250, 3))
        assertEquals(1, BrightnessRules.wheel(5, -3))
    }
}
