package com.meridian11.books.bentobar.data

import kotlinx.serialization.Serializable

/** Where an item lives: in the bar, hidden behind ‹, or off. */
@Serializable
enum class Section { SHOWN, HIDDEN, OFF }

/** How an item draws in the bar. */
@Serializable
enum class Display {
    ICON_AND_TEXT, TEXT, ICON;

    /** A route line stands in the icon's place: an item shown as text alone has none. The item asks for one by this, and the strip draws one by it. */
    val line: Boolean get() = this != TEXT
}

/** Where BentoBar's strip sits in the status bar's free space. */
@Serializable
enum class Position { RIGHT, CENTER, LEFT }

@Serializable
enum class ColorMode { AUTO, LIGHT, DARK }

@Serializable
enum class TextSize { SMALL, DEFAULT, LARGE }

@Serializable
enum class Pill { NONE, SUBTLE, SOLID }

/** When the running timer or next meeting also shows as an Android Live Update chip. */
@Serializable
enum class ChipMode { OFF, FALLBACK, ALWAYS }

/** One item the user added. [id] is unique per instance; [type] picks the [ItemType]. */
@Serializable
@androidx.compose.runtime.Immutable
data class ItemConfig(
    val id: String,
    val type: String,
    val section: Section = Section.SHOWN,
    /** Hidden items with this on pop into the bar while they have something to say. */
    val whenActive: Boolean = false,
    val display: Display = Display.ICON_AND_TEXT,
    val options: Map<String, String> = emptyMap(),
    /** Where the item lives while Desktop Mode is active: [DesktopWhere.SAME] (as [section]), or a section of its own. */
    val desktop: DesktopWhere = DesktopWhere.SAME,
) {
    fun opt(key: String, default: String) = options[key] ?: default
    fun optInt(key: String, default: Int) = options[key]?.toIntOrNull() ?: default
    fun optBool(key: String, default: Boolean) = options[key]?.toBooleanStrictOrNull() ?: default
    fun with(key: String, value: String?) =
        copy(options = if (value == null) options - key else options + (key to value))
}

/**
 * These items with [id] moved into [section] at [index] among that section's other items (the end
 * if out of range). Unchanged if [id] isn't there. [Store.move] and the drag preview both use it.
 */
fun List<ItemConfig>.moved(id: String, section: Section, index: Int, desktop: Boolean = false): List<ItemConfig> {
    val item = firstOrNull { it.id == id } ?: return this
    val rest = filterNot { it.id == id }
    val inSection = rest.withIndex().filter { it.value.cardIn(desktop) == section }
    val at = when {
        inSection.isEmpty() -> rest.size
        index >= inSection.size -> inSection.last().index + 1
        else -> inSection[index.coerceAtLeast(0)].index
    }
    return rest.toMutableList().apply { add(at, item.droppedOn(section, desktop)) }
}

/**
 * A city added to World clock's list: its time zone (an IANA id such as "Asia/Tokyo") and the name
 * the user gave it; without one it is called by the zone's own city.
 */
@Serializable
@androidx.compose.runtime.Immutable
data class WorldCity(val zone: String, val name: String = "")

