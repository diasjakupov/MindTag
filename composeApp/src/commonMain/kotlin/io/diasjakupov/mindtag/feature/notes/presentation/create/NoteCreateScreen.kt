package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.compose.foundation.background
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
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
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
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf

@Composable
fun NoteCreateScreen(
    noteId: Long? = null,
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
            state.titleError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = MindTagColors.Error,
                    modifier = Modifier.padding(top = MindTagSpacing.xs),
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
        ) {
            BasicTextField(
                value = state.subjectName,
                onValueChange = { viewModel.onIntent(NoteCreateIntent.UpdateSubjectName(it)) },
                textStyle = MaterialTheme.typography.bodyLarge.copy(color = Color.White),
                cursorBrush = SolidColor(MindTagColors.Primary),
                singleLine = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(MindTagShapes.lg)
                    .background(MindTagColors.SearchBarBg)
                    .padding(horizontal = MindTagSpacing.lg, vertical = MindTagSpacing.md),
                decorationBox = { innerTextField ->
                    Box {
                        if (state.subjectName.isEmpty()) {
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

            if (state.subjects.isNotEmpty()) {
                Spacer(modifier = Modifier.height(MindTagSpacing.md))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .horizontalScroll(rememberScrollState()),
                    horizontalArrangement = Arrangement.spacedBy(MindTagSpacing.md),
                ) {
                    state.subjects.forEach { subject ->
                        val isSelected = subject.name == state.subjectName
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
                                .clickable { viewModel.onIntent(NoteCreateIntent.SelectSubject(subject.name)) }
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
                }
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xl))

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
            state.contentError?.let { error ->
                Text(
                    text = error,
                    style = MaterialTheme.typography.labelMedium,
                    color = MindTagColors.Error,
                    modifier = Modifier
                        .padding(horizontal = MindTagSpacing.screenHorizontalPadding)
                        .padding(bottom = MindTagSpacing.md),
                )
            }
        }
    }
}
