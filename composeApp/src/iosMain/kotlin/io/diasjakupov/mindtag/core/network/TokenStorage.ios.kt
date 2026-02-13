package io.diasjakupov.mindtag.core.network

import platform.Foundation.NSUserDefaults

actual class TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveToken(token: String, userId: Long) {
        defaults.setObject(token, forKey = "mindtag_access_token")
        defaults.setInteger(userId, forKey = "mindtag_user_id")
    }

    actual fun getToken(): String? =
        defaults.stringForKey("mindtag_access_token")

    actual fun getUserId(): Long? {
        val id = defaults.integerForKey("mindtag_user_id")
        return if (id == 0L) null else id
    }

    actual fun clear() {
        defaults.removeObjectForKey("mindtag_access_token")
        defaults.removeObjectForKey("mindtag_user_id")
    }
}
