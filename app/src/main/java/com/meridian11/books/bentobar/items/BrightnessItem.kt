package com.meridian11.books.bentobar.items

import android.content.Intent
import android.net.Uri
import android.provider.Settings
import android.util.Log
import androidx.compose.material3.Slider
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import com.meridian11.books.bentobar.R
import com.meridian11.books.bentobar.data.Display
import com.meridian11.books.bentobar.data.ItemConfig
import com.meridian11.books.bentobar.ui.MenuCard
import com.meridian11.books.bentobar.ui.MenuEntry
import com.meridian11.books.bentobar.ui.SwitchRow
import com.meridian11.books.bentobar.ui.rememberTick
import com.meridian11.books.bentobar.util.Sym

/**
 * The Brightness item: the screen's brightness as an icon and a number; with "Slider in the bar" on, a slider in the number's
 * place, as the Sound item has for the volume (the strip draws it and takes the pointer; the item says how full it is and sets
 * what it is dragged to, [onSlide]), and the wheel over it steps by [BrightnessRules.WHEEL_PERCENT] when that is switched on.
 * Android lets an app set the brightness only once the user has allowed "Modify system settings" for it, which the menu offers
 * and a click on the slider asks for; until then the item only shows the number.
 */
object BrightnessItem : ItemType("brightness", R.string.item_brightness_title, Sym.LIGHT_MODE, R.string.item_brightness_desc) {
    private fun canWrite(): Boolean = runCatching { Settings.System.canWrite(Env.app) }.getOrDefault(false)

    private fun value(): Int =
        runCatching { Settings.System.getInt(Env.app.contentResolver, Settings.System.SCREEN_BRIGHTNESS) }.getOrDefault(128).coerceIn(0, BrightnessRules.MAX)

    private fun set(value: Int) {
        try {
            Settings.System.putInt(Env.app.contentResolver, Settings.System.SCREEN_BRIGHTNESS, value)
        } catch (e: Exception) {
            Log.w(Env.TAG, "can't set the brightness: ${e.javaClass.simpleName}")
        }
    }

    /** Opens Android's switch for "Modify system settings" for BentoBar. */
    private fun askForAccess() {
        Env.launch(Intent(Settings.ACTION_MANAGE_WRITE_SETTINGS, Uri.parse("package:" + Env.app.packageName)))
    }

    override fun state(item: ItemConfig): ItemState {
        val v = value()
        val pct = BrightnessRules.percent(v)
        // A slider only where it could be set; without the permission the number is shown and the menu says why.
        val slider = if (item.optBool("slider", false) && canWrite()) BarSlider(level = BrightnessRules.level(v)) else null
        return ItemState(icon = Sym.LIGHT_MODE, text = "$pct%", widthKey = "pct", desc = Env.str(R.string.brightness_desc, pct), slider = slider,
            tooltip = if (slider == null || item.display == Display.ICON) null else Env.str(R.string.brightness_tooltip, pct))
    }

    override val usesWheel = true
    override fun onScroll(item: ItemConfig, steps: Int) {
        if (canWrite()) set(BrightnessRules.wheel(value(), steps))
    }

    override fun onSlide(item: ItemConfig, level: Float, done: Boolean) {
        if (!canWrite()) { if (done) askForAccess(); return }
        set(BrightnessRules.value(level))
    }

    override val menu: @Composable (ItemConfig, MenuHost) -> Unit = { _, host ->
        rememberTick()
        val actual = value()
        var dragging by remember { mutableStateOf(false) }
        var v by remember { mutableFloatStateOf(actual.toFloat()) }
        LaunchedEffect(actual, dragging) { if (!dragging) v = actual.toFloat() }
        val label = stringResource(R.string.brightness_label)
        MenuCard(Sym.LIGHT_MODE, stringResource(R.string.item_brightness_title), stringResource(R.string.brightness_pct, BrightnessRules.percent(Math.round(v)))) {
            if (canWrite()) {
                Slider(value = v, onValueChange = { dragging = true; v = it; set(Math.round(it).coerceIn(BrightnessRules.MIN_SET, BrightnessRules.MAX)) },
                    onValueChangeFinished = { dragging = false }, valueRange = BrightnessRules.MIN_SET.toFloat()..BrightnessRules.MAX.toFloat(),
                    modifier = Modifier.semantics { contentDescription = label })
            } else MenuEntry(Sym.SETTINGS, stringResource(R.string.brightness_allow)) { host.close(); askForAccess() }
            MenuEntry(Sym.TUNE, stringResource(R.string.brightness_display_settings)) { host.close(); Env.launch(Intent(Settings.ACTION_DISPLAY_SETTINGS)) }
        }
    }

    override val options: @Composable (ItemConfig, (ItemConfig) -> Unit) -> Unit = { item, set ->
        rememberTick() // the help follows the permission while settings are open
        SwitchRow(stringResource(R.string.brightness_slider), item.optBool("slider", false),
            help = stringResource(if (canWrite()) R.string.brightness_slider_help else R.string.brightness_slider_needs_access)) { set(item.with("slider", it.toString())) }
    }
}
