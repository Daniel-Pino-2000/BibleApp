package com.application.bibleapp.data.network

import com.application.bibleapp.data.model.BibleVersion
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*

/**
 * This class provides functions to fetch Bible versions from a remote API.
 * It uses KtorClientProvider.client to make network requests.
 */
class BibleVersionApi(private val client: HttpClient = KtorClientProvider.client) {

    // Example URL of the endpoint that returns Bible versions in JSON format.
    private val baseUrl = "https://example.com/api/bible/versions"

    /**
     * Fetches the list of Bible versions from the server.
     * @return A list of BibleVersion objects.
     */
    suspend fun getBibleVersions(): List<BibleVersion> {
        try {
            // Make a GET request to the API endpoint
            val response: HttpResponse = client.get(baseUrl) {
                // Optional: You can set headers here if needed
                headers {
                    append(HttpHeaders.Accept, "application/json")
                }
            }

            // Deserialize the response body into a list of BibleVersion
            // The 'body()' function automatically converts JSON to the data class using Ktor serialization
            return response.body()

        } catch (e: Exception) {
            // Handle network or parsing errors
            e.printStackTrace()
            return emptyList() // Return empty list if anything goes wrong
        }
    }
}
