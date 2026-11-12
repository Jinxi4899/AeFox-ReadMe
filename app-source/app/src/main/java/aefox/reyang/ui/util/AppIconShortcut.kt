package aefox.reyang.ui.util

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import androidx.core.content.pm.ShortcutInfoCompat
import androidx.core.content.pm.ShortcutManagerCompat
import androidx.core.graphics.drawable.IconCompat
import aefox.reyang.AppIconShortcutActivity

/**
 * Home-screen shortcuts with a custom icon.
 *
 * Android does not let an app replace its launcher icon with an arbitrary bitmap, so imported
 * pack icons are pinned as shortcuts instead; the shortcut launches [AppIconShortcutActivity],
 * which forwards to the active launcher entry.
 */
object AppIconShortcut {

    /** Publish and request pinning; returns true when the launcher accepted the request. */
    fun create(context: Context, key: String, label: String, bitmap: Bitmap): Boolean = runCatching {
        val shortcut = ShortcutInfoCompat.Builder(context, "app_icon_$key")
            .setShortLabel(label)
            .setLongLabel(label)
            .setIntent(
                Intent(context, AppIconShortcutActivity::class.java).apply {
                    action = Intent.ACTION_MAIN
                    addCategory(Intent.CATEGORY_LAUNCHER)
                    addFlags(Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP)
                },
            )
            .setIcon(IconCompat.createWithAdaptiveBitmap(bitmap))
            .build()
        ShortcutManagerCompat.pushDynamicShortcut(context, shortcut)
        if (ShortcutManagerCompat.isRequestPinShortcutSupported(context)) {
            ShortcutManagerCompat.requestPinShortcut(context, shortcut, null)
        } else {
            false
        }
    }.getOrDefault(false)
}
