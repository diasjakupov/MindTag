package io.diasjakupov.mindtag.core.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class SemanticAnalyzerTest {

    @Test
    fun tokenizeRemovesStopWordsAndPunctuation() {
        val tokens = SemanticAnalyzer.tokenize("The quick brown fox jumps over the lazy dog!")
        assertTrue("the" !in tokens)
        assertTrue("over" !in tokens)
        assertTrue("quick" in tokens)
        assertTrue("brown" in tokens)
        assertTrue("fox" in tokens)
        // No punctuation attached
        assertTrue("dog" in tokens)
        assertTrue("dog!" !in tokens)
    }

    @Test
    fun tokenizeHandlesEmptyString() {
        val tokens = SemanticAnalyzer.tokenize("")
        assertTrue(tokens.isEmpty())
    }

    @Test
    fun cosineSimilarityOfIdenticalDocumentsIsOne() {
        val docs = listOf("Mitosis is cell division", "Mitosis is cell division")
        val tfidf = SemanticAnalyzer.buildTfidfVectors(docs.map { SemanticAnalyzer.tokenize(it) })
        val sim = SemanticAnalyzer.cosineSimilarity(tfidf[0], tfidf[1])
        assertTrue(sim > 0.99f, "Expected ~1.0, got $sim")
    }

    @Test
    fun cosineSimilarityOfUnrelatedDocumentsIsLow() {
        val docs = listOf(
            "Photosynthesis converts sunlight into glucose energy",
            "Binary search trees provide logarithmic lookup time",
        )
        val tfidf = SemanticAnalyzer.buildTfidfVectors(docs.map { SemanticAnalyzer.tokenize(it) })
        val sim = SemanticAnalyzer.cosineSimilarity(tfidf[0], tfidf[1])
        assertTrue(sim < 0.15f, "Expected low similarity, got $sim")
    }

    @Test
    fun cosineSimilarityOfRelatedDocumentsIsModerate() {
        val docs = listOf(
            "Mitosis is the process of cell division into two daughter cells",
            "Cell division through meiosis produces four daughter cells",
        )
        val tfidf = SemanticAnalyzer.buildTfidfVectors(docs.map { SemanticAnalyzer.tokenize(it) })
        val sim = SemanticAnalyzer.cosineSimilarity(tfidf[0], tfidf[1])
        assertTrue(sim > 0.15f, "Expected moderate similarity, got $sim")
    }

    @Test
    fun computeLinksReturnsEmptyForSingleNote() {
        val result = SemanticAnalyzer.computeLinks(
            newNoteId = "note-1",
            newNoteText = "Mitosis is cell division",
            newNoteSubjectId = "bio",
            newNoteWeek = 1,
            allOtherNotes = emptyList(),
        )
        assertTrue(result.isEmpty())
    }

    @Test
    fun computeLinksDetectsRelatedNotes() {
        val result = SemanticAnalyzer.computeLinks(
            newNoteId = "note-new",
            newNoteText = "Mitosis is the process of cell division where chromosomes are replicated and separated",
            newNoteSubjectId = "bio",
            newNoteWeek = 2,
            allOtherNotes = listOf(
                SemanticAnalyzer.NoteInfo("note-1", "Cell division occurs during mitosis and meiosis in biological organisms", "bio", 1),
                SemanticAnalyzer.NoteInfo("note-2", "Supply and demand determines market equilibrium price", "econ", 1),
            ),
        )
        // Should find link to note-1 (biology/cell division overlap), not note-2
        assertTrue(result.any { it.targetNoteId == "note-1" }, "Expected link to related bio note")
        assertTrue(result.none { it.targetNoteId == "note-2" }, "Should not link to unrelated econ note")
    }

    @Test
    fun computeLinksAssignsPrerequisiteForSameSubjectEarlierWeek() {
        val result = SemanticAnalyzer.computeLinks(
            newNoteId = "note-new",
            newNoteText = "Advanced cell division including meiosis and mitosis phases",
            newNoteSubjectId = "bio",
            newNoteWeek = 3,
            allOtherNotes = listOf(
                SemanticAnalyzer.NoteInfo("note-1", "Introduction to cell division and basic mitosis process", "bio", 1),
            ),
        )
        val link = result.firstOrNull { it.targetNoteId == "note-1" }
        assertTrue(link != null, "Expected a link")
        assertEquals("PREREQUISITE", link.linkType)
    }

    @Test
    fun computeLinksAssignsAnalogyForCrossSubjectHighSimilarity() {
        val result = SemanticAnalyzer.computeLinks(
            newNoteId = "note-new",
            newNoteText = "Graph algorithms traverse nodes and edges to find shortest paths using optimization",
            newNoteSubjectId = "cs",
            newNoteWeek = null,
            allOtherNotes = listOf(
                SemanticAnalyzer.NoteInfo("note-1", "Network optimization algorithms find shortest paths through graph nodes and edges", "math", null),
            ),
        )
        val link = result.firstOrNull { it.targetNoteId == "note-1" }
        assertTrue(link != null, "Expected cross-subject link")
        assertEquals("ANALOGY", link.linkType)
    }
}
