package com.application.bibleapp.server.routes

import com.application.bibleapp.server.models.AuthResponse
import com.application.bibleapp.server.models.RegisterRequest
import io.ktor.client.HttpClient
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import kotlinx.serialization.json.Json
import java.util.UUID

/** The email/password used to register [auth], kept alongside it for tests that need to log
 * back in (login/refresh/logout tests) rather than just make an authenticated request. */
data class TestUser(val email: String, val password: String, val auth: AuthResponse)

/**
 * Registers a brand-new user (a fresh random email every call, so tests never collide with
 * each other or with leftover data from a previous run against the shared dev database) and
 * returns the real credentials plus a real AuthResponse - a real userId, a real signed access
 * token, and a real refresh token - ready to use in any test that needs an authenticated
 * request or a fresh login.
 */
suspend fun HttpClient.registerTestUser(
    password: String = "Onezoro22@"
): TestUser {
    val email = "test+${UUID.randomUUID()}@example.com"
    val response = post("/api/v1/auth/register") {
        contentType(ContentType.Application.Json)
        setBody(RegisterRequest(email = email, password = password))
    }
    return TestUser(email, password, Json.decodeFromString(response.bodyAsText()))
}
