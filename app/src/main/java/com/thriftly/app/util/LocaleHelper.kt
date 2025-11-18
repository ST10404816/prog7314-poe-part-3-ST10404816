package com.thriftly.app.util

import android.content.Context
import android.content.res.Configuration
import android.os.Build
import java.util.Locale

/**
 * Small helper to apply a language selection (display name) to a Context.
 * The app stores display names like "English", "Afrikaans", "Zulu".
 * This helper maps those to language tags and updates the context resources.
 */
object LocaleHelper {

    private fun mapDisplayNameToTag(display: String?): String {
        return when (display?.lowercase()) {
            "afrikaans" -> "af"
            "zulu" -> "zu"
            // default and fallback to English
            else -> "en"
        }
    }

    private fun normalizeToTag(tagOrDisplay: String?): String {
        if (tagOrDisplay == null) return "en"
        val lower = tagOrDisplay.lowercase()
        // simple heuristic: if already a short tag like "en" or contains '-' treat as tag
        if (lower.length <= 3 && lower.matches(Regex("[a-z]{2}(-[a-z]{2})?"))) return lower
        return mapDisplayNameToTag(tagOrDisplay)
    }

    /** Update the given context to use the language tag or display name. Returns a wrapped Context. */
    fun updateLocale(context: Context, tagOrDisplay: String?): Context {
        val tag = normalizeToTag(tagOrDisplay)
        val locale = try {
            Locale.forLanguageTag(tag)
        } catch (t: Throwable) {
            Locale(tag)
        }

        Locale.setDefault(locale)
        val res = context.resources
        val config = Configuration(res.configuration)

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            config.setLocale(locale)
            val ctx = context.createConfigurationContext(config)
            return ctx
        } else {
            config.locale = locale
            res.updateConfiguration(config, res.displayMetrics)
            return context
        }
    }
}
