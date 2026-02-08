# Smart Offline MVP Implementation Plan

> **For Claude:** REQUIRED SUB-SKILL: Use superpowers:executing-plans to implement this plan task-by-task.

**Goal:** Make MindTag demo-ready for a university defense by adding local AI semantic linking, auto-generated flashcards, reactive Profile stats, and persistent Planner checkboxes — all without a backend.

**Architecture:** Four independent pillars. Pillar 1 (SemanticAnalyzer) and Pillar 3 (FlashcardGenerator) hook into the existing `CreateNoteUseCase` flow. Pillar 2 (Profile) wires SQLDelight queries into `ProfileViewModel`. Pillar 4 (Planner) adds a new `PlannerTaskEntity` table and rewires `PlannerViewModel` to use it. All pure Kotlin, no network dependencies.

**Tech Stack:** Kotlin 2.3.0, SQLDelight 2.0.2, Koin 4.0.2, Compose Multiplatform 1.10.0, Turbine (testing)

---

### Task 1: Create SemanticAnalyzer — TF-IDF text similarity engine

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/SemanticAnalyzer.kt`
- Test: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/core/domain/usecase/SemanticAnalyzerTest.kt`

**Step 1: Write the tests**

Create the test file. These tests verify the core TF-IDF logic in isolation (no DB):

```kotlin
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
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew :composeApp:jvmTest --tests "io.diasjakupov.mindtag.core.domain.usecase.SemanticAnalyzerTest"`
Expected: FAIL — `SemanticAnalyzer` class does not exist.

**Step 3: Implement SemanticAnalyzer**

```kotlin
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
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "io.diasjakupov.mindtag.core.domain.usecase.SemanticAnalyzerTest"`
Expected: All 8 tests PASS.

---

### Task 2: Wire SemanticAnalyzer into note creation flow

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/usecase/CreateNoteUseCase.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/domain/repository/NoteRepository.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/repository/NoteRepositoryImpl.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`
- Modify: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/test/FakeNoteRepository.kt`
- Test: `composeApp/src/jvmTest/kotlin/io/diasjakupov/mindtag/NoteRepositoryImplTest.kt`

**Step 1: Add `getAllNotesSnapshot()` to NoteRepository interface**

In `NoteRepository.kt`, add one method that returns all notes synchronously (needed by SemanticAnalyzer to compute links against all existing notes):

```kotlin
suspend fun getAllNotesSnapshot(): List<Note>
```

**Step 2: Implement `getAllNotesSnapshot()` in NoteRepositoryImpl**

In `NoteRepositoryImpl.kt`, add after the `deleteNote` method:

```kotlin
override suspend fun getAllNotesSnapshot(): List<Note> =
    db.noteEntityQueries.selectAll().executeAsList().map { it.toDomain() }
```

**Step 3: Add `getAllNotesSnapshot()` to FakeNoteRepository**

In `FakeNoteRepository.kt`, add:

```kotlin
override suspend fun getAllNotesSnapshot(): List<Note> = notesFlow.value
```

**Step 4: Wire SemanticAnalyzer into NoteRepositoryImpl.createNote()**

After the `db.noteEntityQueries.insert(...)` call in `createNote()` (after line 88), add the semantic analysis block:

```kotlin
// Auto-generate semantic links
val allNotes = db.noteEntityQueries.selectAll().executeAsList().map { it.toDomain() }
val otherNotes = allNotes.filter { it.id != id }
val noteInfo = otherNotes.map { n ->
    SemanticAnalyzer.NoteInfo(n.id, "${n.title} ${n.content}", n.subjectId, n.weekNumber)
}
val links = SemanticAnalyzer.computeLinks(
    newNoteId = id,
    newNoteText = "$title $content",
    newNoteSubjectId = subjectId,
    newNoteWeek = null,
    allOtherNotes = noteInfo,
)
links.forEach { link ->
    val linkId = kotlin.uuid.Uuid.random().toString()
    db.semanticLinkEntityQueries.insert(
        id = linkId,
        source_note_id = id,
        target_note_id = link.targetNoteId,
        similarity_score = link.similarityScore.toDouble(),
        link_type = link.linkType,
        strength = link.strength.toDouble(),
        created_at = now,
    )
}
Logger.d(tag, "createNote: generated ${links.size} semantic links")
```

Add import at the top of `NoteRepositoryImpl.kt`:

