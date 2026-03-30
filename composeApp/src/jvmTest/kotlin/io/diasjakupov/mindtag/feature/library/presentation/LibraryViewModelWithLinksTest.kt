package io.diasjakupov.mindtag.feature.library.presentation

import app.cash.turbine.test
import io.diasjakupov.mindtag.test.FakeNoteRepository
import io.diasjakupov.mindtag.test.TestData
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
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelWithLinksTest {

    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())

        val fakeNoteRepository = FakeNoteRepository()
        fakeNoteRepository.setNotes(TestData.notes)
        fakeNoteRepository.setSubjects(TestData.subjects)

        viewModel = LibraryViewModel(fakeNoteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialLoad_isNotLoading() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertFalse(state.isLoading)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialLoad_notesPopulatedFromRepository() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TestData.notes.size, state.notes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialLoad_graphNodesBuiltFromNotes() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            // One graph node per note
            assertEquals(TestData.notes.size, state.graphNodes.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialLoad_graphNodeLabelsMatchNoteTitlesOrSubjectNames() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            // Hub nodes use subject name; satellite nodes use note title (truncated)
            val nodeLabels = state.graphNodes.map { it.label }.toSet()
            // Every note should appear either as a hub (subject name) or satellite (title)
            val subjectNames = TestData.subjects.map { it.name }.toSet()
            val satelliteTitles = state.graphNodes.filter { !it.isHub }.map { it.label }.toSet()
            val noteTitles = TestData.notes.map { it.title.take(18) }.toSet()
            // Hub labels are subject names
            assertTrue(nodeLabels.containsAll(subjectNames))
            // Satellite labels are note title prefixes
            assertTrue(noteTitles.containsAll(satelliteTitles))
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun initialLoad_subjectsPopulated() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(TestData.subjects.size, state.subjects.size)
            cancelAndIgnoreRemainingEvents()
        }
    }

    @Test
    fun selectSubjectFilter_filtersNoteList() = runTest {
        viewModel.state.test {
            awaitItem() // initial load

            viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter(TestData.mathSubject.id))

            val filtered = awaitItem()
            assertTrue(filtered.notes.all { it.subjectName.isNotEmpty() })
            cancelAndIgnoreRemainingEvents()
        }
    }
}
