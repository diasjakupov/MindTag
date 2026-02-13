package io.diasjakupov.mindtag.core.network

expect class TokenStorage {
    fun saveToken(token: String, userId: Long)
    fun getToken(): String?
    fun getUserId(): Long?
    fun clear()
}