```kotlin
import io.diasjakupov.mindtag.core.domain.usecase.SemanticAnalyzer
```

**Step 5: Write integration test**

Add to `NoteRepositoryImplTest.kt`:

```kotlin
@Test
fun createNoteAutoGeneratesSemanticLinks() = runTest {
    // Create two related biology notes
    repository.createNote(
        title = "Cell Division",
        content = "Mitosis is the process of cell division where chromosomes replicate and separate into two identical daughter cells.",
        subjectId = "subj-bio",
    )
    val note2 = repository.createNote(
        title = "Meiosis Process",
        content = "Meiosis is a type of cell division that produces four daughter cells each with half the chromosomes of the parent cell.",
        subjectId = "subj-bio",
    )

    // Check semantic links were created
    val links = database.semanticLinkEntityQueries.selectBySourceNoteId(note2.id).executeAsList()
    assertTrue(links.isNotEmpty(), "Expected auto-generated semantic links")
    assertEquals("subj-bio", note2.subjectId)
}

@Test
fun createNoteDoesNotLinkUnrelatedNotes() = runTest {
    repository.createNote(
        title = "Photosynthesis",
        content = "Plants convert sunlight into glucose through the process of photosynthesis using chlorophyll.",
        subjectId = "subj-bio",
    )
    val note2 = repository.createNote(
        title = "Binary Search",
        content = "Binary search algorithm divides the sorted array in half to find target element in logarithmic time.",
        subjectId = "subj-cs",
    )

    val links = database.semanticLinkEntityQueries.selectBySourceNoteId(note2.id).executeAsList()
    assertTrue(links.isEmpty(), "Expected no links between unrelated notes, got ${links.size}")
}
```

**Step 6: Run all tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

---

### Task 3: Create FlashcardGenerator — auto-generate flashcards from note content

**Files:**
- Create: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/domain/usecase/FlashcardGenerator.kt`
- Test: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/core/domain/usecase/FlashcardGeneratorTest.kt`

**Step 1: Write the tests**

```kotlin
package io.diasjakupov.mindtag.core.domain.usecase

import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

class FlashcardGeneratorTest {

    @Test
    fun generateReturnsEmptyForShortContent() {
        val cards = FlashcardGenerator.generate(
            noteTitle = "Short Note",
            noteContent = "Too short.",
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.isEmpty())
    }

    @Test
    fun generateCapsAtFiveCards() {
        // Create content with many sentences
        val content = (1..20).joinToString(". ") {
            "The important concept number $it involves understanding the fundamental principles of cellular biology and molecular interactions"
        } + "."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Big Note",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.size <= 5, "Expected max 5 cards, got ${cards.size}")
    }

    @Test
    fun generateProducesCardsWithCorrectSubjectId() {
        val content = "Mitosis is the process of cell division where chromosomes are replicated. " +
            "During prophase the chromatin condenses into visible chromosomes. " +
            "Metaphase aligns chromosomes along the cell equator for proper separation."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Cell Division",
            noteContent = content,
            subjectId = "subj-bio",
            noteId = "note-1",
        )
        assertTrue(cards.isNotEmpty())
        cards.forEach { card ->
            assertEquals("subj-bio", card.subjectId)
            assertTrue(card.question.isNotBlank())
            assertTrue(card.correctAnswer.isNotBlank())
            assertTrue(card.sourceNoteIdsJson.contains("note-1"))
        }
    }

    @Test
    fun generateAssignsDifficultyProgression() {
        val content = (1..10).joinToString(". ") {
            "The fundamental theorem number $it describes how biological systems maintain homeostasis through complex feedback mechanisms"
        } + "."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Bio Theorems",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        if (cards.size >= 3) {
            assertEquals("EASY", cards[0].difficulty)
            assertEquals("EASY", cards[1].difficulty)
            assertEquals("MEDIUM", cards[2].difficulty)
        }
        if (cards.size >= 5) {
            assertEquals("MEDIUM", cards[3].difficulty)
            assertEquals("HARD", cards[4].difficulty)
        }
    }

    @Test
    fun generateCreatesFactCheckCards() {
        val content = "Mitosis is the process of cell division where chromosomes are replicated and distributed equally. " +
            "The process ensures genetic consistency between parent and daughter cells through precise mechanisms."
        val cards = FlashcardGenerator.generate(
            noteTitle = "Mitosis",
            noteContent = content,
            subjectId = "bio",
            noteId = "note-1",
        )
        assertTrue(cards.isNotEmpty())
        cards.forEach { card ->
            assertEquals("FACT_CHECK", card.type)
        }
    }
}
```

