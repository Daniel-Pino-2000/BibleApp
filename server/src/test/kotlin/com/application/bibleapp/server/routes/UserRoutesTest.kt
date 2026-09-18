package com.application.bibleapp.server.routes

import com.application.bibleapp.server.models.DeleteAccountRequest
import com.application.bibleapp.server.models.ErrorResponse
import com.application.bibleapp.server.models.UserResponse
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals

class UserRoutesTest {

    @Test
    fun verifyGetMeReturnsTheCurrentUser() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }

        val user = client.registerTestUser()

        val response = client.get("/api/v1/users/me") {
            bearerAuth(user.auth.accessToken)
        }

        assertEquals(HttpStatusCode.OK, response.status)
        val userResponse = Json.decodeFromString<UserResponse>(response.bodyAsText())
        assertEquals(user.auth.userId, userResponse.id)
        assertEquals(user.email, userResponse.email)
    }

    @Test
    fun verifyGetMeWithoutAnAccessTokenFails() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }

        val response = client.get("/api/v1/users/me")

        assertEquals(HttpStatusCode.Unauthorized, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("UNAUTHORIZED", error.code)
    }

    @Test
    fun verifyDeleteMeWithWrongPasswordFailsAndKeepsTheAccount() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }

        val user = client.registerTestUser()

        val deleteResponse = client.delete("/api/v1/users/me") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(DeleteAccountRequest(password = "the-wrong-password"))
        }
        assertEquals(HttpStatusCode.Unauthorized, deleteResponse.status)

        // The account must still exist - a rejected deletion shouldn't remove anything.
        val getResponse = client.get("/api/v1/users/me") {
            bearerAuth(user.auth.accessToken)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
    }

    @Test
    fun verifyDeleteMeWithTheCorrectPasswordRemovesTheAccount() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }

        val user = client.registerTestUser()

        val deleteResponse = client.delete("/api/v1/users/me") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(DeleteAccountRequest(password = user.password))
        }
        assertEquals(HttpStatusCode.NoContent, deleteResponse.status)

        // The access token is still cryptographically valid until it expires (deleting a user
        // doesn't revoke already-issued JWTs), but the user it points at is genuinely gone now,
        // so looking it up correctly reports 404 rather than pretending the account exists.
        val getResponse = client.get("/api/v1/users/me") {
            bearerAuth(user.auth.accessToken)
        }
        assertEquals(HttpStatusCode.NotFound, getResponse.status)
    }
}
