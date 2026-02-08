package io.diasjakupov.mindtag.feature.profile.presentation

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
class ProfileViewModelTest {

    private lateinit var viewModel: ProfileViewModel

    @BeforeTest
    fun setup() {
        Dispatchers.setMain(UnconfinedTestDispatcher())
        viewModel = ProfileViewModel()
    }

    @AfterTest
    fun tearDown() {
        Dispatchers.resetMain()
    }

    @Test
    fun initialStateHasDefaultProfileValues() = runTest {
        viewModel.state.test {
            val state = awaitItem()
            assertEquals("Alex Johnson", state.userName)
            assertEquals("alex.johnson@university.edu", state.email)
            assertEquals(15, state.totalNotes)
            assertEquals(8, state.totalStudySessions)
            assertEquals(7, state.currentStreak)
            assertEquals(4030, state.totalXp)
            assertEquals("January 2026", state.memberSince)
        }
    }

    @Test
    fun onIntentDoesNotChangeState() = runTest {
        val stateBefore = viewModel.state.value

        viewModel.onIntent(ProfileContract.Intent.TapEditProfile)
        viewModel.onIntent(ProfileContract.Intent.TapNotifications)
        viewModel.onIntent(ProfileContract.Intent.TapAppearance)
        viewModel.onIntent(ProfileContract.Intent.TapAbout)
        viewModel.onIntent(ProfileContract.Intent.TapLogout)

        assertEquals(stateBefore, viewModel.state.value)
    }
}
