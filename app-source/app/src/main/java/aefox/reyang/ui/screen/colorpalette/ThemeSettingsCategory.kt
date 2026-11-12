package aefox.reyang.ui.screen.colorpalette

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.CallToAction
import androidx.compose.material.icons.rounded.Colorize
import androidx.compose.material.icons.rounded.Description
import androidx.compose.material.icons.rounded.Edit
import androidx.compose.material.icons.rounded.Home
import androidx.compose.material.icons.rounded.Palette
import androidx.compose.material.icons.rounded.Swipe
import androidx.compose.material.icons.rounded.Wallpaper
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import aefox.reyang.R

/**
 * The categories the appearance page is split into.
 *
 * Mirrors SukiSU's ThemeSettingsCategory trimmed to the sections AeFox ships: only one
 * category's settings are composed at a time; the category grid above the content area
 * switches between them instead of stacking every section in one long list.
 */
enum class ThemeSettingsCategory(
    @StringRes val labelRes: Int,
    @StringRes val titleRes: Int,
    @StringRes val summaryRes: Int,
    val icon: ImageVector,
) {
    COLORS(
        labelRes = R.string.theme_category_colors,
        titleRes = R.string.settings_section_colors,
        summaryRes = R.string.theme_category_colors_summary,
        icon = Icons.Rounded.Colorize,
    ),
    BACKGROUND(
        labelRes = R.string.theme_category_background,
        titleRes = R.string.settings_section_background,
        summaryRes = R.string.theme_category_background_summary,
        icon = Icons.Rounded.Wallpaper,
    ),
    ICON_TEXT(
        labelRes = R.string.theme_category_icon_text,
        titleRes = R.string.settings_section_icon_text,
        summaryRes = R.string.theme_category_icon_text_summary,
        icon = Icons.Rounded.Edit,
    ),
    HOME_CARD(
        labelRes = R.string.theme_category_home_card,
        titleRes = R.string.settings_section_home_card,
        summaryRes = R.string.theme_category_home_card_summary,
        icon = Icons.Rounded.Home,
    ),
    BOTTOM_BAR(
        labelRes = R.string.theme_category_bottom_bar,
        titleRes = R.string.settings_section_bottom_bar,
        summaryRes = R.string.theme_category_bottom_bar_summary,
        icon = Icons.Rounded.CallToAction,
    ),
    INTERACTION(
        labelRes = R.string.theme_category_interaction,
        titleRes = R.string.settings_section_interaction,
        summaryRes = R.string.theme_category_interaction_summary,
        icon = Icons.Rounded.Swipe,
    ),
    DETAILS(
        labelRes = R.string.theme_category_details,
        titleRes = R.string.settings_section_details,
        summaryRes = R.string.theme_category_details_summary,
        icon = Icons.Rounded.Description,
    ),
    THEME_PACKAGE(
        labelRes = R.string.theme_category_theme_package,
        titleRes = R.string.settings_section_theme_package,
        summaryRes = R.string.theme_category_theme_package_summary,
        icon = Icons.Rounded.Palette,
    ),
}

/** A row of theme color dots used by the pinned compact preview (mirrors SukiSU). */
@Composable
internal fun ThemePaletteSwatches(
    colors: List<Color>,
    modifier: Modifier = Modifier,
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(6.dp),
    ) {
        colors.forEach { color ->
            Box(
                modifier = Modifier
                    .size(18.dp)
                    .clip(CircleShape)
                    .background(color),
            )
        }
    }
}
