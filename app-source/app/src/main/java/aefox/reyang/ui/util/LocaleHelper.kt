package aefox.reyang.ui.util

import android.content.Context
import android.content.res.Configuration
import android.content.res.Resources
import android.os.Build
import android.os.LocaleList
import androidx.core.content.edit
import java.util.Locale

/**
 * Persists and applies the app display language.
 *
 * Android 13+ uses [android.app.LocaleManager.applicationLocales] as the source of
 * truth, so the system recreates activities itself. Older versions apply the locale
 * through [Context.createConfigurationContext] in [wrap].
 */
object LocaleHelper {
    private const val PREFS = "settings"
    private const val PREF_LANGUAGE = "app_language"

    /** Follow the system language. */
    const val SYSTEM = ""

    val SUPPORTED_TAGS: List<String> = listOf(
        "en", "ar", "az", "bg", "bn", "bn-BD", "bs", "da", "de", "es", "et",
        "fa", "fil", "fr", "gl", "hi", "hr", "hu", "id", "it", "he", "ja",
        "km", "kn", "ko", "lt", "lv", "mr", "ms", "my", "nl", "pl", "pt",
        "pt-BR", "ro", "ru", "sl", "sr", "te", "th", "tr", "uk", "vi",
        "zh-CN", "zh-HK", "zh-TW",
    )

    fun getPersistedLanguage(context: Context): String =
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .getString(PREF_LANGUAGE, SYSTEM) ?: SYSTEM

    private fun persistLanguage(context: Context, tag: String) {
        context.getSharedPreferences(PREFS, Context.MODE_PRIVATE)
            .edit { putString(PREF_LANGUAGE, tag) }
    }

    private fun getSystemLocale(): Locale = Resources.getSystem().configuration.locales[0]

    private fun localeManager(context: Context): android.app.LocaleManager? =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            context.getSystemService(android.app.LocaleManager::class.java)
        } else {
            null
        }

    private fun syncWithSystem(context: Context): String {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.TIRAMISU) {
            return getPersistedLanguage(context)
        }
        val locales = localeManager(context)?.applicationLocales ?: LocaleList.getEmptyLocaleList()
        val tag = if (locales.isEmpty) SYSTEM else locales[0].toLanguageTag()
        persistLanguage(context, tag)
        return tag
    }

    fun getCurrentLanguage(context: Context): String =
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            syncWithSystem(context)
        } else {
            getPersistedLanguage(context)
        }

    fun setLanguage(context: Context, tag: String) {
        persistLanguage(context, tag)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            localeManager(context)?.applicationLocales = if (tag.isEmpty()) {
                LocaleList.getEmptyLocaleList()
            } else {
                LocaleList.forLanguageTags(tag)
            }
            return
        }
        Locale.setDefault(if (tag.isEmpty()) getSystemLocale() else Locale.forLanguageTag(tag))
    }

    /** Human-readable language name in its own locale. */
    fun displayName(tag: String): String {
        val locale = Locale.forLanguageTag(tag)
        return locale.getDisplayName(locale)
            .replaceFirstChar { if (it.isLowerCase()) it.titlecase(locale) else it.toString() }
    }

    /** Applies the persisted locale to [base] (call from attachBaseContext). */
    fun wrap(base: Context): Context {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            syncWithSystem(base)
            Locale.setDefault(base.resources.configuration.locales[0])
            return base
        }
        val tag = getPersistedLanguage(base)
        if (tag.isEmpty()) {
            Locale.setDefault(getSystemLocale())
            return base
        }
        val locale = Locale.forLanguageTag(tag)
        Locale.setDefault(locale)
        val config = Configuration(base.resources.configuration)
        config.setLocale(locale)
        return base.createConfigurationContext(config)
    }
}
