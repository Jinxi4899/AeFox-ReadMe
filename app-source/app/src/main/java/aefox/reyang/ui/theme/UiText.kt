package aefox.reyang.ui.theme

import androidx.compose.ui.unit.sp

/**
 * Text size roles shared by the Miuix pages.
 *
 * Miuix has no app-level typography role (unlike Material's `Typography`), so pages were
 * hand-picking sizes. These roles keep the current visual scale in one place; they are
 * deliberately derived from the sizes the app already ships instead of the miuix defaults
 * (main 17 / body1 16 / body2 14), which would enlarge most secondary copy.
 *
 * Rules (see DEVELOPMENT.md "UI 排版与尺寸"):
 * - Material pages keep using `MaterialTheme.typography.*`; do not use these roles there.
 * - Miuix pages must use a role instead of a literal `sp`, unless the size is a one-off
 *   mock (theme preview, sticker stage) that mirrors a card geometry constant.
 */
object UiText {
    /** Status badges and step labels (10sp). */
    val Tag = 10.sp

    /** Footnotes and micro hints (11sp). */
    val Caption = 11.sp

    /** Secondary copy: hints, metadata, list subtitles (12sp). */
    val Secondary = 12.sp

    /** Dense secondary values such as label/value rows (13sp). */
    val Detail = 13.sp

    /** Body copy and list item titles (14sp). */
    val Body = 14.sp

    /** Card headers inside the edit pages (15sp). */
    val CardTitle = 15.sp

    /** Primary titles and dialog headers (16sp). */
    val Title = 16.sp

    /** Large titles such as profile names and section heads (18sp). */
    val TitleLarge = 18.sp

    /** Page hero titles and stage preview headings (22sp). */
    val Hero = 22.sp

    /** Classic home status title (36sp). */
    val Display = 36.sp
}
