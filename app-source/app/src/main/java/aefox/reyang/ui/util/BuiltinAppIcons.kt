package aefox.reyang.ui.util

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.compose.runtime.Immutable
import aefox.reyang.MainActivity
import aefox.reyang.R
import aefox.reyang.core.AeFoxLog

/**
 * One built-in launcher icon.
 *
 * Each icon owns two activity-alias components ([aliasClassName] / [aliasClassName] + "Static"):
 * the static alias is shown when dynamic color is off, the other one carries a monochrome layer
 * that Android 13+ launchers tint with wallpaper colors.
 */
@Immutable
data class BuiltinAppIcon(
    val id: String,
    @param:StringRes val nameRes: Int,
    @param:DrawableRes val iconRes: Int,
    @param:DrawableRes val foregroundRes: Int,
)

/**
 * Built-in launcher icons and the component switching that applies them.
 *
 * The manifest declares one activity-alias per icon and variant. Only the enabled alias carries
 * the MAIN/LAUNCHER filter, so exactly one launcher entry exists at any time. Switching must not
 * touch components when the state already matches: on some ROMs re-applying an alias restarts the
 * process, which used to kill the first launch after an update.
 */
object BuiltinAppIcons {
    const val DEFAULT_ID = "default"

    private const val TAG = "BuiltinAppIcons"
    private val mainActivityName = MainActivity::class.java.name

    val all: List<BuiltinAppIcon> = listOf(
        BuiltinAppIcon(
            id = DEFAULT_ID,
            nameRes = R.string.app_icon_builtin_default,
            iconRes = R.mipmap.ic_launcher,
            foregroundRes = R.drawable.aefox_icon_foreground,
        ),
    )

    /** Selected icon or the default one when [id] is unknown (also covers untouched settings). */
    fun find(id: String?): BuiltinAppIcon = all.firstOrNull { it.id == id } ?: all.first()

    /** Enable [icon]'s alias (matching [dynamicColor]) and disable every other launcher entry. */
    fun apply(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false) {
        val pm = context.packageManager
        val selectedClass = componentClassName(icon, dynamicColor)
        setEnabled(pm, ComponentName(context, mainActivityName), true)
        setEnabled(pm, ComponentName(context, selectedClass), true)
        allAliasClassNames()
            .filter { it != selectedClass }
            .forEach { setEnabled(pm, ComponentName(context, it), false) }
    }

    /** Apply only when the component state drifted; avoids restarting during process startup. */
    fun applyIfNeeded(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false) {
        if (!isApplied(context, icon, dynamicColor)) {
            apply(context, icon, dynamicColor)
        }
    }

    /** True when [icon] (and its [dynamicColor] variant) is the single active launcher entry. */
    fun isApplied(context: Context, icon: BuiltinAppIcon, dynamicColor: Boolean = false): Boolean {
        val pm = context.packageManager
        if (!isComponentEnabled(pm, ComponentName(context, mainActivityName))) return false
        val selectedClass = componentClassName(icon, dynamicColor)
        if (!isComponentEnabled(pm, ComponentName(context, selectedClass))) return false
        return allAliasClassNames()
            .filter { it != selectedClass }
            .none { isComponentEnabled(pm, ComponentName(context, it)) }
    }

    private fun isComponentEnabled(pm: PackageManager, component: ComponentName): Boolean {
        val state = try {
            pm.getComponentEnabledSetting(component)
        } catch (t: Throwable) {
            AeFoxLog.w("$TAG: cannot read the enabled state of $component", t)
            return false
        }
        return when (state) {
            PackageManager.COMPONENT_ENABLED_STATE_ENABLED -> true
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_USER,
            PackageManager.COMPONENT_ENABLED_STATE_DISABLED_UNTIL_USED -> false
            else -> try {
                pm.getActivityInfo(component, 0).enabled
            } catch (t: Throwable) {
                false
            }
        }
    }

    private fun componentClassName(icon: BuiltinAppIcon, dynamicColor: Boolean): String {
        val base = mainActivityName + "Alias" + aliasSuffix(icon)
        return if (dynamicColor) base else base + "Static"
    }

    private fun allAliasClassNames(): List<String> = all.flatMap { icon ->
        val base = mainActivityName + "Alias" + aliasSuffix(icon)
        listOf(base, base + "Static")
    }

    private fun aliasSuffix(icon: BuiltinAppIcon): String = "Default"

    private fun setEnabled(pm: PackageManager, component: ComponentName, enabled: Boolean) {
        try {
            pm.setComponentEnabledSetting(
                component,
                if (enabled) {
                    PackageManager.COMPONENT_ENABLED_STATE_ENABLED
                } else {
                    PackageManager.COMPONENT_ENABLED_STATE_DISABLED
                },
                PackageManager.DONT_KILL_APP,
            )
        } catch (t: Throwable) {
            AeFoxLog.w("$TAG: cannot change the enabled state of $component", t)
        }
    }
}
