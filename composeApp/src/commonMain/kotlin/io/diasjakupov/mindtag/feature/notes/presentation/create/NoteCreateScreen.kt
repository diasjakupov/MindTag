package io.diasjakupov.mindtag.feature.notes.presentation.create

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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.AlertDialog
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
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

private val subjectColors = listOf(
    "#135BEC", // blue (primary)
    "#22C55E", // green
    "#F97316", // orange
    "#A855F7", // purple
    "#EF4444", // red
    "#EAB308", // yellow
    "#2DD4BF", // teal
    "#EC4899", // pink
)

@Composable
fun NoteCreateScreen(
    noteId: String? = null,
    onNavigateBack: () -> Unit,
) {
    val viewModel: NoteCreateViewModel = koinViewModel(parameters = { parametersOf(noteId) })
    val state by viewModel.state.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                is NoteCreateEffect.NavigateBackWithSuccess -> onNavigateBack()
                is NoteCreateEffect.ShowError -> { /* Could show snackbar */ }
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark),
    ) {
        // Top bar
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
                text = if (state.isEditMode) "Edit Note" else "New Note",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
                modifier = Modifier.weight(1f),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            IconButton(
                onClick = { viewModel.onIntent(NoteCreateIntent.Save) },
                enabled = !state.isSaving,
            ) {
                Icon(
                    imageVector = MindTagIcons.Check,
                    contentDescription = "Save",
                    tint = if (state.isSaving) MindTagColors.TextSecondary else MindTagColors.Primary,
                )
            }
        }

        // Title field
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        ) {
            BasicTextField(
                value = state.title,
                onValueChange = { viewModel.onIntent(NoteCreateIntent.UpdateTitle(it)) },
                textStyle = MaterialTheme.typography.headlineSmall.copy(color = Color.White),
                cursorBrush = SolidColor(MindTagColors.Primary),
                modifier = Modifier.fillMaxWidth(),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.title.isEmpty()) {
                            Text(
                                text = "Note title",
                                style = MaterialTheme.typography.headlineSmall,
                                color = MindTagColors.TextSecondary,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (state.titleError != null) {
                Text(
                    text = state.titleError!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = MindTagColors.Error,
                    modifier = Modifier.padding(top = MindTagSpacing.xs),
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Subject selector
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
            horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
        ) {
            state.subjects.forEach { subject ->
                val isSelected = subject.id == state.selectedSubjectId
                val chipColor = try {
                    Color(("FF" + subject.colorHex.removePrefix("#")).toLong(16))
                } catch (_: Exception) {
                    MindTagColors.Primary
                }

                Box(
                    modifier = Modifier
                        .clip(MindTagShapes.full)
                        .background(
                            if (isSelected) chipColor.copy(alpha = 0.2f)
                            else MindTagColors.SearchBarBg,
                        )
                        .clickable { viewModel.onIntent(NoteCreateIntent.SelectSubject(subject.id)) }
                        .padding(horizontal = MindTagSpacing.lg, vertical = MindTagSpacing.sm),
                ) {
                    Text(
                        text = subject.name,
                        style = MaterialTheme.typography.labelMedium,
                        color = if (isSelected) chipColor else MindTagColors.TextSecondary,
                        maxLines = 1,
                    )
                }
            }

            // "+ New" chip
            Box(
                modifier = Modifier
                    .clip(MindTagShapes.full)
                    .border(1.dp, MindTagColors.TextSecondary.copy(alpha = 0.3f), MindTagShapes.full)
                    .clickable { viewModel.onIntent(NoteCreateIntent.TapAddSubject) }
                    .padding(horizontal = MindTagSpacing.lg, vertical = MindTagSpacing.sm),
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.xs),
                ) {
                    Icon(
                        imageVector = MindTagIcons.Add,
                        contentDescription = "Add subject",
                        tint = MindTagColors.TextSecondary,
                        modifier = Modifier.size(14.dp),
                    )
                    Text(
                        text = "New",
                        style = MaterialTheme.typography.labelMedium,
                        color = MindTagColors.TextSecondary,
                        maxLines = 1,
                    )
                }
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        // Content field
        Column(
            modifier = Modifier.weight(1f),
        ) {
            BasicTextField(
                value = state.content,
                onValueChange = { viewModel.onIntent(NoteCreateIntent.UpdateContent(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = MindTagColors.TextSlate300),
                cursorBrush = SolidColor(MindTagColors.Primary),
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.content.isEmpty()) {
                            Text(
                                text = "Start writing...",
                                style = MaterialTheme.typography.bodyLarge,
                                color = MindTagColors.TextSecondary,
                            )
                        }
                        innerTextField()
                    }
                },
            )
            if (state.contentError != null) {
                Text(
                    text = state.contentError!!,
                    style = MaterialTheme.typography.labelMedium,
                    color = MindTagColors.Error,
                    modifier = Modifier
                        .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                        .padding(bottom = MindTagSpacing.md),
                )
            }
        }
    }

    if (state.showCreateSubjectDialog) {
        CreateSubjectDialog(
            name = state.newSubjectName,
            selectedColor = state.newSubjectColor,
            nameError = state.newSubjectNameError,
            onNameChange = { viewModel.onIntent(NoteCreateIntent.UpdateNewSubjectName(it)) },
            onColorSelect = { viewModel.onIntent(NoteCreateIntent.UpdateNewSubjectColor(it)) },
            onConfirm = { viewModel.onIntent(NoteCreateIntent.ConfirmCreateSubject) },
            onDismiss = { viewModel.onIntent(NoteCreateIntent.DismissCreateSubjectDialog) },
        )
    }
}

@Composable
private fun CreateSubjectDialog(
    name: String,
    selectedColor: String,
    nameError: String?,
    onNameChange: (String) -> Unit,
    onColorSelect: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = MindTagColors.CardDark,
        shape = MindTagShapes.lg,
        title = {
            Text(
                text = "New Subject",
                style = MaterialTheme.typography.titleMedium,
                color = Color.White,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(MindTagSpacing.md)) {
                // Name field
                BasicTextField(
                    value = name,
                    onValueChange = onNameChange,
                    textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                    cursorBrush = SolidColor(MindTagColors.Primary),
                    singleLine = true,
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(MindTagShapes.lg)
                        .background(MindTagColors.SurfaceDark)
                        .padding(horizontal = MindTagSpacing.md, vertical = MindTagSpacing.sm),
                    decorationBox = { innerTextField ->
                        Box {
                            if (name.isEmpty()) {
                                Text(
                                    text = "Subject name",
                                    style = MaterialTheme.typography.bodyLarge,
                                    color = MindTagColors.TextSecondary,
                                )
                            }
                            innerTextField()
                        }
                    },
                )
                if (nameError != null) {
                    Text(
                        text = nameError,
                        style = MaterialTheme.typography.labelMedium,
                        color = MindTagColors.Error,
                    )
                }

                // Color picker
                Text(
                    text = "Color",
                    style = MaterialTheme.typography.labelMedium,
                    color = MindTagColors.TextSecondary,
                )
                Row(
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.sm),
                ) {
                    subjectColors.forEach { hex ->
                        val circleColor = try {
                            Color(("FF" + hex.removePrefix("#")).toLong(16))
                        } catch (_: Exception) {
                            MindTagColors.Primary
                        }
                        val isSelected = hex == selectedColor
                        Box(
                            modifier = Modifier
                                .size(28.dp)
                                .clip(CircleShape)
                                .background(circleColor)
                                .then(
                                    if (isSelected) Modifier.border(2.dp, Color.White, CircleShape)
                                    else Modifier,
                                )
                                .clickable { onColorSelect(hex) },
                        )
                    }
                }
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm) {
                Text("Create", color = MindTagColors.Primary)
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("Cancel", color = MindTagColors.TextSecondary)
            }
        },
    )
}
