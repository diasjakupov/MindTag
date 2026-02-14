package io.diasjakupov.mindtag.feature.notes.presentation.create

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.core.domain.model.Subject

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun NoteCreateScreenPreview() {
    MindTagTheme {
        NoteCreateScreenContent(
            state = NoteCreateState(
                title = "Cell Division and Mitosis",
                content = "Mitosis is a process of cell division where a single cell divides to produce two genetically identical daughter cells. It consists of four main phases: prophase, metaphase, anaphase, and telophase.",
                subjectName = "Biology",
                subjects = listOf(
                    Subject("1", "Biology", "#22C55E", "biology"),
                    Subject("2", "Economics", "#F59E0B", "economics"),
                    Subject("3", "Computer Science", "#135BEC", "cs"),
                ),
                isSaving = false,
                isEditMode = true,
                editNoteId = 1L,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
