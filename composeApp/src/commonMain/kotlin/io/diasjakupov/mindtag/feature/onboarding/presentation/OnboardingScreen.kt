package io.diasjakupov.mindtag.feature.onboarding.presentation

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateDpAsState
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.widthIn
import androidx.compose.foundation.pager.HorizontalPager
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AccountTree
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.CloudUpload
import androidx.compose.material.icons.outlined.Psychology
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import org.koin.compose.viewmodel.koinViewModel

private data class OnboardingPage(
    val icon: ImageVector,
    val title: String,
    val description: String,
)

private val pages = listOf(
    OnboardingPage(
        icon = Icons.Outlined.AutoAwesome,
        title = "Smart Notes, Smarter Connections",
        description = "MindTag automatically links your study notes across subjects, building a semantic knowledge graph that reveals hidden connections.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.Psychology,
        title = "AI-Powered Study Sessions",
        description = "Adaptive flashcards powered by spaced repetition and semantic analysis. Study smarter, not harder.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.AccountTree,
        title = "Visualize Your Knowledge",
        description = "Explore an interactive knowledge graph showing how your notes connect across different subjects and topics.",
    ),
    OnboardingPage(
        icon = Icons.Outlined.CloudUpload,
        title = "Upload Your Syllabus",
        description = "Import your course syllabus and MindTag will create a personalized study plan tailored to your curriculum.",
    ),
)

@Composable
fun OnboardingScreen(
    onNavigateToHome: () -> Unit = {},
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    val pagerState = rememberPagerState(
        initialPage = state.currentPage,
        pageCount = { state.totalPages },
    )

    // Sync pager state -> ViewModel when user swipes
    LaunchedEffect(pagerState) {
        snapshotFlow { pagerState.currentPage }.collect { page ->
            if (page != state.currentPage) {
                viewModel.updatePageFromPager(page)
            }
        }
    }

    // Sync ViewModel -> pager state when button tapped
    LaunchedEffect(state.currentPage) {
        if (pagerState.currentPage != state.currentPage) {
            pagerState.animateScrollToPage(state.currentPage)
        }
    }

    // Collect effects
    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                OnboardingContract.Effect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar with Skip button
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.lg),
        ) {
            if (state.currentPage < state.totalPages - 1) {
                TextButton(
                    onClick = { viewModel.onIntent(OnboardingContract.Intent.Skip) },
                    modifier = Modifier.align(Alignment.CenterEnd),
                ) {
                    Text(
                        text = "Skip",
                        style = MaterialTheme.typography.bodyMedium,
                        color = MindTagColors.TextSecondary,
                    )
                }
            }
        }

        // Pager content
        HorizontalPager(
            state = pagerState,
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
        ) { pageIndex ->
            OnboardingPageContent(page = pages[pageIndex])
        }

        // Page indicators
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(vertical = MindTagSpacing.xl),
            horizontalArrangement = Arrangement.Center,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            repeat(state.totalPages) { index ->
                val isActive = index == pagerState.currentPage
                val color by animateColorAsState(
                    targetValue = if (isActive) MindTagColors.Primary else MindTagColors.InactiveDot,
                )
                val width by animateDpAsState(
                    targetValue = if (isActive) 24.dp else 8.dp,
                )
                Box(
                    modifier = Modifier
                        .padding(horizontal = 4.dp)
                        .size(width = width, height = 8.dp)
                        .clip(CircleShape)
                        .background(color),
                )
            }
        }

        // Bottom action area
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = MindTagSpacing.xxxl,
                    vertical = MindTagSpacing.xxxl,
                ),
        ) {
            if (state.currentPage == state.totalPages - 1) {
                MindTagButton(
                    text = "Get Started",
                    onClick = { viewModel.onIntent(OnboardingContract.Intent.GetStarted) },
                    variant = MindTagButtonVariant.PrimaryLarge,
                )
            } else {
                MindTagButton(
                    text = "Next",
                    onClick = { viewModel.onIntent(OnboardingContract.Intent.NextPage) },
                    variant = MindTagButtonVariant.PrimaryLarge,
                )
            }
        }
    }
}

@Composable
private fun OnboardingPageContent(page: OnboardingPage) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(horizontal = MindTagSpacing.xxxl),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center,
    ) {
        // Icon circle
        Box(
            modifier = Modifier
                .size(120.dp)
                .clip(CircleShape)
                .background(MindTagColors.Primary.copy(alpha = 0.1f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = page.icon,
                contentDescription = null,
                modifier = Modifier.size(56.dp),
                tint = MindTagColors.Primary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        // Title
        Text(
            text = page.title,
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
            textAlign = TextAlign.Center,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Description
        Text(
            text = page.description,
            style = MaterialTheme.typography.bodyLarge,
            color = MindTagColors.TextSecondary,
            textAlign = TextAlign.Center,
            modifier = Modifier.widthIn(max = 280.dp),
        )
    }
}
