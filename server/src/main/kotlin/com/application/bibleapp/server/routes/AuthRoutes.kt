package com.application.bibleapp.server.routes

import com.application.bibleapp.server.auth.JwtIssuer
import com.application.bibleapp.server.auth.PasswordHasher
import com.application.bibleapp.server.auth.getJwtConfig
import com.application.bibleapp.server.db.tables.Users
import com.application.bibleapp.server.models.AuthResponse
import com.application.bibleapp.server.models.ErrorResponse
import com.application.bibleapp.server.models.RegisterRequest
import io.ktor.http.HttpStatusCode
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.Route
import io.ktor.server.routing.post
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.transactions.transaction
import java.time.Instant

private val EMAIL_REGEX = Regex("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")
private const val MIN_PASSWORD_LENGTH = 8

fun Route.authRoutes() {

    post("/auth/register") {
        val request = call.receive<RegisterRequest>()

        val fieldErrors = mutableMapOf<String, String>()
        if (!EMAIL_REGEX.matches(request.email)) {
            fieldErrors["email"] = "must be a valid email address"
        }
        if (request.password.length < MIN_PASSWORD_LENGTH) {
            fieldErrors["password"] = "must be at least $MIN_PASSWORD_LENGTH characters"
        }
        if (fieldErrors.isNotEmpty()) {
            call.respond(
                HttpStatusCode.BadRequest,
                ErrorResponse(
                    code = "VALIDATION_ERROR",
                    message = "One or more fields are invalid",
                    fieldErrors = fieldErrors
                )
            )
            return@post
        }

        val existingUser = transaction {
            Users.selectAll().where { Users.email eq request.email }.singleOrNull()
        }

        if (existingUser != null) {
            call.respond(
                HttpStatusCode.Conflict,
                ErrorResponse(code = "EMAIL_ALREADY_REGISTERED", message = "Email is already registered")
            )
            return@post
        }

        val passwordHash = PasswordHasher.hash(request.password)

        val userId = transaction {
            Users.insert {
                it[email] = request.email
                it[Users.passwordHash] = passwordHash
                it[createdAt] = Instant.now()
            } get Users.id
        }

        val jwtConfig = call.application.getJwtConfig()

        val expiresInSeconds = 900L

        val accessToken = JwtIssuer.issueAccessToken(
            secret = jwtConfig.secret,
            issuer = jwtConfig.issuer,
            audience = jwtConfig.audience,
            userId = userId,
            expiresInSeconds = expiresInSeconds
        )

        call.respond(
            HttpStatusCode.Created,
            AuthResponse(
                userId = userId.toString(),
                accessToken = "TODO-real-jwt",
                accessTokenExpiresInSeconds = 900,
                refreshToken = "TODO-real-jwt"
            )
        )
    }
}