@Serializable
data class BarConfig(
    val version: Int = 3,
    /** Master switch (the Quick Settings tile flips it, e.g. for presenting). */
    val enabled: Boolean = true,
    val items: List<ItemConfig> = emptyList(),
    val position: Position = Position.RIGHT,
    /** How the Hidden section works (version 3). */
    val hiddenMode: HiddenMode = HiddenMode.SHOW_ALL,
    /** Before version 3: show the ‹ button. Read only by [migrateToV3]. */
    val chevron: Boolean = true,
    /** Before version 3: reveal hidden items on hover. Read only by [migrateToV3]. */
    val revealOnHover: Boolean = false,
    /** Hide revealed items again after this many seconds; 0 (default) keeps them until ‹ is clicked. */
    val autoCollapseSec: Int = 0,
    /** Hidden items pinned open with ‹, kept across restarts (an update, a reboot). */
    val pinnedOpen: Boolean = false,
    /** Presenting: only a running timer or a meeting about to start, plus ‹ for the menu. */
    val presenting: Boolean = false,
    /** Permissions the user switched off in Setup ([Uses] keys): BentoBar doesn't use them even if granted. */
    val turnedOff: Set<String> = emptySet(),
    val textSize: TextSize = TextSize.DEFAULT,
    val pill: Pill = Pill.NONE,
    val color: ColorMode = ColorMode.AUTO,
    /** Space between items in dp (12 matches the system icons' rhythm on the desktop bar). */
    val spacing: Int = 12,
    /** With a popup open, pointing at another item opens its popup, as in a menu bar. Off: nothing opens on hover by itself. */
    val hoverSwitchesPopups: Boolean = false,
    /** "No animations": BentoBar's popups, highlight and items appear and go at once (`ui/Motion`). */
    val noAnimations: Boolean = false,
    /** "Tint with system colors": the popups' glass takes a tint of the system's (wallpaper's) palette. Off: neutral glass. */
    val systemTint: Boolean = false,
    /** The progress bars (except the volume one) are drawn in Material 3's wavy style. */
    val wavyBars: Boolean = false,
    /** How solid the popups are, in percent: 50 is the look they were made with, 100 a solid card, less lets more of the screen show through. */
    val popupOpaque: Int = DEFAULT_POPUP_OPAQUE,
    /** Where ‹ sits when the strip is centred (at the left or right end it stands at the end next to the screen's middle). */
    val chevronCenter: ChevronSide = ChevronSide.LEFT,
    /** Pressing ‹ puts the hidden items where they belong in the layout, instead of unfolding them on the side ‹ is on. */
    val revealInPlace: Boolean = false,
    /** The mouse wheel over the volume or Now playing item changes its level or place. Off: only a click or a drag does. */
    val wheelAdjusts: Boolean = false,
    /**
     * Desktop Mode: with no status bar at all (a tablet's desktop windowing keeps its clock and icons in a taskbar),
     * float along the top of the screen instead of going away. Full-screen apps still hide it. Off by default: where
     * a device keeps a taskbar while an app is full screen, the strip would stay over that app, as it never did.
     * (The name is from before it was called Desktop Mode: saved layouts hold it.)
     */
    val floatWithoutBar: Boolean = false,
    /** Desktop Mode has a layout of its own (alignment, distance from the top, each item's place). Off: it is the regular one. */
    val desktopSeparate: Boolean = false,
    /** Desktop Mode: dp kept clear at the left end of a window's caption (its icon and name). */
    val floatTitleDp: Int = 200,
    /** Desktop Mode: dp kept clear at the right end of a window's caption (its buttons, with room for one more). */
    val floatControlsDp: Int = 220,
    /** Desktop Mode: where the strip sits, or [DesktopAlign.SAME]: as in [position]. */
    val desktopAlign: DesktopAlign = DesktopAlign.SAME,
    /**
     * Desktop Mode: the strip stays where its alignment puts it and lies over a window's icon and name when they are
     * there (never over its buttons). Off: it keeps clear of [floatTitleDp] at a window's left too.
     */
    val desktopOverlap: Boolean = true,
    /** Desktop Mode: dp between the screen's left or right edge and the strip (its pill, or its items without one) when it sits at an edge. */
    val desktopEdgeDp: Int = 0,
    /** Desktop Mode: while a window is maximized (no caption of its own to sit in, an app's own top bar lies under the strip) the strip goes into presenting mode. */
    val desktopHideMaximized: Boolean = false,
    /** How far below the top of the screen the strip sits, in dp. */
    val offsetYDp: Int = 0,
    /** The same, while Desktop Mode is active. */
    val desktopOffsetYDp: Int = 0,
    /**
     * Mirror the running timer or next meeting as an Android Live Update chip: FALLBACK only
     * while BentoBar's own bar isn't showing (accessibility off, or hidden with the tile).
     */
    val chipMode: ChipMode = ChipMode.FALLBACK,
    val onboarded: Boolean = false,
    /**
     * The cities added to World clock: one list for the bar, shown in every clock item's menu, and
     * part of the layout (Copy settings carries it). Layouts saved before it existed have none.
     */
    val cities: List<WorldCity> = emptyList(),
)

/** This config with the settings that belong to one install taken from [local] (see [Store.import]). */
fun BarConfig.keepingLocal(local: BarConfig): BarConfig = copy(enabled = local.enabled, turnedOff = local.turnedOff,
    presenting = local.presenting, pinnedOpen = local.pinnedOpen, onboarded = local.onboarded)

