package io.diasjakupov.mindtag.feature.notes.data.api

import io.diasjakupov.mindtag.core.network.ApiResult
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.dto.NoteCreateRequestDto
import io.diasjakupov.mindtag.core.network.dto.NoteResponseDto
import io.diasjakupov.mindtag.core.network.dto.NoteUpdateRequestDto
import io.diasjakupov.mindtag.core.network.dto.RelatedNoteResponseDto
import io.diasjakupov.mindtag.core.network.safeApiCall
import io.ktor.client.HttpClient
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody

class NoteApi(
    private val client: HttpClient,
    private val authManager: AuthManager,
) {
    suspend fun getNotes(): ApiResult<List<NoteResponseDto>> =
        safeApiCall(authManager) { client.get("/notes") }

    suspend fun getNoteById(id: Long): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) { client.get("/notes/$id") }

    suspend fun createNote(title: String, subject: String, body: String): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) {
            client.post("/notes") {
                setBody(NoteCreateRequestDto(title, subject, body))
            }
        }

    suspend fun updateNote(id: Long, title: String, subject: String, body: String): ApiResult<NoteResponseDto> =
        safeApiCall(authManager) {
            client.put("/notes/$id") {
                setBody(NoteUpdateRequestDto(title, subject, body))
            }
        }

    suspend fun deleteNote(id: Long): ApiResult<Unit> =
        safeApiCall(authManager) { client.delete("/notes/$id") }

    suspend fun getRelatedNotes(noteId: Long): ApiResult<List<RelatedNoteResponseDto>> =
        safeApiCall(authManager) { client.get("/notes/$noteId/related") }
}
