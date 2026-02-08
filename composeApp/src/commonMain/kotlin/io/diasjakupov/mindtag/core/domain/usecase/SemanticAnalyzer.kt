package io.diasjakupov.mindtag.core.domain.usecase

import kotlin.math.ln
import kotlin.math.sqrt

object SemanticAnalyzer {

    data class NoteInfo(
        val id: String,
        val text: String,
        val subjectId: String,
        val weekNumber: Int?,
    )

    data class LinkResult(
        val targetNoteId: String,
        val similarityScore: Float,
        val linkType: String,
        val strength: Float,
    )

    private val STOP_WORDS = setOf(
        "a", "an", "the", "is", "are", "was", "were", "be", "been", "being",
        "have", "has", "had", "do", "does", "did", "will", "would", "shall",
        "should", "may", "might", "must", "can", "could", "am", "not", "no",
        "nor", "but", "or", "and", "if", "then", "else", "when", "at", "by",
        "for", "with", "about", "against", "between", "through", "during",
        "before", "after", "above", "below", "to", "from", "up", "down",
        "in", "out", "on", "off", "over", "under", "again", "further",
        "once", "here", "there", "where", "why", "how", "all", "each",
        "every", "both", "few", "more", "most", "other", "some", "such",
        "only", "own", "same", "so", "than", "too", "very", "just",
        "because", "as", "until", "while", "of", "into", "it", "its",
        "this", "that", "these", "those", "i", "me", "my", "we", "our",
        "you", "your", "he", "him", "his", "she", "her", "they", "them",
        "their", "what", "which", "who", "whom",
    )

    private const val SIMILARITY_THRESHOLD = 0.15f
    private const val ANALOGY_THRESHOLD = 0.25f

    fun tokenize(text: String): List<String> {
        if (text.isBlank()) return emptyList()
        return text.lowercase()
            .replace(Regex("[^a-z0-9\\s]"), "")
            .split(Regex("\\s+"))
            .filter { it.isNotBlank() && it !in STOP_WORDS && it.length > 1 }
    }

    fun buildTfidfVectors(tokenizedDocs: List<List<String>>): List<Map<String, Float>> {
        val n = tokenizedDocs.size
        if (n == 0) return emptyList()

        // Document frequency: how many docs contain each term
        val df = mutableMapOf<String, Int>()
        tokenizedDocs.forEach { tokens ->
            tokens.toSet().forEach { token ->
                df[token] = (df[token] ?: 0) + 1
            }
        }

        return tokenizedDocs.map { tokens ->
            val tf = mutableMapOf<String, Int>()
            tokens.forEach { token -> tf[token] = (tf[token] ?: 0) + 1 }

            val vector = mutableMapOf<String, Float>()
            tf.forEach { (term, count) ->
                val termFreq = count.toFloat() / tokens.size.coerceAtLeast(1)
                val idf = ln((n.toFloat() + 1f) / ((df[term] ?: 0) + 1f)) + 1f
                vector[term] = termFreq * idf
            }
            vector
        }
    }

    fun cosineSimilarity(v1: Map<String, Float>, v2: Map<String, Float>): Float {
        if (v1.isEmpty() || v2.isEmpty()) return 0f

        val allTerms = v1.keys + v2.keys
        var dotProduct = 0f
        var norm1 = 0f
        var norm2 = 0f

        allTerms.forEach { term ->
            val a = v1[term] ?: 0f
            val b = v2[term] ?: 0f
            dotProduct += a * b
            norm1 += a * a
            norm2 += b * b
        }

        val denominator = sqrt(norm1) * sqrt(norm2)
        return if (denominator == 0f) 0f else dotProduct / denominator
    }

    fun computeLinks(
        newNoteId: String,
        newNoteText: String,
        newNoteSubjectId: String,
        newNoteWeek: Int?,
        allOtherNotes: List<NoteInfo>,
    ): List<LinkResult> {
        if (allOtherNotes.isEmpty()) return emptyList()

        val newTokens = tokenize(newNoteText)
        if (newTokens.isEmpty()) return emptyList()

        val allTokenized = listOf(newTokens) + allOtherNotes.map { tokenize(it.text) }
        val tfidfVectors = buildTfidfVectors(allTokenized)
        val newVector = tfidfVectors[0]

        return allOtherNotes.mapIndexedNotNull { index, otherNote ->
            val otherVector = tfidfVectors[index + 1]
            val similarity = cosineSimilarity(newVector, otherVector)

            if (similarity < SIMILARITY_THRESHOLD) return@mapIndexedNotNull null

            val isCrossSubject = newNoteSubjectId != otherNote.subjectId
            val linkType = when {
                isCrossSubject && similarity >= ANALOGY_THRESHOLD -> "ANALOGY"
                isCrossSubject -> "RELATED"
                !isCrossSubject && newNoteWeek != null && otherNote.weekNumber != null
                    && otherNote.weekNumber < newNoteWeek -> "PREREQUISITE"
                else -> "RELATED"
            }

            LinkResult(
                targetNoteId = otherNote.id,
                similarityScore = similarity,
                linkType = linkType,
                strength = similarity,
            )
        }
    }
}
