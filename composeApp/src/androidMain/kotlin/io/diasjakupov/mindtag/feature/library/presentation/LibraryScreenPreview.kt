package io.diasjakupov.mindtag.feature.library.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun LibraryScreenPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.GRAPH,
                isLoading = false,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 1L,
                        title = "Cell Division and Mitosis",
                        summary = "Overview of how eukaryotic cells divide through mitosis, including prophase, metaphase, anaphase, and telophase.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 2L,
                        title = "Supply and Demand Curves",
                        summary = "How market equilibrium is determined by the intersection of supply and demand, and factors that shift each curve.",
                        subjectName = "Economics",
                        subjectColorHex = "#F59E0B",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 3L,
                        title = "Binary Search Algorithm",
                        summary = "Efficient O(log n) search algorithm for sorted arrays, with iterative and recursive implementations.",
                        subjectName = "Computer Science",
                        subjectColorHex = "#135BEC",
                        weekNumber = 5,
                        readTimeMinutes = 3,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", true),
                    LibraryContract.SubjectFilter("2", "Economics", "#F59E0B", false),
                    LibraryContract.SubjectFilter("3", "Computer Science", "#135BEC", false),
                ),
                selectedSubjectId = "1",
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}
