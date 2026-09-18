package com.application.bibleapp.server.models

import kotlinx.serialization.Serializable

@Serializable
data class UserResponse(val id: String, val email: String, val createdAt: String)

@Serializable
data class DeleteAccountRequest(val password: String)
