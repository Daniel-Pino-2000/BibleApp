package com.application.bibleapp.data.model

import kotlinx.serialization.Serializable

/**
 * Represents a Bible version entry from the JSON file.
 * @param versionId Unique identifier for the version
 * @param name Display name of the Bible version
 * @param language Language code (e.g., "en", "es")
 * @param url URL to download the version database
 */
@Serializable
data class BibleVersion(
    val versionId: String,
    val name: String,
    val language: String,
    val url: String
)
