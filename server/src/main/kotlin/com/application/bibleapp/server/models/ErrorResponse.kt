package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable

/**
 * Placeholder shape for error responses, wired up in Routing.kt via StatusPages.
 *
 * TODO: replace with the real error-response contract once it's designed
 * (see docs/API_Integration_Guide.pdf) — e.g. an error code, a user-facing
 * message, and a field for validation-error details.
 */
@Serializable
data class ErrorResponse(
    val error: String
)
