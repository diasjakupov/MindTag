package io.diasjakupov.mindtag.core.network

import java.util.prefs.Preferences

actual class TokenStorage {
    private val prefs: Preferences =
        Preferences.userNodeForPackage(TokenStorage::class.java)

    actual fun saveToken(token: String, userId: Long) {
        prefs.put("access_token", token)
        prefs.putLong("user_id", userId)
        prefs.flush()
    }

    actual fun getToken(): String? = prefs.get("access_token", null)

    actual fun getUserId(): Long? {
        val id = prefs.getLong("user_id", -1L)
        return if (id == -1L) null else id
    }

    actual fun clear() {
        prefs.remove("access_token")
        prefs.remove("user_id")
        prefs.flush()
    }
}
