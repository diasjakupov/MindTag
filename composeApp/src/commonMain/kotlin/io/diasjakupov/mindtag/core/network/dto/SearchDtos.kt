package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.Serializable

@Serializable
data class SearchResultDto(
    val id: Long,
    val title: String,
    val snippet: String,
)

@Serializable
data class SearchResponseDto(
    val total: Long,
    val page: Int,
    val size: Int,
    val results: List<SearchResultDto>,
)

@Serializable
data class SemanticSearchResultDto(
    val id: Long,
    val userId: Long,
    val title: String,
    val body: String,
    val updatedAt: String? = null,
    val contentHash: String? = null,
)
