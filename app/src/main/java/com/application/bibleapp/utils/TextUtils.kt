package com.application.bibleapp.utils

import java.text.Normalizer

object TextUtils {

    // Remove accents/diacritics
    fun String.removeAccents(): String {
        val normalized = Normalizer.normalize(this, Normalizer.Form.NFD)
        return normalized.replace("\\p{InCombiningDiacriticalMarks}+".toRegex(), "")
    }

    // Remove punctuation
    fun String.cleanPunctuation(): String {
        return this.replace("[.,;!?]".toRegex(), "")
    }

    // Normalize for search: lowercase + remove accents + remove punctuation
    fun String.normalizeForSearch(): String {
        return this.lowercase()
            .replace("á","a")
            .replace("é","e")
            .replace("í","i")
            .replace("ó","o")
            .replace("ú","u")
            .replace("ü","u")
            .replace("ñ","n")
            .replace("[^a-z0-9 ]".toRegex(), "") // remove punctuation
    }

}