/**
 * What BentoBar uses, as switched in Setup: a permission counts only if Android granted it and the
 * user hasn't switched it off here.
 */
object Uses {
    const val NOTIFICATIONS = "notifications"
    const val CALENDAR = "calendar"
    const val EXACT_ALARMS = "exactAlarms"

    fun on(key: String) = key !in Store.config.value.turnedOff
}

/**
 * How hidden items work. SHOW_ALL (the default): no ‹; an item in Hidden with a "Show when" rule
 * appears only while it applies, the others always show. CLICK and HOVER: hidden items wait behind
 * ‹ and come out on a click, or on hover too.
 */
@Serializable
enum class HiddenMode { SHOW_ALL, CLICK, HOVER }

/** An item's own place while Desktop Mode is active. */
@Serializable
enum class DesktopWhere { SAME, SHOWN, HIDDEN, OFF }

/** The section this item is in as the Bar page shows it: its Desktop Mode place in the Desktop Mode view, else [ItemConfig.section]. */
fun ItemConfig.sectionIn(desktop: Boolean): Section = if (!desktop) section else when (this.desktop) {
    DesktopWhere.SAME -> section; DesktopWhere.SHOWN -> Section.SHOWN; DesktopWhere.HIDDEN -> Section.HIDDEN; DesktopWhere.OFF -> Section.OFF
}

/** An item with a "Show when" rule in Hidden: it comes into the bar by itself, so the Bar page lists it among the items in the bar. */
fun ItemConfig.popsUpIn(desktop: Boolean): Boolean = whenActive && sectionIn(desktop) == Section.HIDDEN

/** The Bar page card this item is listed in: a pop-up item is in the bar's, where its place among the others is set. */
fun ItemConfig.cardIn(desktop: Boolean): Section = if (popsUpIn(desktop)) Section.SHOWN else sectionIn(desktop)

/**
 * This item after it is dropped on [card]: a pop-up item dropped in the bar's card only changes place and stays a pop-up;
 * dropped on Hidden it is no pop-up any more (its rule is cleared, it waits behind ‹); anything else is [placedIn].
 */
fun ItemConfig.droppedOn(card: Section, desktop: Boolean): ItemConfig = when {
    popsUpIn(desktop) && card == Section.SHOWN -> this
    popsUpIn(desktop) && card == Section.HIDDEN -> copy(whenActive = false)
    else -> placedIn(card, desktop)
}

/** This item put in [section] as that view shows it: the Desktop Mode view sets its Desktop Mode place (back to Same as usual when that is its usual one). */
fun ItemConfig.placedIn(section: Section, desktop: Boolean): ItemConfig = if (!desktop) copy(section = section) else copy(desktop =
    if (section == this.section) DesktopWhere.SAME else when (section) { Section.SHOWN -> DesktopWhere.SHOWN; Section.HIDDEN -> DesktopWhere.HIDDEN; Section.OFF -> DesktopWhere.OFF })

/** The section this item is in right now: its own while Desktop Mode is active and it has one, else [ItemConfig.section]. */
val ItemConfig.sectionNow: Section get() = when {
    !DesktopMode.active.value -> section
    else -> when (desktop) { DesktopWhere.SAME -> section; DesktopWhere.SHOWN -> Section.SHOWN; DesktopWhere.HIDDEN -> Section.HIDDEN; DesktopWhere.OFF -> Section.OFF }
}

/** Where ‹ stands when the strip is centred. */
@Serializable
enum class ChevronSide { LEFT, RIGHT }

/** Whether ‹ stands at the strip's left end, for a strip placed at [position]. */
fun BarConfig.chevronOnLeft(position: Position): Boolean = when (position) {
    Position.LEFT -> false
    Position.RIGHT -> true
    Position.CENTER -> chevronCenter == ChevronSide.LEFT
}

/** Where the strip sits while Desktop Mode is active: as usual, or one of the three places. */
@Serializable
enum class DesktopAlign {
    SAME, RIGHT, CENTER, LEFT;
    fun resolve(regular: Position): Position = when (this) { SAME -> regular; RIGHT -> Position.RIGHT; CENTER -> Position.CENTER; LEFT -> Position.LEFT }
}

