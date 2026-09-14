package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable

/**
 * Placeholder shape for error responses, wired up in Routing.kt via StatusPages.
 *
 * (see docs/API_Integration_Guide.pdf) — e.g. an error code, a user-facing
 * message, and a field for validation-error details.
 */
@Serializable
data class ErrorResponse(
    val code: String,
    val message: String,
    val fieldErrors: Map<String, String>? = null
)
