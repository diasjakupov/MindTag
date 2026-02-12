package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
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
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChip
import io.diasjakupov.mindtag.core.designsystem.components.MindTagChipVariant
import io.diasjakupov.mindtag.core.designsystem.components.ShimmerBox
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteDetailScreen(
    noteId: String,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (String) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToEdit: (String) -> Unit = {},
) {
    val viewModel: NoteDetailViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteDetailEffect.NavigateBack -> onNavigateBack()
                is NoteDetailEffect.NavigateToNote -> onNavigateToNote(effect.noteId)
                is NoteDetailEffect.NavigateToQuiz -> onNavigateToQuiz(effect.sessionId)
                is NoteDetailEffect.NavigateToEdit -> onNavigateToEdit(effect.noteId)
                is NoteDetailEffect.ShowError -> { /* TODO: snackbar */ }
            }
        }
    }

    if (state.isLoading) {
        NoteDetailShimmerSkeleton()
        return
    }

    val note = state.note ?: return

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top navigation bar
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            IconButton(onClick = onNavigateBack) {
                Icon(
                    imageVector = MindTagIcons.ArrowBack,
                    contentDescription = "Back",
                    tint = Color.White,
                )
            }
            Text(
                text = note.title,
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
                textAlign = TextAlign.Center,
            )
            Row {
                IconButton(onClick = { viewModel.onIntent(NoteDetailIntent.TapEdit) }) {
                    Icon(
                        imageVector = MindTagIcons.Edit,
                        contentDescription = "Edit",
                        tint = MindTagColors.TextSlate300,
                    )
                }
                IconButton(onClick = { viewModel.onIntent(NoteDetailIntent.TapDelete) }) {
                    Icon(
                        imageVector = MindTagIcons.Delete,
                        contentDescription = "Delete",
                        tint = MindTagColors.TextSlate300,
                    )
                }
            }
        }

        // Toolbar actions: Listen + Quiz Me
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            // Listen placeholder
            IconButton(onClick = { /* placeholder */ }) {
                Icon(
                    imageVector = MindTagIcons.Headphones,
                    contentDescription = "Listen",
                    tint = MindTagColors.TextSlate300,
                )
            }
            // Quiz Me pill button
            if (state.isCreatingQuiz) {
                CircularProgressIndicator(
                    color = MindTagColors.Primary,
                    modifier = Modifier.size(24.dp),
                    strokeWidth = 2.dp,
                )
            } else {
                MindTagButton(
                    text = "Quiz Me",
                    onClick = { viewModel.onIntent(NoteDetailIntent.TapQuizMe) },
                    variant = MindTagButtonVariant.Pill,
                )
            }
        }

        // Metadata chips row
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.md),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            if (state.subjectName.isNotEmpty()) {
                MindTagChip(
                    text = state.subjectName,
                    variant = MindTagChipVariant.Metadata,
                )
            }
            // Separator dot
            if (state.subjectName.isNotEmpty()) {
                Box(
                    modifier = Modifier
                        .size(4.dp)
                        .clip(MindTagShapes.full)
                        .background(MindTagColors.TextTertiary),
                )
            }
            Text(
                text = "${note.readTimeMinutes} min read",
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextSecondary,
            )
        }

        // Scrollable note content
        Column(
            modifier = Modifier
                .weight(1f)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .padding(top = MindTagSpacing.xl),
        ) {
            Text(
                text = note.content,
                style = MaterialTheme.typography.bodyLarge,
                color = MindTagColors.TextSlate300,
                lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
        }

        // Related Notes section
        if (state.relatedNotes.isNotEmpty()) {
            RelatedNotesSection(
                relatedNotes = state.relatedNotes,
                onNoteTap = { viewModel.onIntent(NoteDetailIntent.TapRelatedNote(it)) },
            )
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { viewModel.onIntent(NoteDetailIntent.DismissDeleteDialog) },
            title = { Text("Delete Note", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete this note? This cannot be undone.",
                    color = MindTagColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { viewModel.onIntent(NoteDetailIntent.ConfirmDelete) }) {
                    Text("Delete", color = MindTagColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { viewModel.onIntent(NoteDetailIntent.DismissDeleteDialog) }) {
                    Text("Cancel", color = MindTagColors.TextSecondary)
                }
            },
            containerColor = MindTagColors.CardDark,
        )
    }
}