**Step 2: Run tests to verify they fail**

Run: `./gradlew :composeApp:jvmTest --tests "io.diasjakupov.mindtag.core.domain.usecase.FlashcardGeneratorTest"`
Expected: FAIL — class not found.

**Step 3: Implement FlashcardGenerator**

```kotlin
package io.diasjakupov.mindtag.core.domain.usecase

object FlashcardGenerator {

    private const val MAX_CARDS = 5
    private const val MIN_SENTENCE_WORDS = 8

    data class GeneratedCard(
        val question: String,
        val correctAnswer: String,
        val type: String,
        val difficulty: String,
        val subjectId: String,
        val sourceNoteIdsJson: String,
        val explanation: String,
    )

    fun generate(
        noteTitle: String,
        noteContent: String,
        subjectId: String,
        noteId: String,
    ): List<GeneratedCard> {
        val sentences = splitSentences(noteContent)
            .filter { it.split(" ").size >= MIN_SENTENCE_WORDS }

        if (sentences.isEmpty()) return emptyList()

        val sourceJson = "[\"$noteId\"]"

        return sentences.take(MAX_CARDS).mapIndexed { index, sentence ->
            val difficulty = when {
                index < 2 -> "EASY"
                index < 4 -> "MEDIUM"
                else -> "HARD"
            }

            val question = createQuestion(sentence, noteTitle)
            GeneratedCard(
                question = question,
                correctAnswer = sentence.trim(),
                type = "FACT_CHECK",
                difficulty = difficulty,
                subjectId = subjectId,
                sourceNoteIdsJson = sourceJson,
                explanation = "From your note: $noteTitle",
            )
        }
    }

    private fun splitSentences(text: String): List<String> {
        return text.split(Regex("(?<=[.!?])\\s+"))
            .map { it.trim() }
            .filter { it.isNotBlank() }
    }

    private fun createQuestion(sentence: String, noteTitle: String): String {
        // Strategy: find a key noun phrase and blank it out
        val words = sentence.split(" ")
        if (words.size < MIN_SENTENCE_WORDS) return "True or False: $sentence"

        // Find the longest capitalized phrase or a noun-like word to blank
        val blankTarget = findBlankTarget(words)
        return if (blankTarget != null) {
            sentence.replace(blankTarget, "_____") + " (Fill in the blank)"
        } else {
            // Fallback: "What is described by" style
            "According to your notes on $noteTitle: True or False — $sentence"
        }
    }

    private fun findBlankTarget(words: List<String>): String? {
        // Strategy 1: Look for a sequence of capitalized words (proper nouns / key terms)
        // Skip the first word since it's always capitalized after a period
        val candidates = mutableListOf<String>()
        var i = 1
        while (i < words.size) {
            if (words[i].first().isUpperCase() && words[i].all { it.isLetter() }) {
                val start = i
                while (i < words.size && words[i].first().isUpperCase()) i++
                candidates.add(words.subList(start, i).joinToString(" "))
            }
            i++
        }
        if (candidates.isNotEmpty()) {
            return candidates.maxByOrNull { it.length }
        }

        // Strategy 2: Look for technical-sounding words (long words that aren't stop words)
        val technicalWords = words.drop(1)
            .filter { it.length > 6 && it.all { c -> c.isLetter() } }
            .filter { it.lowercase() !in SemanticAnalyzer.tokenize(it).isEmpty().let { setOf<String>() } }

        return technicalWords.maxByOrNull { it.length }
    }
}
```

**Step 4: Run tests to verify they pass**

Run: `./gradlew :composeApp:jvmTest --tests "io.diasjakupov.mindtag.core.domain.usecase.FlashcardGeneratorTest"`
Expected: All 5 tests PASS.

---

