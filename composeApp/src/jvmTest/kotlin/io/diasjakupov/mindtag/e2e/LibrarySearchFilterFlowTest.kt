package io.diasjakupov.mindtag.e2e

import io.diasjakupov.mindtag.core.domain.model.Subject
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetNotesUseCase
import io.diasjakupov.mindtag.feature.notes.domain.usecase.GetSubjectsUseCase
import io.diasjakupov.mindtag.test.FakeNoteRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.resetMain
import kotlinx.coroutines.test.runTest
import kotlinx.coroutines.test.setMain
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibrarySearchFilterFlowTest {

    private lateinit var fakeNoteRepository: FakeNoteRepository
    private lateinit var getNotesUseCase: GetNotesUseCase
    private lateinit var getSubjectsUseCase: GetSubjectsUseCase

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeNoteRepository = FakeNoteRepository()
        fakeNoteRepository.setSubjects(SUBJECTS)
        fakeNoteRepository.setNotes(NOTES)
        getNotesUseCase = GetNotesUseCase(fakeNoteRepository)
        getSubjectsUseCase = GetSubjectsUseCase(fakeNoteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun loadAllNotes_returns15Notes() = runTest {
        val notes = getNotesUseCase()
        assertEquals(15, notes.size)
    }

    @Test
    fun filterByBiology_returns5Notes() = runTest {
        val notes = getNotesUseCase(subjectFilter = BIO)
        assertEquals(5, notes.size)
        assertTrue(notes.all { it.subjectId == BIO })
    }

    @Test
    fun filterByEconomics_returns5Notes() = runTest {
        val notes = getNotesUseCase(subjectFilter = ECON)
        assertEquals(5, notes.size)
        assertTrue(notes.all { it.subjectId == ECON })
    }

    @Test
    fun filterByComputerScience_returns5Notes() = runTest {
        val notes = getNotesUseCase(subjectFilter = CS)
        assertEquals(5, notes.size)
        assertTrue(notes.all { it.subjectId == CS })
    }

    @Test
    fun filterByNonExistentSubject_returnsEmpty() = runTest {
        val notes = getNotesUseCase(subjectFilter = "subj-nonexistent")
        assertTrue(notes.isEmpty())
    }

    @Test
    fun searchByTitle_matchesCorrectNotes() = runTest {
        val allNotes = getNotesUseCase()
        val query = "Binary"
        val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Binary Search Trees", filtered[0].title)
    }

    @Test
    fun searchByTitle_caseInsensitive() = runTest {
        val allNotes = getNotesUseCase()
        val query = "dna"
        val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertTrue(filtered[0].title.contains("DNA", ignoreCase = true))
    }

    @Test
    fun searchWithSubjectFilter_combinesFilters() = runTest {
        val csNotes = getNotesUseCase(subjectFilter = CS)
        assertEquals(5, csNotes.size)

        val query = "Sort"
        val filtered = csNotes.filter { it.title.contains(query, ignoreCase = true) }
        assertEquals(1, filtered.size)
        assertEquals("Sorting Algorithms Compared", filtered[0].title)
    }

    @Test
    fun loadSubjects_returns3Subjects() = runTest {
        val subjects = getSubjectsUseCase()
        assertEquals(3, subjects.size)
    }

    @Test
    fun subjects_orderedAlphabeticallyByName() = runTest {
        val subjects = getSubjectsUseCase()
        assertEquals("Biology 101", subjects[0].name)
        assertEquals("Computer Science", subjects[1].name)
        assertEquals("Economics 101", subjects[2].name)
    }

    @Test
    fun notesOrderedByUpdatedAtDescending() = runTest {
        val notes = getNotesUseCase()
        for (i in 0 until notes.size - 1) {
            assertTrue(
                notes[i].updatedAt >= notes[i + 1].updatedAt,
                "Notes should be ordered by updatedAt descending: index $i (${notes[i].updatedAt}) < ${i+1} (${notes[i+1].updatedAt})"
            )
        }
    }

    @Test
    fun searchNoMatch_returnsEmpty() = runTest {
        val allNotes = getNotesUseCase()
        val query = "zzzzNonExistentTopic"
        val filtered = allNotes.filter { it.title.contains(query, ignoreCase = true) }
        assertTrue(filtered.isEmpty())
    }

    @Test
    fun emptySearch_returnsAllNotes() = runTest {
        val allNotes = getNotesUseCase()
        val query = ""
        val filtered = allNotes.filter { query.isBlank() || it.title.contains(query, ignoreCase = true) }
        assertEquals(15, filtered.size)
    }

    // ── Test fixtures ─────────────────────────────────────────────────────────

    companion object {
        private const val BIO = "subj-bio-101"
        private const val ECON = "subj-econ-101"
        private const val CS = "subj-cs-101"

        // Subjects ordered alphabetically (Biology, Computer Science, Economics)
        val SUBJECTS = listOf(
            Subject(id = BIO,  name = "Biology 101",      colorHex = "#22C55E", iconName = "leaf"),
            Subject(id = CS,   name = "Computer Science", colorHex = "#135BEC", iconName = "code"),
            Subject(id = ECON, name = "Economics 101",    colorHex = "#F59E0B", iconName = "trending_up"),
        )

        // 15 notes ordered by updatedAt descending (most recent first)
        val NOTES: List<Note> = buildList {
            var ts = 1738886400000L // base timestamp, decreasing per note
            fun note(id: Long, title: String, subjectId: String): Note {
                val note = Note(
                    id = id, title = title, content = "Content for $title",
                    summary = "Summary", subjectId = subjectId,
                    weekNumber = null, readTimeMinutes = 5,
                    createdAt = ts, updatedAt = ts,
                )
                ts -= 3_600_000L // each note 1 hour older
                return note
            }
            // Biology (5)
            add(note(1L,  "Cell Division and Mitosis",      BIO))
            add(note(2L,  "DNA Replication",                BIO))
            add(note(3L,  "Photosynthesis",                 BIO))
            add(note(4L,  "Krebs Cycle",                    BIO))
            add(note(5L,  "Evolution and Natural Selection", BIO))
            // Computer Science (5)
            add(note(6L,  "Binary Search Trees",            CS))
            add(note(7L,  "Big-O Notation",                 CS))
            add(note(8L,  "Graph Algorithms",               CS))
            add(note(9L,  "Sorting Algorithms Compared",    CS))
            add(note(10L, "Dynamic Programming",            CS))
            // Economics (5)
            add(note(11L, "Supply and Demand",              ECON))
            add(note(12L, "GDP and Economic Growth",        ECON))
            add(note(13L, "Monetary Policy",                ECON))
            add(note(14L, "Market Structures",              ECON))
            add(note(15L, "International Trade",            ECON))
        }
    }
}
