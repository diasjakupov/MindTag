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
import androidx.compose.foundation.layout.widthIn
import io.diasjakupov.mindtag.core.designsystem.LocalWindowSizeClass
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.WindowSizeClass
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
    noteId: Long,
    onNavigateBack: () -> Unit,
    onNavigateToNote: (Long) -> Unit,
    onNavigateToQuiz: (String) -> Unit,
    onNavigateToEdit: (Long) -> Unit = {},
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

    NoteDetailScreenContent(
        state = state,
        onIntent = viewModel::onIntent,
        onNavigateBack = onNavigateBack,
    )
}

@Composable
fun NoteDetailScreenContent(
    state: NoteDetailState,
    onIntent: (NoteDetailIntent) -> Unit,
    onNavigateBack: () -> Unit,
) {
    val note = state.note ?: return
    val windowSizeClass = LocalWindowSizeClass.current

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar — always full width
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
                IconButton(onClick = { onIntent(NoteDetailIntent.TapEdit) }) {
                    Icon(
                        imageVector = MindTagIcons.Edit,
                        contentDescription = "Edit",
                        tint = MindTagColors.TextSlate300,
                    )
                }
                IconButton(onClick = { onIntent(NoteDetailIntent.TapDelete) }) {
                    Icon(
                        imageVector = MindTagIcons.Delete,
                        contentDescription = "Delete",
                        tint = MindTagColors.TextSlate300,
                    )
                }
            }
        }

        if (windowSizeClass == WindowSizeClass.Expanded) {
            // Two-pane layout
            Row(modifier = Modifier.weight(1f)) {
                // Left pane: note content
                Column(
                    modifier = Modifier
                        .weight(0.65f)
                        .verticalScroll(rememberScrollState()),
                ) {
                    NoteDetailActionBar(state = state, onIntent = onIntent)
                    NoteDetailMetadata(state = state, note = note)
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MindTagColors.TextSlate300,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        modifier = Modifier
                            .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                            .padding(top = MindTagSpacing.xl),
                    )
                    Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
                }
                // Right pane: always present to prevent layout jump
                Column(
                    modifier = Modifier
                        .weight(0.35f)
                        .verticalScroll(rememberScrollState())
                        .padding(top = MindTagSpacing.xl, end = MindTagSpacing.screenHorizontalPadding),
                ) {
                    Text(
                        text = "RELATED NOTES",
                        style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
                        color = MindTagColors.TextTertiary,
                    )
                    Spacer(modifier = Modifier.height(MindTagSpacing.lg))
                    if (state.relatedNotes.isEmpty()) {
                        Text(
                            text = "No related notes yet",
                            style = MaterialTheme.typography.bodySmall,
                            color = MindTagColors.TextSecondary,
                        )
                    } else {
                        state.relatedNotes.forEach { related ->
                            RelatedNoteCard(
                                relatedNote = related,
                                onClick = { onIntent(NoteDetailIntent.TapRelatedNote(related.noteId)) },
                                modifier = Modifier.fillMaxWidth(),
                            )
                            Spacer(modifier = Modifier.height(MindTagSpacing.lg))
                        }
                    }
                }
            }
        } else {
            // Compact & Medium: single-column
            val contentModifier = if (windowSizeClass == WindowSizeClass.Medium) {
                Modifier.widthIn(max = MindTagSpacing.contentMaxWidthMedium)
                    .align(Alignment.CenterHorizontally)
            } else {
                Modifier
            }

            Column(modifier = Modifier.fillMaxWidth(), horizontalAlignment = Alignment.CenterHorizontally) {
                Column(modifier = contentModifier.fillMaxWidth()) {
                    NoteDetailActionBar(state = state, onIntent = onIntent)
                    NoteDetailMetadata(state = state, note = note)
                }
            }

            Column(
                modifier = Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState()),
                horizontalAlignment = Alignment.CenterHorizontally,
            ) {
                Column(modifier = contentModifier.fillMaxWidth()) {
                    Text(
                        text = note.content,
                        style = MaterialTheme.typography.bodyLarge,
                        color = MindTagColors.TextSlate300,
                        lineHeight = MaterialTheme.typography.bodyLarge.lineHeight,
                        modifier = Modifier
                            .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                            .padding(top = MindTagSpacing.xl),
                    )
                    Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
                }
            }

            if (state.relatedNotes.isNotEmpty()) {
                RelatedNotesSection(
                    relatedNotes = state.relatedNotes,
                    onNoteTap = { onIntent(NoteDetailIntent.TapRelatedNote(it)) },
                )
            }
        }
    }

    if (state.showDeleteConfirmation) {
        AlertDialog(
            onDismissRequest = { onIntent(NoteDetailIntent.DismissDeleteDialog) },
            title = { Text("Delete Note", color = Color.White) },
            text = {
                Text(
                    "Are you sure you want to delete this note? This cannot be undone.",
                    color = MindTagColors.TextSecondary,
                )
            },
            confirmButton = {
                TextButton(onClick = { onIntent(NoteDetailIntent.ConfirmDelete) }) {
                    Text("Delete", color = MindTagColors.Error)
                }
            },
            dismissButton = {
                TextButton(onClick = { onIntent(NoteDetailIntent.DismissDeleteDialog) }) {
                    Text("Cancel", color = MindTagColors.TextSecondary)
                }
            },
            containerColor = MindTagColors.CardDark,
        )
    }
}

@Composable
private fun NoteDetailActionBar(
    state: NoteDetailState,
    onIntent: (NoteDetailIntent) -> Unit,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding, vertical = MindTagSpacing.xxs),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically,
    ) {
        IconButton(onClick = { /* placeholder */ }) {
            Icon(
                imageVector = MindTagIcons.Headphones,
                contentDescription = "Listen",
                tint = MindTagColors.TextSlate300,
            )
        }
        if (state.isCreatingQuiz) {
            CircularProgressIndicator(
                color = MindTagColors.Primary,
                modifier = Modifier.size(24.dp),
                strokeWidth = 2.dp,
            )
        } else {
            MindTagButton(
                text = "Quiz Me",
                onClick = { onIntent(NoteDetailIntent.TapQuizMe) },
                variant = MindTagButtonVariant.Pill,
            )
        }
    }
}

@Composable
private fun NoteDetailMetadata(
    state: NoteDetailState,
    note: io.diasjakupov.mindtag.feature.notes.domain.model.Note,
) {
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
}

@Composable
private fun RelatedNotesSection(
    relatedNotes: List<RelatedNote>,
    onNoteTap: (Long) -> Unit,
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(MindTagShapes.xl)
            .background(MindTagColors.SurfaceDarkAlt2)
            .padding(top = MindTagSpacing.lg, bottom = MindTagSpacing.xxl),
    ) {
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
    modifier: Modifier = Modifier,
) {
    val subjectColor = try {
        Color(("FF" + relatedNote.subjectColorHex.removePrefix("#")).toLong(16))
    } catch (_: Exception) {
        MindTagColors.AccentPurple
    }

    Column(
        modifier = modifier
            .widthIn(min = 160.dp)
            .height(128.dp)
            .clip(MindTagShapes.lg)
            .background(Color(0xFF151B26))
            .border(1.dp, MindTagColors.NodeBorder, MindTagShapes.lg)
            .clickable(onClick = onClick)
            .padding(MindTagSpacing.lg),
        verticalArrangement = Arrangement.SpaceBetween,
    ) {
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
