package com.application.bibleapp.data.remote

import io.ktor.client.HttpClient
import io.ktor.client.engine.cio.CIO
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * Provides a single shared HttpClient instance for the whole app.
 *
 * Why we use an object:
 *  - 'object' makes this a singleton (only one instance exists)
 *  - prevents memory leaks
 *  - prevents creating multiple clients unnecessarily
 *
 * Why this matters:
 *  - HttpClient is expensive to create
 *  - Reusing one instance improves performance and battery usage
 */
object HttpClientProvider {

    /**
     * Shared JSON config — reused both for Ktor's automatic content negotiation
     * and for manual `json.decodeFromString(...)` calls (e.g. HelloAoBibleDataSource
     * decoding a manually-buffered response body) so behavior stays consistent.
     */
    val json = Json {
        // If API returns extra fields your models don't have, your app will NOT crash
        ignoreUnknownKeys = true

        // Allows slightly non-strict JSON (e.g., missing quotes)
        isLenient = true
    }

    /**
     * Lazily-initialized Ktor HTTP client.
     *
     * 'by lazy' means:
     *  - client is created ONLY when first accessed
     *  - not created at app startup
     *  - thread-safe
     */
    val client: HttpClient by lazy {

        // Create HttpClient using CIO engine (good async I/O engine)
        HttpClient(CIO) {

            // Install automatic content negotiation
            install(ContentNegotiation) {
                // Tell Ktor to use kotlinx.serialization for JSON
                json(json)
            }

            // Prevents a hung/slow request from blocking a bulk download indefinitely.
            // Large single-request downloads (e.g. a translation's complete.json) override
            // requestTimeoutMillis per-call since a multi-MB body can legitimately take
            // longer than this default on a slow connection.
            install(HttpTimeout) {
                requestTimeoutMillis = 15_000
                connectTimeoutMillis = 10_000
                socketTimeoutMillis = 15_000
            }
        }
    }
}
