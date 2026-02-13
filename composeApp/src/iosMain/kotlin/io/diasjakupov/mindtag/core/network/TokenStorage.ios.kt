package io.diasjakupov.mindtag.core.network

import platform.Foundation.NSUserDefaults

actual class TokenStorage {
    private val defaults = NSUserDefaults.standardUserDefaults

    actual fun saveToken(token: String, userId: Long) {
        defaults.setObject(token, forKey = "mindtag_access_token")
        defaults.setObject(userId.toString(), forKey = "mindtag_user_id")
    }

    actual fun getToken(): String? =
        defaults.stringForKey("mindtag_access_token")

    actual fun getUserId(): Long? =
        defaults.stringForKey("mindtag_user_id")?.toLongOrNull()

    actual fun clear() {
        defaults.removeObjectForKey("mindtag_access_token")
        defaults.removeObjectForKey("mindtag_user_id")
    }
}
