package com.application.bibleapp.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.time.Instant
import java.util.UUID

object JwtIssuer {
    const val ACCESS_TOKEN_EXPIRES_IN_SECONDS = 900L

    fun issueAccessToken(
        secret: String,
        issuer: String,
        audience: String,
        userId: UUID,
        expiresInSeconds: Long = ACCESS_TOKEN_EXPIRES_IN_SECONDS
    ): String {
        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withExpiresAt(Instant.now().plusSeconds(expiresInSeconds))
            .sign(Algorithm.HMAC256(secret))
    }
}