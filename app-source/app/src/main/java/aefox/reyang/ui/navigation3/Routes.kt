package aefox.reyang.ui.navigation3

import android.os.Parcelable
import top.yukonga.miuix.kmp.nav.core.NavKey
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable

// Type-safe navigation keys for miuix-nav.
// Each destination is a NavKey (data object/data class) and can be saved/restored in the back stack.
@Serializable
sealed interface Route : NavKey, Parcelable {
    @Parcelize
    @Serializable
    data object Home : Route

    @Parcelize
    @Serializable
    data object Settings : Route

    @Parcelize
    @Serializable
    data object About : Route

    @Parcelize
    @Serializable
    data object ColorPalette : Route

    // Standalone custom color / image effect editor (opened from the COLORS category).
    @Parcelize
    @Serializable
    data object AppearanceOverlay : Route

    // Full-page interface text editor.
    @Parcelize
    @Serializable
    data object CustomTexts : Route

    // Launcher icon picker: built-in aliases and imported icon packs.
    @Parcelize
    @Serializable
    data object AppIcon : Route

    // Background crop for a staged pick (preview + ratio/rotation before applying).
    @Parcelize
    @Serializable
    data class BackgroundCrop(val sourcePath: String) : Route
}
