package io.diasjakupov.mindtag.core.network.dto

import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertNotNull

class DtoDeserializationTest {

    private val json = Json { ignoreUnknownKeys = true }

    @Test
    fun noteResponseDto_deserializesUpdatedAt() {
        val raw = """
            {
              "id": 1,
              "title": "Test Note",
              "subject": "Science",
              "body": "Some content",
              "contentHash": "abc123",
              "createdAt": "2024-01-01T00:00:00",
              "updatedAt": "2024-06-01T10:00:00"
            }
        """.trimIndent()

        val dto = json.decodeFromString<NoteResponseDto>(raw)

        assertNotNull(dto.updatedAt, "updatedAt must not be null — check @SerialName annotation")
        assertEquals("2024-06-01T10:00:00", dto.updatedAt)
    }

    @Test
    fun noteResponseDto_updatedAt_isNullableWhenAbsent() {
        val raw = """
            {
              "id": 2,
              "title": "No Update",
              "subject": "Math",
              "body": "Body",
              "contentHash": "def456",
              "createdAt": "2024-01-01T00:00:00"
            }
        """.trimIndent()

        val dto = json.decodeFromString<NoteResponseDto>(raw)

        assertEquals(null, dto.updatedAt)
    }

    // ── Task 2: RelatedNoteResponseDto ───────────────────────────────────────

    @Test
    fun relatedNoteResponseDto_deserializesIdAsLong() {
        val raw = """
            [
              { "id": 42, "title": "Related Note A" },
              { "id": 99, "title": "Related Note B" }
            ]
        """.trimIndent()

        val dtos = json.decodeFromString<List<RelatedNoteResponseDto>>(raw)

        assertEquals(2, dtos.size)
        assertEquals(42L, dtos[0].id)
        assertEquals("Related Note A", dtos[0].title)
        assertEquals(99L, dtos[1].id)
    }
}
