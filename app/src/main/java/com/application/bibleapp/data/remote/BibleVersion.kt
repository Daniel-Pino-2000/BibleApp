package com.application.bibleapp.data.remote

import kotlinx.serialization.Serializable

/**
 * A single Bible version entry as returned by bibles.json.
 *
 * Example:
 *  - id: "en-kjv"
 *  - language: "English"
 *  - name: "King James Version"
 *
 * This is NOT a downloaded Bible. It is just metadata describing a version.
 */
@Serializable
data class BibleVersionDto(
    /** Unique version identifier used in API paths (e.g., "en-kjv"). */
    val id: String,

    /** Human-readable language name (e.g., "English", "Spanish"). */
    val language: String,

    /** Full display name of the Bible version (e.g., "King James Version"). */
    val name: String
)
