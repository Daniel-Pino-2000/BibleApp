package com.application.bibleapp.data.remote

/**
 * Versions available in one language, ready for display under a section header.
 */
data class LanguageGroup(
    val languageName: String,
    val languageCode: String,
    val versions: List<BibleVersionDto>
)

/**
 * Groups [versions] by language (sorted alphabetically by language name), optionally
 * filtered first by [query] against language name, version name, and description.
 * Pure function — no Android/network dependency — so it's plain-JVM testable.
 */
fun groupVersionsByLanguage(versions: List<BibleVersionDto>, query: String = ""): List<LanguageGroup> {
    val trimmedQuery = query.trim()
    val filtered = if (trimmedQuery.isEmpty()) {
        versions
    } else {
        versions.filter { version ->
            version.language.name.contains(trimmedQuery, ignoreCase = true) ||
                version.version.contains(trimmedQuery, ignoreCase = true) ||
                version.description?.contains(trimmedQuery, ignoreCase = true) == true
        }
    }

    return filtered
        .groupBy { it.language.name }
        .entries
        .sortedBy { it.key.lowercase() }
        .map { (languageName, versionsInLanguage) ->
            LanguageGroup(
                languageName = languageName,
                languageCode = versionsInLanguage.first().language.code,
                versions = versionsInLanguage.sortedBy { it.version.lowercase() }
            )
        }
}
