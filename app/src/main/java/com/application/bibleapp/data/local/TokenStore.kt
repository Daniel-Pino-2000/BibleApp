package com.application.bibleapp.data.local

import android.content.Context
import android.content.SharedPreferences
import androidx.security.crypto.EncryptedSharedPreferences
import androidx.security.crypto.MasterKey

/**
 * Persists the backend session (access token, refresh token, user id) in an encrypted
 * SharedPreferences file rather than a plain one. These are real bearer credentials —
 * equivalent to a password for as long as they're valid — so they get the same protection a
 * password would, not the same treatment as an ordinary app preference.
 */
class TokenStore(context: Context) {

    private val masterKey = MasterKey.Builder(context)
        .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
        .build()

    private val prefs: SharedPreferences = EncryptedSharedPreferences.create(
        context,
        "auth_tokens",
        masterKey,
        EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
        EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
    )

    val userId: String? get() = prefs.getString(KEY_USER_ID, null)
    val accessToken: String? get() = prefs.getString(KEY_ACCESS_TOKEN, null)
    val refreshToken: String? get() = prefs.getString(KEY_REFRESH_TOKEN, null)

    /** True once a session has been saved and hasn't been cleared - doesn't verify the
     *  tokens are still valid server-side, just that we have something to try sending. */
    val isLoggedIn: Boolean get() = accessToken != null && refreshToken != null

    fun saveSession(userId: String, accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_USER_ID, userId)
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    /** Called after a successful /auth/refresh - the backend rotates the refresh token on
     *  every use (the old one stops working), so the new pair must replace the old one here
     *  too, not just be handed back to whoever asked for the refresh. */
    fun updateTokens(accessToken: String, refreshToken: String) {
        prefs.edit()
            .putString(KEY_ACCESS_TOKEN, accessToken)
            .putString(KEY_REFRESH_TOKEN, refreshToken)
            .apply()
    }

    fun clear() {
        prefs.edit().clear().apply()
    }

    private companion object {
        const val KEY_ACCESS_TOKEN = "access_token"
        const val KEY_REFRESH_TOKEN = "refresh_token"
        const val KEY_USER_ID = "user_id"
    }
}
