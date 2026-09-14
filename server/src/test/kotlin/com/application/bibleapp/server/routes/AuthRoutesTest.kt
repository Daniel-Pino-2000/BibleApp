package com.application.bibleapp.server.routes

import com.application.bibleapp.server.models.AuthResponse
import com.application.bibleapp.server.models.RegisterRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.util.UUID
import kotlin.test.Test
import kotlin.test.assertEquals

// TODO: registered users accumulate in the shared dev Postgres across test runs since there's
// no cleanup (same known simplification as UsersTableTest). Generating a unique email per test
// invocation avoids the immediate problem (collisions with a previous run's data) without
// needing a dedicated test database yet.
class AuthRoutesTest {

    private fun uniqueRegisterRequest() = RegisterRequest(
        email = "authroutestest+${UUID.randomUUID()}@example.com",
        password = "Onezoro22@"
    )

    @Test
    fun verifyRegisterCreatesUser() = testApplication {
        environment {
            config = ApplicationConfig("application.conf")
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val response = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(uniqueRegisterRequest())
        }

        assertEquals(HttpStatusCode.Created, response.status)

        val authResponse = Json.decodeFromString<AuthResponse>(response.bodyAsText())

        // A valid userId must parse as a UUID - UUID.fromString throws IllegalArgumentException
        // (which fails the test) if the string isn't one.
        UUID.fromString(authResponse.userId)
        assertEquals("TODO-real-jwt", authResponse.accessToken)
        assertEquals("TODO-real-jwt", authResponse.refreshToken)
    }

    @Test
    fun verifyDuplicateEmailCase() = testApplication {
        environment {
            config = ApplicationConfig("application.conf")
        }

        val client = createClient {
            install(ContentNegotiation) {
                json()
            }
        }

        val request = uniqueRegisterRequest()

        val firstResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Created, firstResponse.status)

        val secondResponse = client.post("/api/v1/auth/register") {
            contentType(ContentType.Application.Json)
            setBody(request)
        }
        assertEquals(HttpStatusCode.Conflict, secondResponse.status)
    }
}