/**
 * Whether the strip is floating with no status bar right now: the Desktop Mode settings apply only then.
 * Set by the controller as it looks at the windows; read through [sectionNow] and [positionNow] so that the
 * strip, the ticker and the menus all follow one rule.
 */
object DesktopMode {
    /** Floating, with a layout of its own ([BarConfig.desktopSeparate]): what [sectionNow], [positionNow] and [offsetYNowDp] follow. */
    val active = kotlinx.coroutines.flow.MutableStateFlow(false)
    /** A window is maximized while floating, and the user asked for presenting mode then ([BarConfig.desktopHideMaximized]). */
    val maximized = kotlinx.coroutines.flow.MutableStateFlow(false)
}

/** Where the strip sits right now: [BarConfig.desktopAlign] while Desktop Mode is active, else [BarConfig.position]. */
val BarConfig.positionNow: Position get() = if (DesktopMode.active.value) desktopAlign.resolve(position) else position

/** How far below the top of the screen the strip sits right now, in dp. */
val BarConfig.offsetYNowDp: Int get() = (if (DesktopMode.active.value) desktopOffsetYDp else offsetYDp).coerceIn(0, MAX_OFFSET_Y_DP)

/** The least solid the popups can be, in percent: below this, text on them stops being readable. */
const val MIN_POPUP_OPAQUE = 20

/** The popups' opacity as they were made (the first setting's 50%). */
const val DEFAULT_POPUP_OPAQUE = 50

/**
 * The alpha of a popup's veil that is [base] at the standard look, at [percent] solid: 50 is [base] itself, less fades it
 * toward nothing, more takes it toward a fully solid card at 100.
 */
fun popupAlpha(base: Float, percent: Int): Float {
    val p = percent.coerceIn(MIN_POPUP_OPAQUE, 100)
    return if (p <= DEFAULT_POPUP_OPAQUE) base * p / DEFAULT_POPUP_OPAQUE
    else base + (1f - base) * (p - DEFAULT_POPUP_OPAQUE) / (100 - DEFAULT_POPUP_OPAQUE)
}

/** The most the strip can be moved down, in dp. */
const val MAX_OFFSET_Y_DP = 64

/** The most room that can be kept between the strip and the screen's side in Desktop Mode, in dp. */
const val MAX_EDGE_DP = 64

/**
 * Version 3 replaced the ‹ switch and "reveal on hover" with [HiddenMode]. With ‹ off, a hidden
 * item without a rule never showed; under SHOW_ALL it would, so those move to Off and the bar looks
 * the same as before.
 */
fun BarConfig.migrateToV3(): BarConfig = if (version >= 3) this else copy(
    version = 3,
    hiddenMode = when { !chevron -> HiddenMode.SHOW_ALL; revealOnHover -> HiddenMode.HOVER; else -> HiddenMode.CLICK },
    items = if (chevron) items else items.map { if (it.section == Section.HIDDEN && !it.whenActive) it.copy(section = Section.OFF) else it },
)

/** Whether an item in the bar config is drawn right now (outside presenting), for the strip and the preview. */
fun BarConfig.shows(item: ItemConfig, active: Boolean): Boolean = when (item.sectionNow) {
    Section.SHOWN -> true
    Section.HIDDEN -> if (hiddenMode == HiddenMode.SHOW_ALL) !item.whenActive || active else item.whenActive && active
    Section.OFF -> false
}

/**
 * The items waiting behind ‹ (the click and hover modes; Show everything has no ‹ for them): hidden
 * ones that aren't out on their own. The strip, the settings preview and the controller all ask here.
 */
fun BarConfig.behindChevron(active: (ItemConfig) -> Boolean): List<ItemConfig> =
    if (hiddenMode == HiddenMode.SHOW_ALL) emptyList() else items.filter { it.sectionNow == Section.HIDDEN && !shows(it, active(it)) }

/**
 * The items the ‹ menu lists as hidden: in the layout but not drawn right now. Under Show everything
 * that's a rule item waiting for its rule, never a hidden item that is drawn.
 */
fun BarConfig.notDrawn(active: (ItemConfig) -> Boolean): List<ItemConfig> =
    items.filter { it.sectionNow != Section.OFF && !shows(it, active(it)) }

/** Whether [shows] can be true for [item] once its "Show when" rule applies: the items worth sampling. */
fun BarConfig.couldShow(item: ItemConfig): Boolean = shows(item, active = true)
