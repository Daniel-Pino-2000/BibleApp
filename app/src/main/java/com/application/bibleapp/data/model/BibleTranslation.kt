package com.application.bibleapp.data.model

enum class TextDirection { LTR, RTL }

/**
 * A downloadable Bible translation, independent of whichever remote API supplied it.
 * The version picker and download flow depend on this, not on a source-specific DTO,
 * so swapping the backing API doesn't ripple into UI/ViewModel code.
 */
data class BibleTranslation(
    val id: String,
    val displayName: String,
    val nativeName: String,
    val languageName: String,
    val languageCode: String,
    val textDirection: TextDirection
)
