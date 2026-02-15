package io.diasjakupov.mindtag.feature.notes.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.SearchResponseDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.parameter

class SearchApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun search(query: String, page: Int, size: Int): ApiResult<SearchResponseDto> =
        safeApiCall(authManager) {
            client.get("/search") {
                parameter("query", query)
                parameter("page", page)
                parameter("size", size)
            }
        }

    suspend fun listBySubject(subject: String, page: Int, size: Int): ApiResult<SearchResponseDto> =
        safeApiCall(authManager) {
            client.get("/search/list") {
                parameter("subject", subject)
                parameter("page", page)
                parameter("size", size)
            }
        }
}