### Task 4: Wire FlashcardGenerator into note creation flow

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/notes/data/repository/NoteRepositoryImpl.kt`

**Step 1: Add flashcard generation after semantic link generation**

In `NoteRepositoryImpl.createNote()`, after the semantic links block (added in Task 2), add:

```kotlin
// Auto-generate flashcards
val generatedCards = FlashcardGenerator.generate(
    noteTitle = title,
    noteContent = content,
    subjectId = subjectId,
    noteId = id,
)
generatedCards.forEach { card ->
    val cardId = kotlin.uuid.Uuid.random().toString()
    db.flashCardEntityQueries.insert(
        id = cardId,
        question = card.question,
        type = card.type,
        difficulty = card.difficulty,
        subject_id = card.subjectId,
        correct_answer = card.correctAnswer,
        options_json = null,
        source_note_ids_json = card.sourceNoteIdsJson,
        ai_explanation = card.explanation,
        ease_factor = 2.5,
        interval_days = 0,
        repetitions = 0,
        next_review_at = null,
        created_at = now,
    )
}
Logger.d(tag, "createNote: generated ${generatedCards.size} flashcards")
```

Add import:

```kotlin
import io.diasjakupov.mindtag.core.domain.usecase.FlashcardGenerator
```

**Step 2: Write integration test**

Add to `NoteRepositoryImplTest.kt`:

```kotlin
@Test
fun createNoteAutoGeneratesFlashcards() = runTest {
    val note = repository.createNote(
        title = "Photosynthesis",
        content = "Photosynthesis is the process by which green plants convert sunlight into chemical energy. " +
            "Chlorophyll absorbs light energy primarily from the red and blue wavelengths of visible light. " +
            "The light reactions occur in the thylakoid membranes and produce ATP and NADPH molecules.",
        subjectId = "subj-bio",
    )

    val cards = database.flashCardEntityQueries.selectAll().executeAsList()
    assertTrue(cards.isNotEmpty(), "Expected auto-generated flashcards")
    assertTrue(cards.all { it.subject_id == "subj-bio" })
    assertTrue(cards.all { it.source_note_ids_json?.contains(note.id) == true })
}
```

**Step 3: Run all tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

---

### Task 5: Wire Profile stats to real SQLDelight queries

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/profile/presentation/ProfileContract.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/profile/presentation/ProfileViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`
- Modify: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/feature/profile/presentation/ProfileViewModelTest.kt`

**Step 1: Update ProfileContract.State to add isLoading**

```kotlin
data class State(
    val userName: String = "Alex Johnson",
    val email: String = "alex.johnson@university.edu",
    val totalNotes: Int = 0,
    val totalStudySessions: Int = 0,
    val currentStreak: Int = 0,
    val totalXp: Int = 0,
    val memberSince: String = "January 2026",
    val isLoading: Boolean = true,
)
```

Defaults change from hardcoded values to 0 — they'll be populated by DB queries.

**Step 2: Update ProfileViewModel to accept MindTagDatabase and query stats**

```kotlin
package io.diasjakupov.mindtag.feature.profile.presentation

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Effect
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.Intent
import io.diasjakupov.mindtag.feature.profile.presentation.ProfileContract.State
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn

