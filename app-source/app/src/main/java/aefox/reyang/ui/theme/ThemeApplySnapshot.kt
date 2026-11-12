package aefox.reyang.ui.theme

import android.app.Activity
import android.content.Context
import aefox.reyang.ui.theme.themepackage.ThemePrefKeys

/**
 * Settings keys that cannot take effect through recomposition alone. A change
 * to any of them must rebuild the Activity window: the UI mode drives the
 * window/decor setup and the predictive-back flag is registered per process.
 * Every other theme key (colors, wallpaper, custom texts, appearance) is read
 * from the theme snapshot and refreshes in place.
 */
private val THEME_RECREATE_REQUIRED_KEYS: Set<String> = setOf(
    ThemePrefKeys.UI_MODE,
    ThemePrefKeys.ENABLE_PREDICTIVE_BACK,
)

/**
 * Snapshot of the recreate-relevant settings taken before a theme apply, so the
 * caller can decide afterwards whether the window has to be rebuilt.
 */
class ThemeApplySnapshot private constructor(
    private val values: Map<String, Any?>,
) {
    /** True when a recreate-required key differs from the captured state. */
    fun requiresRecreate(context: Context): Boolean {
        val current = context.getSharedPreferences("settings", Context.MODE_PRIVATE).all
        return THEME_RECREATE_REQUIRED_KEYS.any { key -> current[key] != values[key] }
    }

    companion object {
        fun capture(context: Context): ThemeApplySnapshot {
            val current = context.getSharedPreferences("settings", Context.MODE_PRIVATE).all
            return ThemeApplySnapshot(
                THEME_RECREATE_REQUIRED_KEYS.associateWith { key -> current[key] },
            )
        }
    }
}

/**
 * Theme apply effect policy: recomposition covers colors, wallpaper and texts;
 * a changed recreate-required key rebuilds the hosting Activity immediately so
 * the apply never stays half-applied.
 */
fun recreateActivityIfThemeRequires(
    activity: Activity?,
    snapshot: ThemeApplySnapshot?,
    context: Context,
) {
    if (activity == null || snapshot == null) return
    if (activity.isFinishing || activity.isDestroyed) return
    if (!snapshot.requiresRecreate(context)) return
    activity.recreate()
}
