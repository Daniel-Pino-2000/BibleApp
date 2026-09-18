package com.application.bibleapp.server.auth

import io.ktor.server.application.ApplicationCall
import io.ktor.server.auth.jwt.JWTPrincipal
import io.ktor.server.auth.principal
import java.util.UUID

/**
 * The authenticated user's id, read from the verified JWT's "sub" claim - never from anything
 * the client passes as a request field, which is how every resource endpoint enforces "you can
 * only see/edit your own data" (see docs/api_contract.md conventions).
 *
 * Safe to call without a null check inside any route nested under authenticate("auth-jwt"):
 * Ktor's Authentication plugin already rejects the request (via the challenge configured in
 * plugins/Security.kt) before the route handler ever runs if there's no valid principal.
 */
fun ApplicationCall.currentUserId(): UUID =
    UUID.fromString(principal<JWTPrincipal>()!!.payload.subject)
