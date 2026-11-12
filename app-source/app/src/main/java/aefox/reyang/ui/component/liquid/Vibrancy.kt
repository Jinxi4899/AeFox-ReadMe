// Adapted from an open-source liquid glass implementation (Apache 2.0).
// Mirrored from compose-miuix-ui example.

package aefox.reyang.ui.component.liquid

import top.yukonga.miuix.kmp.blur.BackdropEffectScope
import top.yukonga.miuix.kmp.blur.colorControls

fun BackdropEffectScope.vibrancy() {
    colorControls(
        brightness = 0f,
        contrast = 1f,
        saturation = 1.5f,
    )
}
