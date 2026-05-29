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
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 2L,
                        title = "DNA Replication",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                    RelatedNote(
                        noteId = 3L,
                        title = "Meiosis and Genetic Variation",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun NoteDetailScreenProcessedPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 10L,
                    title = "Photosynthesis: Light-Dependent Reactions",
                    content = "The light-dependent reactions of photosynthesis take place in the thylakoid membranes of the chloroplast and convert solar energy into chemical energy stored in ATP and NADPH.\n\nKey steps:\n\n1. Photon absorption \u2014 Chlorophyll molecules in Photosystem II absorb light, exciting electrons to a higher energy state.\n\n2. Water splitting (photolysis) \u2014 Water molecules are split into oxygen, protons, and electrons. The oxygen is released as a byproduct.\n\n3. Electron transport chain \u2014 High-energy electrons flow from PSII through a series of carriers to Photosystem I, pumping protons into the thylakoid lumen and creating a proton gradient.\n\n4. ATP synthesis \u2014 Protons flow back through ATP synthase, driving the phosphorylation of ADP into ATP (chemiosmosis).\n\n5. NADPH formation \u2014 At Photosystem I, electrons are re-energized by light and used to reduce NADP\u207a into NADPH.\n\nThe ATP and NADPH produced here power the Calvin cycle, where CO\u2082 is fixed into glucose.",
                    summary = "How chloroplasts convert sunlight into ATP and NADPH through Photosystems II and I, water splitting, and chemiosmosis.",
                    subjectId = "1",
                    subjectName = "Biology",
                    weekNumber = 4,
                    readTimeMinutes = 7,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Biology",
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 11L,
                        title = "The Calvin Cycle",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                    RelatedNote(
                        noteId = 12L,
                        title = "Cellular Respiration Overview",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                    RelatedNote(
                        noteId = 13L,
                        title = "Chloroplast Structure",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                ),
                isLoading = false,
                isCreatingQuiz = false,
                quizGenerationStatus = "",
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Quiz generating — Waiting for AI")
@Composable
private fun NoteDetailScreenQuizGeneratingPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 50L,
                    title = "Cell Division and Mitosis",
                    content = "Mitosis is a process of cell division where a single cell divides to produce two genetically identical daughter cells.\n\nThe four main phases are prophase, metaphase, anaphase, and telophase.",
                    summary = "Overview of eukaryotic cell division through mitosis: prophase, metaphase, anaphase, and telophase.",
                    subjectId = "1",
                    subjectName = "Biology",
                    weekNumber = 3,
                    readTimeMinutes = 5,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Biology",
                relatedNotes = emptyList(),
                isLoading = false,
                isCreatingQuiz = true,
                quizGenerationStatus = "Waiting for AI...",
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 900)
@Composable
private fun NoteDetailScreenContentWithRelatedPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 40L,
                    title = "Newton's Second Law of Motion",
                    content = "Newton's second law states that the acceleration of an object is directly proportional to the net force acting on it and inversely proportional to its mass.\n\nF = m × a\n\nForce and acceleration are vectors — they share the same direction. A more massive object accelerates more slowly under the same force.",
                    summary = "Acceleration of an object is proportional to net force and inversely proportional to mass: F = m × a.",
                    subjectId = "2",
                    subjectName = "Physics",
                    weekNumber = 2,
                    readTimeMinutes = 5,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Physics",
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 41L,
                        title = "Vectors and Scalars",
                        subjectName = "Mathematics",
                        subjectColorHex = "#EAB308",
                    ),
                    RelatedNote(
                        noteId = 42L,
                        title = "Free Body Diagrams",
                        subjectName = "Physics",
                        subjectColorHex = "#A855F7",
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 1100)
@Composable
private fun NoteDetailScreenRelatedNotesPreview() {
    MindTagTheme {
        NoteDetailScreenContent(
            state = NoteDetailState(
                note = Note(
                    id = 20L,
                    title = "Diffusion and Osmosis",
                    content = "Diffusion is the net movement of particles from a region of higher concentration to a region of lower concentration, driven by random thermal motion.\n\nOsmosis is a special case of diffusion involving the movement of water across a selectively permeable membrane, from a region of lower solute concentration to higher solute concentration.\n\nBoth processes are passive — they do not require cellular energy (ATP).",
                    summary = "Passive transport of particles and water across membranes, governed by concentration gradients.",
                    subjectId = "1",
                    subjectName = "Biology",
                    weekNumber = 2,
                    readTimeMinutes = 4,
                    createdAt = 0L,
                    updatedAt = 0L,
                ),
                subjectName = "Biology",
                relatedNotes = listOf(
                    RelatedNote(
                        noteId = 21L,
                        title = "Solubility and Concentration",
                        subjectName = "Chemistry",
                        subjectColorHex = "#EF4444",
                    ),
                    RelatedNote(
                        noteId = 22L,
                        title = "Fick's Law of Diffusion",
                        subjectName = "Physics",
                        subjectColorHex = "#A855F7",
                    ),
                    RelatedNote(
                        noteId = 23L,
                        title = "Cell Membrane Structure",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                    ),
                ),
                isLoading = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
