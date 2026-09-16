package com.application.bibleapp.server.routes

import com.application.bibleapp.server.models.ErrorResponse
import com.application.bibleapp.server.models.ReadingProgressResponse
import com.application.bibleapp.server.models.UpdateReadingProgressRequest
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.bearerAuth
import io.ktor.client.request.get
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.config.ApplicationConfig
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import java.time.Instant
import kotlin.test.Test
import kotlin.test.assertEquals

class ReadingProgressRoutesTest {

    @Test
    fun verifyGetBeforeAnySyncReturnsNotFound() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }
        val user = client.registerTestUser()

        val response = client.get("/api/v1/reading-progress") {
            bearerAuth(user.auth.accessToken)
        }

        assertEquals(HttpStatusCode.NotFound, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("NOT_FOUND", error.code)
    }

    @Test
    fun verifyPutCreatesProgressAndGetReturnsIt() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }
        val user = client.registerTestUser()

        val putResponse = client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(
                UpdateReadingProgressRequest(
                    versionId = "kjv",
                    bookId = 43,
                    chapter = 3,
                    verse = 16,
                    readAt = Instant.now().toString()
                )
            )
        }
        assertEquals(HttpStatusCode.OK, putResponse.status)

        val getResponse = client.get("/api/v1/reading-progress") {
            bearerAuth(user.auth.accessToken)
        }
        assertEquals(HttpStatusCode.OK, getResponse.status)
        val progress = Json.decodeFromString<ReadingProgressResponse>(getResponse.bodyAsText())
        assertEquals(43, progress.bookId)
        assertEquals(3, progress.chapter)
        assertEquals(16, progress.verse)
    }

    @Test
    fun verifyANewerReadAtOverwritesTheStoredPosition() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }
        val user = client.registerTestUser()

        val now = Instant.now()
        client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(UpdateReadingProgressRequest("kjv", bookId = 1, chapter = 1, verse = 1, readAt = now.toString()))
        }

        val laterResponse = client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(
                UpdateReadingProgressRequest(
                    "kjv",
                    bookId = 43,
                    chapter = 3,
                    verse = 16,
                    readAt = now.plusSeconds(60).toString()
                )
            )
        }

        assertEquals(HttpStatusCode.OK, laterResponse.status)
        val updated = Json.decodeFromString<ReadingProgressResponse>(laterResponse.bodyAsText())
        assertEquals(43, updated.bookId)
    }

    @Test
    fun verifyAnOlderReadAtIsIgnoredAndTheStoredPositionWins() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }
        val user = client.registerTestUser()

        val now = Instant.now()
        client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(UpdateReadingProgressRequest("kjv", bookId = 43, chapter = 3, verse = 16, readAt = now.toString()))
        }

        // Simulates a device that was offline and only now syncs an older read event - it must
        // not clobber the newer position already stored (docs/api_contract.md decision 8).
        val staleResponse = client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(
                UpdateReadingProgressRequest(
                    "kjv",
                    bookId = 1,
                    chapter = 1,
                    verse = 1,
                    readAt = now.minusSeconds(3600).toString()
                )
            )
        }

        assertEquals(HttpStatusCode.OK, staleResponse.status)
        val stillCurrent = Json.decodeFromString<ReadingProgressResponse>(staleResponse.bodyAsText())
        assertEquals(43, stillCurrent.bookId)
        assertEquals(3, stillCurrent.chapter)
        assertEquals(16, stillCurrent.verse)
    }

    @Test
    fun verifyValidationRejectsAnOutOfRangeBookId() = testApplication {
        environment { config = ApplicationConfig("application.conf") }
        val client = createClient { install(ContentNegotiation) { json() } }
        val user = client.registerTestUser()

        val response = client.put("/api/v1/reading-progress") {
            contentType(ContentType.Application.Json)
            bearerAuth(user.auth.accessToken)
            setBody(
                UpdateReadingProgressRequest(
                    "kjv",
                    bookId = 999,
                    chapter = 1,
                    verse = 1,
                    readAt = Instant.now().toString()
                )
            )
        }

        assertEquals(HttpStatusCode.BadRequest, response.status)
        val error = Json.decodeFromString<ErrorResponse>(response.bodyAsText())
        assertEquals("VALIDATION_ERROR", error.code)
    }
}
