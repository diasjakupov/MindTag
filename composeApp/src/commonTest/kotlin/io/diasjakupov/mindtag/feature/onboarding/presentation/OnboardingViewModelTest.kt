package io.diasjakupov.mindtag.feature.onboarding.presentation

import app.cash.turbine.test
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

@OptIn(ExperimentalCoroutinesApi::class)
class OnboardingViewModelTest {

    private lateinit var viewModel: OnboardingViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = OnboardingViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasPageZeroAndFourTotalPages() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals(0, state.currentPage)
            assertEquals(4, state.totalPages)
        }
    }

    @Test
    fun nextPageIncrementsCurrentPage() = runTest {
        viewModel.onIntent(OnboardingContract.Intent.NextPage)

        viewModel.state.test {
            assertEquals(1, awaitItem().currentPage)
        }
    }

    @Test
    fun nextPageDoesNotExceedLastPage() = runTest {
        // Navigate to the last page
        repeat(3) { viewModel.onIntent(OnboardingContract.Intent.NextPage) }
        assertEquals(3, viewModel.state.value.currentPage)

        // Try going past the last page
        viewModel.onIntent(OnboardingContract.Intent.NextPage)
        assertEquals(3, viewModel.state.value.currentPage)
    }

    @Test
    fun previousPageDecrementsCurrentPage() = runTest {
        viewModel.onIntent(OnboardingContract.Intent.NextPage)
        viewModel.onIntent(OnboardingContract.Intent.NextPage)
        assertEquals(2, viewModel.state.value.currentPage)

        viewModel.onIntent(OnboardingContract.Intent.PreviousPage)
        assertEquals(1, viewModel.state.value.currentPage)
    }

    @Test
    fun previousPageDoesNotGoBelowZero() = runTest {
        assertEquals(0, viewModel.state.value.currentPage)

        viewModel.onIntent(OnboardingContract.Intent.PreviousPage)
        assertEquals(0, viewModel.state.value.currentPage)
    }

    @Test
    fun skipEmitsNavigateToHomeEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(OnboardingContract.Intent.Skip)
            assertEquals(OnboardingContract.Effect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun getStartedEmitsNavigateToHomeEffect() = runTest {
        viewModel.effect.test {
            viewModel.onIntent(OnboardingContract.Intent.GetStarted)
            assertEquals(OnboardingContract.Effect.NavigateToHome, awaitItem())
        }
    }

    @Test
    fun updatePageFromPagerSetsCurrentPage() = runTest {
        viewModel.updatePageFromPager(2)
        assertEquals(2, viewModel.state.value.currentPage)
    }
}
