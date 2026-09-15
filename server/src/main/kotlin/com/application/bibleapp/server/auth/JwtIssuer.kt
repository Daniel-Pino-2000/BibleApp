package com.application.bibleapp.server.auth

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import java.util.Date
import java.util.UUID

object JwtIssuer {
    fun issueAccessToken(
        secret: String,
        issuer: String,
        audience: String,
        userId: UUID,
        expiresInSeconds: Long
    ): String {

        val expirationTime = Date(
            System.currentTimeMillis() + expiresInSeconds * 1000
        )

        return JWT.create()
            .withIssuer(issuer)
            .withAudience(audience)
            .withSubject(userId.toString())
            .withExpiresAt(expirationTime).sign(Algorithm.HMAC256(secret))
    }
}