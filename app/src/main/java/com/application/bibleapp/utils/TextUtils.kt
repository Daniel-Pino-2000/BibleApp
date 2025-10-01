package com.application.bibleapp.utils

import java.text.Normalizer

object TextUtils {

    // Normalize: lowercase + remove accents/diacritics + remove punctuation
    fun String.normalizeForSearch(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized
            .replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "") // strip accents
            .lowercase()
            .replace("[^a-z0-9 ]".toRegex(), " ") // strip punctuation, keep spaces
            .replace("\\s+".toRegex(), " ")       // collapse multiple spaces
            .trim()
    }
}