class ProfileViewModel(
    private val db: MindTagDatabase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "ProfileVM"

    init {
        loadStats()
    }

    private fun loadStats() {
        combine(
            db.noteEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            db.studySessionEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
            db.userProgressEntityQueries.selectAll().asFlow().mapToList(Dispatchers.IO),
        ) { notes, sessions, progressList ->
            val maxStreak = progressList.maxOfOrNull { it.current_streak } ?: 0L
            val totalXp = progressList.sumOf { it.total_xp }

            updateState {
                copy(
                    totalNotes = notes.size,
                    totalStudySessions = sessions.size,
                    currentStreak = maxStreak.toInt(),
                    totalXp = totalXp.toInt(),
                    isLoading = false,
                )
            }
        }.launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
    }
}
```

**Step 3: Update Koin DI to inject MindTagDatabase into ProfileViewModel**

In `Modules.kt`, change:

```kotlin
viewModel { ProfileViewModel() }
```

to:

```kotlin
viewModel { ProfileViewModel(get()) }
```

**Step 4: Update ProfileViewModelTest**

The test currently checks hardcoded values. Replace the entire test to use an in-memory database:

```kotlin
package io.diasjakupov.mindtag.feature.profile.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {

    private lateinit var db: MindTagDatabase
    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        db = MindTagDatabase(driver)

        // Seed some test data
        val now = System.currentTimeMillis()
        db.subjectEntityQueries.insert("subj-1", "Bio", "#22C55E", "leaf", 0.0, 0, 0, now, now)
        db.noteEntityQueries.insert("n1", "Note 1", "Content", "Sum", "subj-1", null, 5, now, now)
        db.noteEntityQueries.insert("n2", "Note 2", "Content", "Sum", "subj-1", null, 3, now, now)
        db.studySessionEntityQueries.insert("s1", "subj-1", "QUICK_QUIZ", now, null, 10, null, "COMPLETED")
        db.userProgressEntityQueries.insert("subj-1", 0.65, 3, 5, 0.8, 7, 500, now)

        viewModel = ProfileViewModel(db)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun statsLoadFromDatabase() = runTest {
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()

            assertFalse(state.isLoading)
            assertEquals(2, state.totalNotes)
            assertEquals(1, state.totalStudySessions)
            assertEquals(7, state.currentStreak)
            assertEquals(500, state.totalXp)
            assertEquals("Alex Johnson", state.userName)
            cancelAndConsumeRemainingEvents()
        }
    }

    @Test
    fun profileHasStaticUserInfo() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Alex Johnson", state.userName)
            assertEquals("alex.johnson@university.edu", state.email)
            assertEquals("January 2026", state.memberSince)
            cancelAndConsumeRemainingEvents()
        }
    }
}
```

Note: This test must be in `jvmTest` since it uses `JdbcSqliteDriver`. Move it from `commonTest` to `jvmTest`:
- Delete: `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/feature/profile/presentation/ProfileViewModelTest.kt`
- Create: `composeApp/src/jvmTest/kotlin/io/diasjakupov/mindtag/feature/profile/presentation/ProfileViewModelTest.kt`

**Step 5: Run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

---

### Task 6: Add PlannerTaskEntity table and seed data

**Files:**
- Create: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/PlannerTaskEntity.sq`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/data/seed/SeedData.kt`

**Step 1: Create PlannerTaskEntity.sq**

```sql
CREATE TABLE PlannerTaskEntity (
    id TEXT NOT NULL PRIMARY KEY,
    week_id TEXT NOT NULL,
    week_number INTEGER NOT NULL,
    week_title TEXT NOT NULL,
    week_date_range TEXT NOT NULL,
    is_current_week INTEGER NOT NULL DEFAULT 0,
    title TEXT NOT NULL,
    subject_name TEXT NOT NULL,
    subject_color_hex TEXT NOT NULL,
    type TEXT NOT NULL,
    is_completed INTEGER NOT NULL DEFAULT 0
);

CREATE INDEX idx_planner_week ON PlannerTaskEntity(week_id);

selectAll:
SELECT * FROM PlannerTaskEntity ORDER BY week_number ASC, id ASC;

selectByWeekId:
SELECT * FROM PlannerTaskEntity WHERE week_id = ? ORDER BY id ASC;

insert:
INSERT OR REPLACE INTO PlannerTaskEntity (id, week_id, week_number, week_title, week_date_range, is_current_week, title, subject_name, subject_color_hex, type, is_completed)
VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?, ?, ?);

toggleCompleted:
UPDATE PlannerTaskEntity SET is_completed = CASE WHEN is_completed = 0 THEN 1 ELSE 0 END WHERE id = ?;

countByWeekId:
SELECT COUNT(*) FROM PlannerTaskEntity WHERE week_id = ?;

countCompletedByWeekId:
SELECT COUNT(*) FROM PlannerTaskEntity WHERE week_id = ? AND is_completed = 1;

countAll:
SELECT COUNT(*) FROM PlannerTaskEntity;

countAllCompleted:
SELECT COUNT(*) FROM PlannerTaskEntity WHERE is_completed = 1;
```

**Step 2: Add planner seed data to SeedData.kt**

Add a new method `insertPlannerTasks(db: MindTagDatabase)` and call it from `populate()`:

```kotlin
// Add to populate():
insertPlannerTasks(db)

