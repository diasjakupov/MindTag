package io.diasjakupov.mindtag

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.material3.Scaffold
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
import io.diasjakupov.mindtag.core.navigation.MindTagNavigationRail
import androidx.compose.runtime.Composable
import androidx.compose.runtime.mutableStateListOf
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.runtime.snapshots.SnapshotStateList
import androidx.compose.ui.Modifier
import androidx.navigation3.runtime.NavKey
import androidx.navigation3.runtime.entryProvider
import androidx.navigation3.ui.NavDisplay
import androidx.compose.runtime.collectAsState
import androidx.lifecycle.viewmodel.navigation3.rememberViewModelStoreNavEntryDecorator
import androidx.navigation3.runtime.rememberSaveableStateHolderNavEntryDecorator
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.core.navigation.MindTagBottomBar
import io.diasjakupov.mindtag.core.navigation.Route
import io.diasjakupov.mindtag.core.network.AuthManager
import io.diasjakupov.mindtag.core.network.AuthState
import io.diasjakupov.mindtag.feature.auth.presentation.AuthScreen
import io.diasjakupov.mindtag.feature.library.presentation.LibraryScreen
import io.diasjakupov.mindtag.feature.notes.presentation.create.NoteCreateScreen
import io.diasjakupov.mindtag.feature.notes.presentation.detail.NoteDetailScreen
import io.diasjakupov.mindtag.feature.study.presentation.hub.StudyHubScreen
import io.diasjakupov.mindtag.feature.study.presentation.quiz.QuizScreen
import io.diasjakupov.mindtag.feature.study.presentation.results.ResultsScreen
import org.koin.compose.koinInject

private val topLevelRoutes: Set<Route> = setOf(
    Route.Library, Route.Study,
)

private val slideEnterTransition = NavDisplay.transitionSpec {
    slideInHorizontally(tween(300)) { it } + fadeIn(tween(300)) togetherWith
        slideOutHorizontally(tween(300)) { -it / 4 } + fadeOut(tween(300))
}

private val slidePopTransition = NavDisplay.popTransitionSpec {
    slideInHorizontally(tween(300)) { -it / 4 } + fadeIn(tween(300)) togetherWith
        slideOutHorizontally(tween(300)) { it } + fadeOut(tween(300))
}

private val pushScreenMetadata: Map<String, Any> = slideEnterTransition + slidePopTransition

private class TopLevelBackStack(startKey: Route) {
    private val topLevelStacks = linkedMapOf<Route, SnapshotStateList<NavKey>>(
        startKey to mutableStateListOf(startKey),
    )

    var topLevelKey by mutableStateOf(startKey)
        private set

    val backStack: SnapshotStateList<NavKey> = mutableStateListOf<NavKey>(startKey)

    private fun updateBackStack() {
        backStack.clear()
        backStack.addAll(topLevelStacks.flatMap { it.value })
    }

    fun selectTab(tab: Route) {
        if (topLevelStacks[tab] == null) {
            topLevelStacks[tab] = mutableStateListOf(tab)
        } else {
            topLevelStacks.apply {
                remove(tab)?.let { put(tab, it) }
            }
        }
        topLevelKey = tab
        updateBackStack()
    }

    fun push(key: NavKey) {
        topLevelStacks[topLevelKey]?.add(key)
        updateBackStack()
    }

    fun removeLast() {
        val currentStack = topLevelStacks[topLevelKey]
        if (currentStack != null && currentStack.size > 1) {
            currentStack.removeAt(currentStack.size - 1)
            updateBackStack()
        } else if (topLevelStacks.size > 1) {
            topLevelStacks.remove(topLevelKey)
            topLevelKey = topLevelStacks.keys.last()
            updateBackStack()
        }
    }
}

@Composable
fun App() {
    MindTagTheme {
        BoxWithConstraints {
            val windowSizeClass = when {
                maxWidth < 600.dp -> WindowSizeClass.Compact
                maxWidth <= 840.dp -> WindowSizeClass.Medium
                else -> WindowSizeClass.Expanded
            }

            CompositionLocalProvider(LocalWindowSizeClass provides windowSizeClass) {
                val authManager: AuthManager = koinInject()
                val authState by authManager.state.collectAsState()

                when (authState) {
                    is AuthState.Unauthenticated -> {
                        AuthScreen(onNavigateToHome = { /* handled by auth state change */ })
                    }
                    is AuthState.Authenticated -> {
                        MainApp()
                    }
                }
            }
        }
    }
}

@Composable
private fun MainApp() {
    val nav = remember { TopLevelBackStack(Route.Library) }
    val windowSizeClass = LocalWindowSizeClass.current

    val currentEntry = nav.backStack.lastOrNull()
    val showNav = currentEntry is Route && currentEntry in topLevelRoutes
    val isCompact = windowSizeClass == WindowSizeClass.Compact

    val navContent: @Composable (Modifier) -> Unit = { modifier ->
        NavDisplay(
            backStack = nav.backStack,
            onBack = { nav.removeLast() },
            modifier = modifier,
            entryDecorators = listOf(
                rememberSaveableStateHolderNavEntryDecorator(),
                rememberViewModelStoreNavEntryDecorator(),
            ),
            transitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            popTransitionSpec = {
                fadeIn(tween(200)) togetherWith fadeOut(tween(200))
            },
            entryProvider = entryProvider {
                entry<Route.Library> {
                    LibraryScreen(
                        onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) },
                        onNavigateToCreateNote = { nav.push(Route.NoteCreate()) },
                    )
                }
                entry<Route.Study> {
                    StudyHubScreen(
                        onNavigateToQuiz = { sessionId -> nav.push(Route.Quiz(sessionId)) },
                    )
                }
                entry<Route.NoteCreate>(metadata = pushScreenMetadata) { key ->
                    NoteCreateScreen(
                        noteId = key.noteId,
                        onNavigateBack = { nav.removeLast() },
                    )
                }
                entry<Route.NoteDetail>(metadata = pushScreenMetadata) { key ->
                    NoteDetailScreen(
                        noteId = key.noteId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToNote = { noteId -> nav.push(Route.NoteDetail(noteId)) },
                        onNavigateToEdit = { noteId -> nav.push(Route.NoteCreate(noteId)) },
                        onNavigateToQuiz = { sessionId -> nav.push(Route.Quiz(sessionId)) },
                    )
                }
                entry<Route.Quiz>(metadata = pushScreenMetadata) { key ->
                    QuizScreen(
                        sessionId = key.sessionId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToResults = { sessionId ->
                            nav.push(Route.QuizResults(sessionId))
                        },
                    )
                }
                entry<Route.QuizResults>(metadata = pushScreenMetadata) { key ->
                    ResultsScreen(
                        sessionId = key.sessionId,
                        onNavigateBack = { nav.removeLast() },
                        onNavigateToLibrary = { nav.selectTab(Route.Library) },
                    )
                }
            },
        )
    }

    if (isCompact) {
        Scaffold(
            bottomBar = {
                if (showNav) {
                    MindTagBottomBar(
                        currentRoute = nav.topLevelKey,
                        onTabSelected = { nav.selectTab(it) },
                    )
                }
            },
        ) { innerPadding ->
            navContent(Modifier.padding(innerPadding))
        }
    } else {
        Row(modifier = Modifier.fillMaxSize()) {
            if (showNav) {
                MindTagNavigationRail(
                    currentRoute = nav.topLevelKey,
                    onTabSelected = { nav.selectTab(it) },
                )
            }
            Scaffold { innerPadding ->
                navContent(Modifier.padding(innerPadding))
            }
        }
    }
}