@Composable
private fun RelatedNotesSection(
    relatedNotes: List<RelatedNote>,
    onNoteTap: (String) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.xl)
            .background(MindTagColors.SurfaceDarkAlt2)
            .padding(top = MindTagSpacing.lg, bottom = MindTagSpacing.xxl),
    ) {
        // Header
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.xxl),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = "RELATED NOTES",
                style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                color = MindTagColors.TextTertiary,
                letterSpacing = MaterialTheme.typography.labelSmall.letterSpacing,
            )
            Text(
                text = "View Graph",
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.Primary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.lg))

        // Horizontal scroll of related note cards
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MindTagSpacing.xxl),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
        ) {
            relatedNotes.forEach { related ->
                RelatedNoteCard(
                    relatedNote = related,
                    onClick = { onNoteTap(related.noteId) },
                )
            }
        }
    }
}

@Composable
private fun NoteDetailShimmerSkeleton() {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .height(MindTagSpacing.topAppBarHeight)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            ShimmerBox(
                modifier = Modifier.size(MindTagSpacing.iconButtonSize),
                shape = MindTagShapes.md,
            )
            Spacer(modifier = Modifier.weight(1f))
            ShimmerBox(
                modifier = Modifier
                    .fillMaxWidth(0.4f)
                    .height(20.dp),
            )
            Spacer(modifier = Modifier.weight(1f))
            Spacer(modifier = Modifier.size(MindTagSpacing.iconButtonSize))
        }

        // Toolbar row shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.xxs),
            horizontalArrangement = Arrangement.SpaceBetween,
        ) {
            ShimmerBox(
                modifier = Modifier.size(40.dp),
                shape = MindTagShapes.md,
            )
            ShimmerBox(
                modifier = Modifier
                    .size(width = 100.dp, height = 36.dp),
                shape = MindTagShapes.full,
            )
        }

        // Metadata chips row shimmer
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.md),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            ShimmerBox(
                modifier = Modifier.size(width = 80.dp, height = 28.dp),
                shape = MindTagShapes.full,
            )
            ShimmerBox(
                modifier = Modifier.size(width = 80.dp, height = 28.dp),
                shape = MindTagShapes.full,
            )
        }

        // Content lines shimmer
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                .padding(top = MindTagSpacing.xl),
            verticalArrangement = Arrangement.spacedBy(MindTagSpacing.lg),
        ) {
            val widths = listOf(1f, 0.9f, 0.95f, 0.85f, 0.8f, 1f, 0.7f, 0.6f)
            widths.forEach { fraction ->
                ShimmerBox(
                    modifier = Modifier
                        .fillMaxWidth(fraction)
                        .height(16.dp),
                    shape = MindTagShapes.sm,
                )
            }
        }

        // Related notes section shimmer
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(top = MindTagSpacing.lg, bottom = MindTagSpacing.xxl),
        ) {
            ShimmerBox(
                modifier = Modifier
                    .padding(horizontal = MindTagSpacing.xxl)
                    .fillMaxWidth(0.3f)
                    .height(14.dp),
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.lg))
            Row(
                modifier = Modifier.padding(horizontal = MindTagSpacing.xxl),
                horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
            ) {
                repeat(3) {
                    ShimmerBox(
                        modifier = Modifier
                            .width(160.dp)
                            .height(128.dp),
                    )
                }
            }
        }
    }
}

@Composable
private fun RelatedNoteCard(
    relatedNote: RelatedNote,
    onClick: () -> Unit,
) {
    val subjectColor = try {
        Color(("FF" + relatedNote.subjectColorHex.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MindTagColors.AccentPurple
    }

    Column(
        modifier = Modifier
            .width(160.dp)
            .height(128.dp)
            .clip(MindTagShapes.lg)
            .background(Color(0xFF151B26))
            .border(1.dp, MindTagColors.NodeBorder, MindTagShapes.lg)
            .clickable(onClick = onClick)
            .padding(MindTagSpacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
        // Subject icon
        Box(
            modifier = Modifier
                .size(32.dp)
                .clip(MindTagShapes.md)
                .background(subjectColor.copy(alpha = 0.15f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = MindTagIcons.MenuBook,
                contentDescription = null,
                tint = subjectColor,
                modifier = Modifier.size(20.dp),
            )
        }

        Column {
            Text(
                text = relatedNote.subjectName,
                style = MaterialTheme.typography.labelMedium,
                color = MindTagColors.TextSlate500,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.xxs))
            Text(
                text = relatedNote.title,
                style = MaterialTheme.typography.bodySmall.copy(fontWeight = FontWeight.SemiBold),
                color = MindTagColors.TextSlate300,
                maxLines = 2,
                overflow = TextOverflow.Ellipsis,
            )
        }
    }
}
