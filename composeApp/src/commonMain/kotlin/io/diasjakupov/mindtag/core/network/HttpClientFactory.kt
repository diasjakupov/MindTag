package io.diasjakupov.mindtag.core.network

import io.diasjakupov.mindtag.core.util.Logger
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.bearerAuth
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.contentType
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

object HttpClientFactory {

    fun create(authManager: AuthManager): HttpClient = HttpClient {
        install(ContentNegotiation) {
            json(Json {
                ignoreUnknownKeys = true
                isLenient = true
                encodeDefaults = true
            })
        }

        install(Logging) {
            logger = object : io.ktor.client.plugins.logging.Logger {
                override fun log(message: String) {
                    Logger.d("HTTP", message)
                }
            }
            level = LogLevel.ALL
        }

        defaultRequest {
            url(ServerConfig.BASE_URL)
            contentType(ContentType.Application.Json)
            val token = authManager.token
            if (token != null) {
                bearerAuth(token)
            }
        }
    }
}

suspend inline fun <reified T> safeApiCall(
    authManager: AuthManager,
    crossinline block: suspend () -> HttpResponse,
): ApiResult<T> {
    return try {
        val response = block()
        if (response.status.isSuccess()) {
            ApiResult.Success(response.body<T>())
        } else if (response.status.value == 401) {
            authManager.logout()
            ApiResult.Error("Session expired. Please log in again.", 401)
        } else {
            val errorBody = try { response.bodyAsText() } catch (_: Exception) { "" }
            ApiResult.Error(
                message = errorBody.ifBlank { "Server error (${response.status.value})" },
                code = response.status.value,
            )
        }
    } catch (e: Exception) {
        Logger.e("safeApiCall", "API call failed", e)
        ApiResult.Error(e.message ?: "Something went wrong. Try again.")
    }
}
