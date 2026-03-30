package io.diasjakupov.mindtag.feature.library.presentation

import app.cash.turbine.test
import io.diasjakupov.mindtag.test.FakeNoteRepository
import io.diasjakupov.mindtag.test.TestData
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.TestCoroutineScheduler
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceTimeBy
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
class LibraryViewModelTest {

    private val testScheduler = TestCoroutineScheduler()
    private val testDispatcher = UnconfinedTestDispatcher(testScheduler)

    private lateinit var fakeNoteRepository: FakeNoteRepository
    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(testDispatcher)
        fakeNoteRepository = FakeNoteRepository()
        fakeNoteRepository.setNotes(TestData.notes)
        fakeNoteRepository.setSubjects(TestData.subjects)
        viewModel = LibraryViewModel(fakeNoteRepository)
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    /** Helper: waits for state to be loaded (isLoading == false). */
    private suspend fun awaitLoadedState(): LibraryContract.State {
        var result: LibraryContract.State? = null
        viewModel.state.test {
            var state = awaitItem()
            if (state.isLoading) {
                state = awaitItem()
            }
            result = state
            cancelAndConsumeRemainingEvents()
        }
        return result!!
    }

    @Test
    fun initialStateHasListViewModeAndLoadsNotes() = runTest(testScheduler) {
        val state = awaitLoadedState()
        assertEquals(LibraryContract.ViewMode.LIST, state.viewMode)
        assertFalse(state.isLoading)
        assertEquals(3, state.notes.size)
    }

    @Test
    fun initialStateLoadedSubjectFilters() = runTest(testScheduler) {
        val state = awaitLoadedState()
        assertEquals(2, state.subjects.size)
        assertEquals("Mathematics", state.subjects[0].name)
        assertEquals("Physics", state.subjects[1].name)
        assertFalse(state.subjects[0].isSelected)
        assertFalse(state.subjects[1].isSelected)
    }

    @Test
    fun initialStateHasEmptySearchQuery() = runTest(testScheduler) {
        val state = awaitLoadedState()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun initialStateBuildsGraphNodes() = runTest(testScheduler) {
        val state = awaitLoadedState()
        assertEquals(3, state.graphNodes.size)
    }

    @Test
    fun initialStateHasEmptyGraphEdgesWithNoLinks() = runTest(testScheduler) {
        val state = awaitLoadedState()
        assertTrue(state.graphEdges.isEmpty())
    }

    @Test
    fun switchViewToGraph() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SwitchView(LibraryContract.ViewMode.GRAPH))
        assertEquals(LibraryContract.ViewMode.GRAPH, viewModel.state.value.viewMode)
    }

    @Test
    fun switchViewClearsSelectedNode() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(1L))
        assertEquals(1L, viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.SwitchView(LibraryContract.ViewMode.LIST))
        assertNull(viewModel.state.value.selectedNodeId)
    }

    @Test
    fun searchFiltersByTitle() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))
        advanceTimeBy(500)

        val state = viewModel.state.value
        assertEquals("Linear", state.searchQuery)
        assertEquals(1, state.notes.size)
        assertEquals("Linear Algebra Basics", state.notes.first().title)
    }

    @Test
    fun searchIsCaseInsensitive() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("calculus"))
        advanceTimeBy(500)

        val state = viewModel.state.value
        assertEquals(1, state.notes.size)
        assertEquals("Calculus Fundamentals", state.notes.first().title)
    }

    @Test
    fun emptySearchReturnsAllNotes() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))
        advanceTimeBy(500)
        assertEquals(1, viewModel.state.value.notes.size)

        viewModel.onIntent(LibraryContract.Intent.Search(""))
        advanceTimeBy(500)
        assertEquals(3, viewModel.state.value.notes.size)
    }

    @Test
    fun selectSubjectFilterFiltersNotes() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))

        val state = viewModel.state.value
        assertEquals("subj-1", state.selectedSubjectId)
        assertEquals(2, state.notes.size)
        assertTrue(state.notes.all { it.subjectName == "Mathematics" })
    }

    @Test
    fun selectSameSubjectFilterTogglesOff() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))
        assertEquals("subj-1", viewModel.state.value.selectedSubjectId)

        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))
        assertNull(viewModel.state.value.selectedSubjectId)
        assertEquals(3, viewModel.state.value.notes.size)
    }

    @Test
    fun selectSubjectFilterUpdatesIsSelectedFlag() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-2"))

        val subjects = viewModel.state.value.subjects
        assertFalse(subjects.first { it.id == "subj-1" }.isSelected)
        assertTrue(subjects.first { it.id == "subj-2" }.isSelected)
    }

    @Test
    fun tapNoteEmitsNavigateToNoteEffect() = runTest(testScheduler) {
        viewModel.effect.test {
            viewModel.onIntent(LibraryContract.Intent.TapNote(1L))
            assertEquals(LibraryContract.Effect.NavigateToNote(1L), awaitItem())
        }
    }

    @Test
    fun tapGraphNodeSelectsNode() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(1L))
        assertEquals(1L, viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapSameGraphNodeDeselectsNode() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(1L))
        assertEquals(1L, viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(1L))
        assertNull(viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapDifferentGraphNodeSwitchesSelection() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(1L))
        assertEquals(1L, viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.TapGraphNode(2L))
        assertEquals(2L, viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapCreateNoteEmitsNavigateToCreateNoteEffect() = runTest(testScheduler) {
        viewModel.effect.test {
            viewModel.onIntent(LibraryContract.Intent.TapCreateNote)
            assertEquals(LibraryContract.Effect.NavigateToCreateNote, awaitItem())
        }
    }

    @Test
    fun noteListItemsContainCorrectSubjectInfo() = runTest(testScheduler) {
        val state = awaitLoadedState()
        val algebraItem = state.notes.first { it.id == 1L }
        assertEquals("Mathematics", algebraItem.subjectName)
        assertEquals("#FF5733", algebraItem.subjectColorHex)
        assertEquals(1, algebraItem.weekNumber)
        assertEquals(5, algebraItem.readTimeMinutes)
    }

    @Test
    fun searchAndSubjectFilterCombine() = runTest(testScheduler) {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-2"))
        assertEquals(1, viewModel.state.value.notes.size)

        viewModel.onIntent(LibraryContract.Intent.Search("Newtonian"))
        advanceTimeBy(500)
        assertEquals(1, viewModel.state.value.notes.size)
        assertEquals("Newtonian Mechanics", viewModel.state.value.notes.first().title)

        // Text search does not combine with subject filter — "Linear" matches a Math note
        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))
        advanceTimeBy(500)
        assertEquals(1, viewModel.state.value.notes.size)
    }

    @Test
    fun graphNodeLabelsAreTruncatedTo20Chars() = runTest(testScheduler) {
        val state = awaitLoadedState()
        state.graphNodes.forEach { node ->
            assertTrue(node.label.length <= 20)
        }
    }

    @Test
    fun graphNodesHaveCorrectSubjectColors() = runTest(testScheduler) {
        val state = awaitLoadedState()
        val mathNodes = state.graphNodes.filter { node ->
            state.notes.filter { it.subjectName == "Mathematics" }.any { it.id == node.noteId }
        }
        mathNodes.forEach { node ->
            assertEquals("#FF5733", node.subjectColorHex)
        }
    }
}
