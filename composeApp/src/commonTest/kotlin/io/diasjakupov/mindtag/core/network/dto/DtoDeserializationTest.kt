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

    // ── Task 3: Search DTOs ───────────────────────────────────────────────────

    @Test
    fun searchResultDto_deserializesIdAsLong() {
        val raw = """
            {
              "total": 2,
              "page": 0,
              "size": 20,
              "results": [
                { "id": 10, "title": "First Result", "snippet": "Some snippet" },
                { "id": 20, "title": "Second Result", "snippet": "Another snippet" }
              ]
            }
        """.trimIndent()

        val dto = json.decodeFromString<SearchResponseDto>(raw)

        assertEquals(2L, dto.total)
        assertEquals(10L, dto.results[0].id)
        assertEquals("First Result", dto.results[0].title)
        assertEquals(20L, dto.results[1].id)
    }

    @Test
    fun semanticSearchResultDto_deserializesIdAsLong() {
        val raw = """
            [
              {
                "id": 55,
                "userId": 1,
                "title": "Semantic Note",
                "body": "Semantic body content",
                "updatedAt": "2024-05-01T09:00:00",
                "contentHash": "xyz789"
              }
            ]
        """.trimIndent()

        val dtos = json.decodeFromString<List<SemanticSearchResultDto>>(raw)

        assertEquals(1, dtos.size)
        assertEquals(55L, dtos[0].id)
        assertEquals("Semantic Note", dtos[0].title)
    }
}
