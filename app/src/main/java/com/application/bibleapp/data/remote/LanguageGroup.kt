package com.application.bibleapp.data.remote

import com.application.bibleapp.data.model.BibleTranslation

/**
 * Translations available in one language, ready for display under a section header.
 */
data class LanguageGroup(
    val languageName: String,
    val languageCode: String,
    val translations: List<BibleTranslation>
)

/**
 * Groups [translations] by language (sorted alphabetically by language name),
 * optionally filtered first by [query] against language name, display name, and
 * native name. Pure function — no Android/network dependency — so it's plain-JVM
 * testable.
 */
fun groupVersionsByLanguage(translations: List<BibleTranslation>, query: String = ""): List<LanguageGroup> {
    val trimmedQuery = query.trim()
    val filtered = if (trimmedQuery.isEmpty()) {
        translations
    } else {
        translations.filter { translation ->
            translation.languageName.contains(trimmedQuery, ignoreCase = true) ||
                translation.displayName.contains(trimmedQuery, ignoreCase = true) ||
                translation.nativeName.contains(trimmedQuery, ignoreCase = true)
        }
    }

    return filtered
        .groupBy { it.languageName }
        .entries
        .sortedBy { it.key.lowercase() }
        .map { (languageName, translationsInLanguage) ->
            LanguageGroup(
                languageName = languageName,
                languageCode = translationsInLanguage.first().languageCode,
                translations = translationsInLanguage.sortedBy { it.displayName.lowercase() }
            )
        }
}
