package io.diasjakupov.mindtag.core.network

import android.content.Context
import android.content.SharedPreferences

actual class TokenStorage(context: Context) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("mindtag_auth", Context.MODE_PRIVATE)

    actual fun saveToken(token: String, userId: Long) {
        prefs.edit()
            .putString("access_token", token)
            .putLong("user_id", userId)
            .apply()
    }

    actual fun getToken(): String? = prefs.getString("access_token", null)

    actual fun getUserId(): Long? {
        val id = prefs.getLong("user_id", -1L)
        return if (id == -1L) null else id
    }

    actual fun clear() {
        prefs.edit().clear().apply()
    }
}
