package com.application.bibleapp.server.auth

import java.security.MessageDigest
import java.security.SecureRandom
import java.util.Base64

/**
 * Refresh tokens are high-entropy random strings, not JWTs - the server never decodes
 * anything out of one, it just looks up its hash in the database. That's also why hashing
 * a refresh token is different from hashing a password: a refresh token already has enormous
 * entropy from being randomly generated (unlike a human-chosen password), so brute-forcing it
 * is already infeasible regardless of hash speed. A fast cryptographic hash (SHA-256) is the
 * right tool here; bcrypt's deliberate slowness would just waste CPU for no real benefit.
 */
object RefreshTokenIssuer {
    private const val TOKEN_BYTE_LENGTH = 32
    private val secureRandom = SecureRandom()

    /** A new, unhashed, URL-safe random token - this is the value the client actually receives. */
    fun generateToken(): String {
        val bytes = ByteArray(TOKEN_BYTE_LENGTH)
        secureRandom.nextBytes(bytes)
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes)
    }

    /** The value actually stored in the database - never the raw token itself. */
    fun hash(rawToken: String): String {
        val digestBytes = MessageDigest.getInstance("SHA-256").digest(rawToken.toByteArray(Charsets.UTF_8))
        return digestBytes.joinToString(separator = "") { byte -> "%02x".format(byte) }
    }
}
