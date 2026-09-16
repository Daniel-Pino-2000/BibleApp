package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable

@Serializable
data class ReadingProgressResponse(
    val versionId: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    val updatedAt: String
)

@Serializable
data class UpdateReadingProgressRequest(
    val versionId: String,
    val bookId: Int,
    val chapter: Int,
    val verse: Int,
    /** The client's local timestamp for this read event - see docs/api_contract.md decision 8. */
    val readAt: String
)
