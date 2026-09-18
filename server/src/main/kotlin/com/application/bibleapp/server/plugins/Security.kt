package com.application.bibleapp.server.plugins

import com.application.bibleapp.server.auth.getJwtConfig
import com.application.bibleapp.server.models.ErrorResponse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.jwt.jwt
import io.ktor.server.response.respond

fun Application.configureSecurity() {
    val jwtConfig = getJwtConfig()

    install(Authentication) {
        jwt("auth-jwt") {
            realm = jwtConfig.realm
            verifier(
                JWT.require(Algorithm.HMAC256(jwtConfig.secret))
                    .withAudience(jwtConfig.audience)
                    .withIssuer(jwtConfig.issuer)
                    .build()
            )
            validate { credential ->
                if (credential.payload.audience.contains(jwtConfig.audience) &&
                    credential.payload.subject != null) {
                    JWTPrincipal(credential.payload)
                } else {
                    null
                }
            }
            // Without this, a missing/invalid/expired token gets Ktor's default bare 401 with
            // no body matching our error contract. This keeps every protected endpoint's 401
            // response consistent with the rest of the API.
            challenge { _, _ ->
                call.respond(
                    HttpStatusCode.Unauthorized,
                    ErrorResponse(code = "UNAUTHORIZED", message = "Missing or invalid access token")
                )
            }
        }
    }
}
