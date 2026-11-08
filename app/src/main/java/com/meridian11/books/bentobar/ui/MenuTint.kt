package com.meridian11.books.bentobar.ui

import kotlinx.coroutines.flow.MutableStateFlow

/**
 * A color (ARGB) the open popup lends the card's glass, or null for none: Now playing's "Color from the album art". The
 * popup's content sets it while it is open and clears it as it closes; [MenuSurface] mixes a little of it into the veil.
 */
object MenuTint {
    val color = MutableStateFlow<Int?>(null)
}
