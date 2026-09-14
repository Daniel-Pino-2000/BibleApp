package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable

@Serializable
data class RegisterRequest(val email: String, val password: String)

@Serializable
data class AuthResponse(
    val userId: String,
    val accessToken: String,
    val accessTokenExpiresInSeconds: Long,
    val refreshToken: String
)
