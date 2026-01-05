package com.application.bibleapp.data.network

import io.ktor.client.*
import io.ktor.client.engine.android.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.serialization.kotlinx.json.*
import kotlinx.serialization.json.Json

/**
 * Provides a single, shared Ktor HttpClient instance for network operations.
 * Using 'object' ensures one instance exists across the app.
 */
object KtorClientProvider {

    val client: HttpClient by lazy {
        HttpClient(Android) {  // Android-specific HTTP engine
            install(ContentNegotiation) {  // Handle JSON automatically
                json(Json {
                    ignoreUnknownKeys = true // Ignore any extra JSON fields not in our model
                    isLenient = true         // Accept flexible JSON formats
                })
            }
        }
    }
}
