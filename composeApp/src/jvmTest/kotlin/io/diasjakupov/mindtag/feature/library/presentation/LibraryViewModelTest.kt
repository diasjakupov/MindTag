package io.diasjakupov.mindtag.feature.library.presentation

import app.cash.sqldelight.driver.jdbc.sqlite.JdbcSqliteDriver
import app.cash.turbine.test
import io.diasjakupov.mindtag.data.local.MindTagDatabase
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
import kotlin.test.assertNull
import kotlin.test.assertTrue

@OptIn(ExperimentalCoroutinesApi::class)
class LibraryViewModelTest {

    private lateinit var fakeNoteRepository: FakeNoteRepository
    private lateinit var db: MindTagDatabase
    private lateinit var viewModel: LibraryViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        fakeNoteRepository = FakeNoteRepository()
        fakeNoteRepository.setNotes(TestData.notes)
        fakeNoteRepository.setSubjects(TestData.subjects)

        val driver = JdbcSqliteDriver(JdbcSqliteDriver.IN_MEMORY)
        MindTagDatabase.Schema.create(driver)
        db = MindTagDatabase(driver)

        viewModel = LibraryViewModel(fakeNoteRepository, db)
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
    fun initialStateHasListViewModeAndLoadsNotes() = runTest {
        val state = awaitLoadedState()
        assertEquals(LibraryContract.ViewMode.LIST, state.viewMode)
        assertFalse(state.isLoading)
        assertEquals(3, state.notes.size)
    }

    @Test
    fun initialStateLoadedSubjectFilters() = runTest {
        val state = awaitLoadedState()
        assertEquals(2, state.subjects.size)
        assertEquals("Mathematics", state.subjects[0].name)
        assertEquals("Physics", state.subjects[1].name)
        assertFalse(state.subjects[0].isSelected)
        assertFalse(state.subjects[1].isSelected)
    }

    @Test
    fun initialStateHasEmptySearchQuery() = runTest {
        val state = awaitLoadedState()
        assertEquals("", state.searchQuery)
    }

    @Test
    fun initialStateBuildsGraphNodes() = runTest {
        val state = awaitLoadedState()
        assertEquals(3, state.graphNodes.size)
    }

    @Test
    fun initialStateHasEmptyGraphEdgesWithNoLinks() = runTest {
        val state = awaitLoadedState()
        assertTrue(state.graphEdges.isEmpty())
    }

    @Test
    fun switchViewToGraph() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SwitchView(LibraryContract.ViewMode.GRAPH))
        assertEquals(LibraryContract.ViewMode.GRAPH, viewModel.state.value.viewMode)
    }

    @Test
    fun switchViewClearsSelectedNode() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-1"))
        assertEquals("note-1", viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.SwitchView(LibraryContract.ViewMode.LIST))
        assertNull(viewModel.state.value.selectedNodeId)
    }

    @Test
    fun searchFiltersByTitle() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))

        val state = viewModel.state.value
        assertEquals("Linear", state.searchQuery)
        assertEquals(1, state.notes.size)
        assertEquals("Linear Algebra Basics", state.notes.first().title)
    }

    @Test
    fun searchIsCaseInsensitive() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("calculus"))

        val state = viewModel.state.value
        assertEquals(1, state.notes.size)
        assertEquals("Calculus Fundamentals", state.notes.first().title)
    }

    @Test
    fun emptySearchReturnsAllNotes() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))
        assertEquals(1, viewModel.state.value.notes.size)

        viewModel.onIntent(LibraryContract.Intent.Search(""))
        assertEquals(3, viewModel.state.value.notes.size)
    }

    @Test
    fun selectSubjectFilterFiltersNotes() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))

        val state = viewModel.state.value
        assertEquals("subj-1", state.selectedSubjectId)
        assertEquals(2, state.notes.size)
        assertTrue(state.notes.all { it.subjectName == "Mathematics" })
    }

    @Test
    fun selectSameSubjectFilterTogglesOff() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))
        assertEquals("subj-1", viewModel.state.value.selectedSubjectId)

        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-1"))
        assertNull(viewModel.state.value.selectedSubjectId)
        assertEquals(3, viewModel.state.value.notes.size)
    }

    @Test
    fun selectSubjectFilterUpdatesIsSelectedFlag() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-2"))

        val subjects = viewModel.state.value.subjects
        assertFalse(subjects.first { it.id == "subj-1" }.isSelected)
        assertTrue(subjects.first { it.id == "subj-2" }.isSelected)
    }

    @Test
    fun tapNoteEmitsNavigateToNoteEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(LibraryContract.Intent.TapNote("note-1"))
            assertEquals(LibraryContract.Effect.NavigateToNote("note-1"), awaitItem())
        }
    }

    @Test
    fun tapGraphNodeSelectsNode() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-1"))
        assertEquals("note-1", viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapSameGraphNodeDeselectsNode() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-1"))
        assertEquals("note-1", viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-1"))
        assertNull(viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapDifferentGraphNodeSwitchesSelection() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-1"))
        assertEquals("note-1", viewModel.state.value.selectedNodeId)

        viewModel.onIntent(LibraryContract.Intent.TapGraphNode("note-2"))
        assertEquals("note-2", viewModel.state.value.selectedNodeId)
    }

    @Test
    fun tapCreateNoteEmitsNavigateToCreateNoteEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(LibraryContract.Intent.TapCreateNote)
            assertEquals(LibraryContract.Effect.NavigateToCreateNote, awaitItem())
        }
    }

    @Test
    fun noteListItemsContainCorrectSubjectInfo() = runTest {
        val state = awaitLoadedState()
        val algebraItem = state.notes.first { it.id == "note-1" }
        assertEquals("Mathematics", algebraItem.subjectName)
        assertEquals("#FF5733", algebraItem.subjectColorHex)
        assertEquals(1, algebraItem.weekNumber)
        assertEquals(5, algebraItem.readTimeMinutes)
    }

    @Test
    fun searchAndSubjectFilterCombine() = runTest {
        awaitLoadedState()
        viewModel.onIntent(LibraryContract.Intent.SelectSubjectFilter("subj-2"))
        assertEquals(1, viewModel.state.value.notes.size)

        viewModel.onIntent(LibraryContract.Intent.Search("Newtonian"))
        assertEquals(1, viewModel.state.value.notes.size)
        assertEquals("Newtonian Mechanics", viewModel.state.value.notes.first().title)

        viewModel.onIntent(LibraryContract.Intent.Search("Linear"))
        assertEquals(0, viewModel.state.value.notes.size)
    }

    @Test
    fun graphNodeLabelsAreTruncatedTo18Chars() = runTest {
        val state = awaitLoadedState()
        state.graphNodes.forEach { node ->
            assertTrue(node.label.length <= 18)
        }
    }

    @Test
    fun graphNodesHaveCorrectSubjectColors() = runTest {
        val state = awaitLoadedState()
        val mathNodes = state.graphNodes.filter { node ->
            state.notes.filter { it.subjectName == "Mathematics" }.any { it.id == node.noteId }
        }
        mathNodes.forEach { node ->
            assertEquals("#FF5733", node.subjectColorHex)
        }
    }
}