// New method:
private fun insertPlannerTasks(db: MindTagDatabase) {
    val q = db.plannerTaskEntityQueries

    // Week 1 — all completed
    q.insert("t1_1", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Intro to Cognitive Science", "Psychology 101", "#3B82F6", "LECTURE", 1)
    q.insert("t1_2", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Chapter 1: Learning Fundamentals", "Psychology 101", "#3B82F6", "READING", 1)
    q.insert("t1_3", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Supply & Demand Basics", "Economics 101", "#F97316", "LECTURE", 1)
    q.insert("t1_4", "week_1", 1, "Introduction & Study Methods", "Jan 6 - Jan 12", 0, "Week 1 Review Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

    // Week 2 — mostly completed
    q.insert("t2_1", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Memory & Retention Models", "Psychology 101", "#3B82F6", "LECTURE", 1)
    q.insert("t2_2", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Organic Compound Structures", "Chemistry 201", "#22C55E", "LECTURE", 1)
    q.insert("t2_3", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Market Equilibrium Analysis", "Economics 101", "#F97316", "READING", 1)
    q.insert("t2_4", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Lab Report: Molecules", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
    q.insert("t2_5", "week_2", 2, "Core Concepts Deep Dive", "Jan 13 - Jan 19", 0, "Concepts Quiz", "Psychology 101", "#3B82F6", "QUIZ", 1)

    // Week 3
    q.insert("t3_1", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Behavioral Economics Intro", "Economics 101", "#F97316", "LECTURE", 1)
    q.insert("t3_2", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Chapter 4: Decision Making", "Psychology 101", "#3B82F6", "READING", 1)
    q.insert("t3_3", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Reaction Kinetics Lab", "Chemistry 201", "#22C55E", "ASSIGNMENT", 1)
    q.insert("t3_4", "week_3", 3, "Applied Frameworks", "Jan 20 - Jan 26", 0, "Applied Frameworks Quiz", "Economics 101", "#F97316", "QUIZ", 0)

    // Week 4 — current week, partially done
    q.insert("t4_1", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Review: Psych Chapters 1-4", "Psychology 101", "#3B82F6", "READING", 1)
    q.insert("t4_2", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Thermodynamics Lecture", "Chemistry 201", "#22C55E", "LECTURE", 1)
    q.insert("t4_3", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Macro vs Micro Economics", "Economics 101", "#F97316", "LECTURE", 0)
    q.insert("t4_4", "week_4", 4, "Midterm Preparation", "Jan 27 - Feb 2", 1, "Practice Midterm Exam", "Psychology 101", "#3B82F6", "QUIZ", 0)

    // Week 5 — not started
    q.insert("t5_1", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Neuroplasticity & Learning", "Psychology 101", "#3B82F6", "LECTURE", 0)
    q.insert("t5_2", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Electrochemistry Basics", "Chemistry 201", "#22C55E", "LECTURE", 0)
    q.insert("t5_3", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "International Trade Theory", "Economics 101", "#F97316", "READING", 0)
    q.insert("t5_4", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Chem Lab: Electrochemistry", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
    q.insert("t5_5", "week_5", 5, "Advanced Topics", "Feb 3 - Feb 9", 0, "Advanced Topics Quiz", "Psychology 101", "#3B82F6", "QUIZ", 0)

    // Week 6 — not started
    q.insert("t6_1", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Comprehensive Review Session", "Psychology 101", "#3B82F6", "LECTURE", 0)
    q.insert("t6_2", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Final Lab Submission", "Chemistry 201", "#22C55E", "ASSIGNMENT", 0)
    q.insert("t6_3", "week_6", 6, "Final Review & Synthesis", "Feb 10 - Feb 16", 0, "Economics Policy Analysis", "Economics 101", "#F97316", "READING", 0)
}
```

**Step 3: Build to verify schema compiles**

Run: `./gradlew :composeApp:generateCommonMainMindTagDatabaseInterface`
Expected: BUILD SUCCESSFUL — SQLDelight generates `PlannerTaskEntityQueries`.

---

### Task 7: Rewire PlannerViewModel to use SQLDelight

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/planner/presentation/PlannerViewModel.kt`
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/di/Modules.kt`
- Rewrite: `composeApp/src/jvmTest/kotlin/io/diasjakupov/mindtag/feature/planner/presentation/PlannerViewModelTest.kt` (move from commonTest)

**Step 1: Rewrite PlannerViewModel**

```kotlin
package io.diasjakupov.mindtag.feature.planner.presentation

import androidx.lifecycle.viewModelScope
import app.cash.sqldelight.coroutines.asFlow
import app.cash.sqldelight.coroutines.mapToList
import io.diasjakupov.mindtag.core.mvi.MviViewModel
import io.diasjakupov.mindtag.core.util.Logger
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.local.PlannerTaskEntity
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Effect
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.Intent
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTask
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.PlannerTaskType
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.State
import io.diasjakupov.mindtag.feature.planner.presentation.PlannerContract.WeekData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

class PlannerViewModel(
    private val db: MindTagDatabase,
) : MviViewModel<State, Intent, Effect>(State()) {

    override val tag = "PlannerVM"

    init {
        loadTasks()
    }

    private fun loadTasks() {
        db.plannerTaskEntityQueries.selectAll()
            .asFlow()
            .mapToList(Dispatchers.IO)
            .onEach { entities ->
                val weeks = buildWeeksFromEntities(entities)
                val allTasks = entities.size
                val completedTasks = entities.count { it.is_completed != 0L }
                val overallProgress = if (allTasks == 0) 0f else completedTasks.toFloat() / allTasks

                val currentWeekId = entities.firstOrNull { it.is_current_week != 0L }?.week_id
                val expandedId = state.value.expandedWeekId ?: currentWeekId

                updateState {
                    copy(
                        weeks = weeks,
                        overallProgress = overallProgress,
                        expandedWeekId = expandedId,
                        isLoading = false,
                    )
                }
            }
            .launchIn(viewModelScope)
    }

    override fun onIntent(intent: Intent) {
        Logger.d(tag, "onIntent: $intent")
        when (intent) {
            is Intent.SwitchView -> updateState { copy(viewMode = intent.mode) }
            is Intent.ToggleWeek -> {
                val newExpanded = if (state.value.expandedWeekId == intent.weekId) null else intent.weekId
                updateState { copy(expandedWeekId = newExpanded) }
            }
            is Intent.ToggleTask -> {
                viewModelScope.launch(Dispatchers.IO) {
                    db.plannerTaskEntityQueries.toggleCompleted(intent.taskId)
                }
            }
        }
    }

    private fun buildWeeksFromEntities(entities: List<PlannerTaskEntity>): List<WeekData> {
        return entities.groupBy { it.week_id }.map { (weekId, tasks) ->
            val first = tasks.first()
            val completedCount = tasks.count { it.is_completed != 0L }
            val progress = if (tasks.isEmpty()) 0f else completedCount.toFloat() / tasks.size
            WeekData(
                id = weekId,
                weekNumber = first.week_number.toInt(),
                title = first.week_title,
                dateRange = first.week_date_range,
                progress = progress,
                isCurrentWeek = first.is_current_week != 0L,
                tasks = tasks.map { entity ->
                    PlannerTask(
                        id = entity.id,
                        title = entity.title,
                        subjectName = entity.subject_name,
                        subjectColorHex = entity.subject_color_hex,
                        type = PlannerTaskType.valueOf(entity.type),
                        isCompleted = entity.is_completed != 0L,
                    )
                },
            )
        }.sortedBy { it.weekNumber }
    }
}
```

**Step 2: Add isLoading to PlannerContract.State**

In `PlannerContract.kt`, add:

```kotlin
data class State(
    val viewMode: ViewMode = ViewMode.LIST,
    val weeks: List<WeekData> = emptyList(),
    val expandedWeekId: String? = null,
    val overallProgress: Float = 0f,
    val isLoading: Boolean = true,
)
```

**Step 3: Update Koin DI**

In `Modules.kt`, change:

```kotlin
viewModel { PlannerViewModel() }
```

to:

```kotlin
viewModel { PlannerViewModel(get()) }
```

**Step 4: Rewrite PlannerViewModelTest (move to jvmTest)**

Delete `composeApp/src/commonTest/kotlin/io/diasjakupov/mindtag/feature/planner/presentation/PlannerViewModelTest.kt`.

Create `composeApp/src/jvmTest/kotlin/io/diasjakupov/mindtag/feature/planner/presentation/PlannerViewModelTest.kt`:

```kotlin
package io.diasjakupov.mindtag.feature.planner.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
import io.diasjakupov.mindtag.data.seed.SeedData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertFalse
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class PlannerViewModelTest {

    private lateinit var db: MindTagDatabase
    private lateinit var viewModel: PlannerViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        db = MindTagDatabase(driver)
        db.transaction { SeedData.populate(db) }
        viewModel = PlannerViewModel(db)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    private suspend fun awaitLoadedState(): PlannerContract.State {
        var result: PlannerContract.State? = null
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) state = awaitItem()
            result = state
            cancelAndConsumeRemainingEvents()
        }
        return result!!
    }

    @Test
    fun initialStateHasListViewModeAndWeeks() = runTest {
        val state = awaitLoadedState()
        assertEquals(PlannerContract.ViewMode.LIST, state.viewMode)
        assertTrue(state.weeks.isNotEmpty())
    }

    @Test
    fun initialStateContainsSixWeeks() = runTest {
        val state = awaitLoadedState()
        assertEquals(6, state.weeks.size)
    }

    @Test
    fun switchViewToCalendar() = runTest {
        awaitLoadedState()
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))
        assertEquals(PlannerContract.ViewMode.CALENDAR, viewModel.state.value.viewMode)
    }

    @Test
    fun switchViewBackToList() = runTest {
        awaitLoadedState()
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.CALENDAR))
        viewModel.onIntent(PlannerContract.Intent.SwitchView(PlannerContract.ViewMode.LIST))
        assertEquals(PlannerContract.ViewMode.LIST, viewModel.state.value.viewMode)
    }

    @Test
    fun toggleWeekExpandsNewWeek() = runTest {
        awaitLoadedState()
        viewModel.onIntent(PlannerContract.Intent.ToggleWeek("week_1"))
        assertEquals("week_1", viewModel.state.value.expandedWeekId)
    }

    @Test
    fun toggleWeekCollapsesCurrentlyExpandedWeek() = runTest {
        awaitLoadedState()
        val currentExpanded = viewModel.state.value.expandedWeekId!!
        viewModel.onIntent(PlannerContract.Intent.ToggleWeek(currentExpanded))
        assertNull(viewModel.state.value.expandedWeekId)
    }

    @Test
    fun week1HasAllTasksCompleted() = runTest {
        val state = awaitLoadedState()
        val week1 = state.weeks.first { it.id == "week_1" }
        assertTrue(week1.tasks.all { it.isCompleted })
        assertEquals(1.0f, week1.progress)
    }

    @Test
    fun currentWeekIsWeek4() = runTest {
        val state = awaitLoadedState()
        val currentWeek = state.weeks.first { it.isCurrentWeek }
        assertEquals("week_4", currentWeek.id)
        assertEquals(4, currentWeek.weekNumber)
    }

    @Test
    fun weekTasksHaveCorrectTypes() = runTest {
        val state = awaitLoadedState()
        val week1 = state.weeks.first { it.id == "week_1" }
        val types = week1.tasks.map { it.type }
        assertTrue(PlannerContract.PlannerTaskType.LECTURE in types)
        assertTrue(PlannerContract.PlannerTaskType.READING in types)
        assertTrue(PlannerContract.PlannerTaskType.QUIZ in types)
    }

    @Test
    fun toggleTaskPersistsToDatabase() = runTest {
        awaitLoadedState()
        // Find an incomplete task
        val week4 = viewModel.state.value.weeks.first { it.id == "week_4" }
        val incompleteTask = week4.tasks.first { !it.isCompleted }

        viewModel.onIntent(PlannerContract.Intent.ToggleTask(incompleteTask.id))

        // Wait for DB update to propagate
        viewModel.state.test {
            val state = awaitItem()
            val updatedWeek = state.weeks.first { it.id == "week_4" }
            val updatedTask = updatedWeek.tasks.first { it.id == incompleteTask.id }
            assertTrue(updatedTask.isCompleted)
            cancelAndConsumeRemainingEvents()
        }
    }
}
```

**Step 5: Run all tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

---

### Task 8: Platform verification and final build

**Step 1: Run full test suite**

Run: `./gradlew :composeApp:jvmTest`
Expected: All tests pass.

**Step 2: Build Android**

Run: `./gradlew :composeApp:assembleDebug`
Expected: BUILD SUCCESSFUL

**Step 3: Run Desktop**

Run: `./gradlew :composeApp:run`
Expected: App launches. Verify:
- Home Dashboard shows data
- Library List + Graph work
- Create a note → see it in Library → check Graph for new edges
- Study Hub → start quiz → answer → results
- Profile shows real stats
- Planner checkboxes persist across tab switches

**Step 4: Manual smoke test flow**

Execute the demo flow:
1. Open app → Onboarding → Get Started
2. Home → tap review card → Note Detail → tap related note
3. Library → Graph view → tap node → preview card
4. Library → Create Note (title: "Newton's Laws of Motion", content: 3+ sentences about physics) → Save
5. Library → Graph view → verify new node appeared with edges
6. Study Hub → Quick Quiz → answer questions → Results
7. Profile → verify Total Notes incremented
8. Planner → check a task → verify progress updates → switch tabs → come back → still checked
