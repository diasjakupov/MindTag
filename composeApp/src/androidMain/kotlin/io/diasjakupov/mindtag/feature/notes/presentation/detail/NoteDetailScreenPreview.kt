package io.diasjakupov.mindtag.feature.notes.presentation.detail

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme
import io.diasjakupov.mindtag.feature.notes.domain.model.Note
import io.diasjakupov.mindtag.feature.notes.domain.model.RelatedNote

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun NoteDetailScreenPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 1L,
                    title = "Cell Division and Mitosis",
                    content = "Mitosis is a process of cell division where a single cell divides to produce two genetically identical daughter cells.\n\nIt consists of four main phases:\n\n1. Prophase \u2014 Chromatin condenses into chromosomes, the nuclear envelope begins to break down, and spindle fibers start to form.\n\n2. Metaphase \u2014 Chromosomes align along the metaphase plate at the center of the cell.\n\n3. Anaphase \u2014 Sister chromatids separate and move to opposite poles of the cell.\n\n4. Telophase \u2014 Nuclear envelopes reform around each set of chromosomes, and the chromosomes begin to decondense.",
                    summary = "Overview of eukaryotic cell division through mitosis",
                    subjectId = "1",
                    subjectName = "Biology",
                    weekNumber = 3,
                    readTimeMinutes = 5,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Biology",
                subjectColorHex = "#22C55E",
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 2L,
                        title = "DNA Replication",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        similarityScore = 0.85f,
                    ),
                    RelatedNote(
                        noteId = 3L,
                        title = "Meiosis and Genetic Variation",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        similarityScore = 0.78f,
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
