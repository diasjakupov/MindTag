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

private const val BIO_HEX = "#22C55E"
private const val PHY_HEX = "#A855F7"
private const val CHM_HEX = "#EF4444"
private const val CS_HEX = "#135BEC"
private const val MTH_HEX = "#EAB308"

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 800, name = "Graph — two clusters")
@Composable
private fun LibraryScreenGraphTwoClustersPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.GRAPH,
                isLoading = false,
                graphNodes = listOf(
                    // Biology cluster — left
                    LibraryContract.GraphNode(noteId = 1L, label = "Biology", subjectColorHex = BIO_HEX, x = 220f, y = 280f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 101L, label = "", subjectColorHex = BIO_HEX, x = 90f, y = 280f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 102L, label = "", subjectColorHex = BIO_HEX, x = 155f, y = 167f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 103L, label = "", subjectColorHex = BIO_HEX, x = 285f, y = 167f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 104L, label = "", subjectColorHex = BIO_HEX, x = 285f, y = 393f, radius = 28f, hubNoteId = 1L),

                    // Physics cluster — right
                    LibraryContract.GraphNode(noteId = 2L, label = "Physics", subjectColorHex = PHY_HEX, x = 580f, y = 280f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 201L, label = "", subjectColorHex = PHY_HEX, x = 710f, y = 280f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 202L, label = "", subjectColorHex = PHY_HEX, x = 645f, y = 167f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 203L, label = "", subjectColorHex = PHY_HEX, x = 645f, y = 393f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 204L, label = "", subjectColorHex = PHY_HEX, x = 515f, y = 393f, radius = 28f, hubNoteId = 2L),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", BIO_HEX, false),
                    LibraryContract.SubjectFilter("2", "Physics", PHY_HEX, false),
                ),
                selectedSubjectId = null,
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 900, name = "Graph — node selected with preview")
@Composable
private fun LibraryScreenGraphSelectedPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.GRAPH,
                isLoading = false,
                selectedNodeId = 103L,
                graphNodes = listOf(
                    LibraryContract.GraphNode(noteId = 1L, label = "Biology", subjectColorHex = BIO_HEX, x = 220f, y = 280f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 101L, label = "", subjectColorHex = BIO_HEX, x = 90f, y = 280f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 102L, label = "", subjectColorHex = BIO_HEX, x = 155f, y = 167f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 103L, label = "", subjectColorHex = BIO_HEX, x = 285f, y = 167f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 104L, label = "", subjectColorHex = BIO_HEX, x = 285f, y = 393f, radius = 28f, hubNoteId = 1L),

                    LibraryContract.GraphNode(noteId = 2L, label = "Physics", subjectColorHex = PHY_HEX, x = 580f, y = 280f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 201L, label = "", subjectColorHex = PHY_HEX, x = 710f, y = 280f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 202L, label = "", subjectColorHex = PHY_HEX, x = 645f, y = 167f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 203L, label = "", subjectColorHex = PHY_HEX, x = 645f, y = 393f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 204L, label = "", subjectColorHex = PHY_HEX, x = 515f, y = 393f, radius = 28f, hubNoteId = 2L),
                ),
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 103L,
                        title = "Photosynthesis: Light-Dependent Reactions",
                        summary = "How chloroplasts convert sunlight into ATP and NADPH through Photosystems II and I, water splitting, and chemiosmosis.",
                        subjectName = "Biology",
                        subjectColorHex = BIO_HEX,
                        weekNumber = 4,
                        readTimeMinutes = 7,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", BIO_HEX, false),
                    LibraryContract.SubjectFilter("2", "Physics", PHY_HEX, false),
                ),
                selectedSubjectId = null,
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, heightDp = 900, name = "Graph — five clusters fit-to-screen")
@Composable
private fun LibraryScreenGraphMultiClusterPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.GRAPH,
                isLoading = false,
                graphNodes = listOf(
                    // Biology — top-left
                    LibraryContract.GraphNode(noteId = 1L, label = "Biology", subjectColorHex = BIO_HEX, x = 350f, y = 350f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 101L, label = "", subjectColorHex = BIO_HEX, x = 220f, y = 350f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 102L, label = "", subjectColorHex = BIO_HEX, x = 350f, y = 220f, radius = 28f, hubNoteId = 1L),
                    LibraryContract.GraphNode(noteId = 103L, label = "", subjectColorHex = BIO_HEX, x = 220f, y = 220f, radius = 28f, hubNoteId = 1L),

                    // Physics — top-right
                    LibraryContract.GraphNode(noteId = 2L, label = "Physics", subjectColorHex = PHY_HEX, x = 770f, y = 350f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 201L, label = "", subjectColorHex = PHY_HEX, x = 900f, y = 350f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 202L, label = "", subjectColorHex = PHY_HEX, x = 770f, y = 220f, radius = 28f, hubNoteId = 2L),
                    LibraryContract.GraphNode(noteId = 203L, label = "", subjectColorHex = PHY_HEX, x = 900f, y = 220f, radius = 28f, hubNoteId = 2L),

                    // Chemistry — bottom-left
                    LibraryContract.GraphNode(noteId = 3L, label = "Chemistry", subjectColorHex = CHM_HEX, x = 350f, y = 770f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 301L, label = "", subjectColorHex = CHM_HEX, x = 220f, y = 770f, radius = 28f, hubNoteId = 3L),
                    LibraryContract.GraphNode(noteId = 302L, label = "", subjectColorHex = CHM_HEX, x = 350f, y = 900f, radius = 28f, hubNoteId = 3L),
                    LibraryContract.GraphNode(noteId = 303L, label = "", subjectColorHex = CHM_HEX, x = 220f, y = 900f, radius = 28f, hubNoteId = 3L),

                    // Computer Science — bottom-right
                    LibraryContract.GraphNode(noteId = 4L, label = "CS", subjectColorHex = CS_HEX, x = 770f, y = 770f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 401L, label = "", subjectColorHex = CS_HEX, x = 900f, y = 770f, radius = 28f, hubNoteId = 4L),
                    LibraryContract.GraphNode(noteId = 402L, label = "", subjectColorHex = CS_HEX, x = 770f, y = 900f, radius = 28f, hubNoteId = 4L),
                    LibraryContract.GraphNode(noteId = 403L, label = "", subjectColorHex = CS_HEX, x = 900f, y = 900f, radius = 28f, hubNoteId = 4L),

                    // Mathematics — center
                    LibraryContract.GraphNode(noteId = 5L, label = "Math", subjectColorHex = MTH_HEX, x = 560f, y = 560f, radius = 56f, isHub = true),
                    LibraryContract.GraphNode(noteId = 501L, label = "", subjectColorHex = MTH_HEX, x = 430f, y = 560f, radius = 28f, hubNoteId = 5L),
                    LibraryContract.GraphNode(noteId = 502L, label = "", subjectColorHex = MTH_HEX, x = 690f, y = 560f, radius = 28f, hubNoteId = 5L),
                    LibraryContract.GraphNode(noteId = 503L, label = "", subjectColorHex = MTH_HEX, x = 560f, y = 430f, radius = 28f, hubNoteId = 5L),
                    LibraryContract.GraphNode(noteId = 504L, label = "", subjectColorHex = MTH_HEX, x = 560f, y = 690f, radius = 28f, hubNoteId = 5L),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", BIO_HEX, false),
                    LibraryContract.SubjectFilter("2", "Physics", PHY_HEX, false),
                    LibraryContract.SubjectFilter("3", "Chemistry", CHM_HEX, false),
                    LibraryContract.SubjectFilter("4", "Computer Science", CS_HEX, false),
                    LibraryContract.SubjectFilter("5", "Mathematics", MTH_HEX, false),
                ),
                selectedSubjectId = null,
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Library — keyword search")
@Composable
private fun LibraryScreenKeywordSearchPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                searchQuery = "cellular energy",
                searchMode = LibraryContract.SearchMode.TEXT,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 50L,
                        title = "Cellular Respiration",
                        summary = "How cells release energy from glucose through glycolysis, the Krebs cycle, and the electron transport chain.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 4,
                        readTimeMinutes = 6,
                    ),
                    LibraryContract.NoteListItem(
                        id = 51L,
                        title = "Activation Energy and Catalysts",
                        summary = "The minimum energy required to start a chemical reaction, and how enzymes lower this barrier in cells.",
                        subjectName = "Chemistry",
                        subjectColorHex = "#EF4444",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 52L,
                        title = "Cell Membrane Transport",
                        summary = "Active and passive transport mechanisms — energy-dependent pumps versus diffusion across the lipid bilayer.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", false),
                    LibraryContract.SubjectFilter("2", "Physics", "#A855F7", false),
                    LibraryContract.SubjectFilter("3", "Chemistry", "#EF4444", false),
                    LibraryContract.SubjectFilter("4", "Economics", "#F59E0B", false),
                ),
                selectedSubjectId = null,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Library — semantic search")
@Composable
private fun LibraryScreenSemanticSearchPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                searchQuery = "cellular energy",
                searchMode = LibraryContract.SearchMode.SEMANTIC,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 60L,
                        title = "ATP and the Mitochondrion",
                        summary = "Adenosine triphosphate as the universal energy currency, and how mitochondria synthesize it via oxidative phosphorylation.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 4,
                        readTimeMinutes = 6,
                    ),
                    LibraryContract.NoteListItem(
                        id = 61L,
                        title = "Photosynthesis: Light-Dependent Reactions",
                        summary = "How chloroplasts convert sunlight into ATP and NADPH through Photosystems II and I.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 5,
                        readTimeMinutes = 7,
                    ),
                    LibraryContract.NoteListItem(
                        id = 62L,
                        title = "Thermodynamics: Free Energy",
                        summary = "Gibbs free energy and how it predicts whether a biochemical reaction is spontaneous or requires input.",
                        subjectName = "Physics",
                        subjectColorHex = "#A855F7",
                        weekNumber = 6,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 63L,
                        title = "Glucose Metabolism Pathways",
                        summary = "Aerobic and anaerobic routes for breaking down glucose, with comparative ATP yields.",
                        subjectName = "Chemistry",
                        subjectColorHex = "#EF4444",
                        weekNumber = 4,
                        readTimeMinutes = 5,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", false),
                    LibraryContract.SubjectFilter("2", "Physics", "#A855F7", false),
                    LibraryContract.SubjectFilter("3", "Chemistry", "#EF4444", false),
                    LibraryContract.SubjectFilter("4", "Economics", "#F59E0B", false),
                ),
                selectedSubjectId = null,
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Library — subject filter")
@Composable
private fun LibraryScreenSubjectFilterPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                searchQuery = "",
                searchMode = LibraryContract.SearchMode.TEXT,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 70L,
                        title = "Cell Division and Mitosis",
                        summary = "Overview of how eukaryotic cells divide through mitosis: prophase, metaphase, anaphase, and telophase.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 71L,
                        title = "Photosynthesis: Light-Dependent Reactions",
                        summary = "How chloroplasts convert sunlight into ATP and NADPH through Photosystems II and I.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 5,
                        readTimeMinutes = 7,
                    ),
                    LibraryContract.NoteListItem(
                        id = 72L,
                        title = "Diffusion and Osmosis",
                        summary = "Passive transport of particles and water across membranes, governed by concentration gradients.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 73L,
                        title = "DNA Replication",
                        summary = "Semiconservative duplication of the double helix, mediated by helicase, primase, and DNA polymerase.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 4,
                        readTimeMinutes = 6,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", true),
                    LibraryContract.SubjectFilter("2", "Physics", "#A855F7", false),
                    LibraryContract.SubjectFilter("3", "Chemistry", "#EF4444", false),
                    LibraryContract.SubjectFilter("4", "Economics", "#F59E0B", false),
                    LibraryContract.SubjectFilter("5", "Computer Science", "#135BEC", false),
                ),
                selectedSubjectId = "1",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622, name = "Library — journey: before tap")
@Composable
private fun LibraryScreenJourneyPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 20L,
                        title = "Diffusion and Osmosis",
                        summary = "Passive transport of particles and water across membranes, governed by concentration gradients.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 41L,
                        title = "Solubility and Concentration",
                        summary = "How temperature, pressure, and solvent polarity affect how much solute can dissolve in a given solvent.",
                        subjectName = "Chemistry",
                        subjectColorHex = "#EF4444",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 42L,
                        title = "Fick's Law of Diffusion",
                        summary = "Quantifies the rate of diffusion as proportional to the concentration gradient and the diffusion coefficient.",
                        subjectName = "Physics",
                        subjectColorHex = "#A855F7",
                        weekNumber = 4,
                        readTimeMinutes = 6,
                    ),
                    LibraryContract.NoteListItem(
                        id = 43L,
                        title = "Cell Membrane Structure",
                        summary = "Phospholipid bilayer with embedded proteins forming the selectively permeable boundary of every cell.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 1,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 44L,
                        title = "Supply and Demand Curves",
                        summary = "How market equilibrium is determined by the intersection of supply and demand, and what shifts each curve.",
                        subjectName = "Economics",
                        subjectColorHex = "#F59E0B",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", false),
                    LibraryContract.SubjectFilter("2", "Physics", "#A855F7", false),
                    LibraryContract.SubjectFilter("3", "Chemistry", "#EF4444", false),
                    LibraryContract.SubjectFilter("4", "Economics", "#F59E0B", false),
                ),
                selectedSubjectId = null,
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun LibraryScreenListPreview() {
    MindTagTheme {
        LibraryScreenContent(
            state = LibraryContract.State(
                viewMode = LibraryContract.ViewMode.LIST,
                isLoading = false,
                notes = listOf(
                    LibraryContract.NoteListItem(
                        id = 1L,
                        title = "Photosynthesis: Light-Dependent Reactions",
                        summary = "How chloroplasts convert sunlight into ATP and NADPH through Photosystems II and I, water splitting, and chemiosmosis.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 4,
                        readTimeMinutes = 7,
                    ),
                    LibraryContract.NoteListItem(
                        id = 2L,
                        title = "Newton's Second Law of Motion",
                        summary = "F = m × a — the acceleration of an object is proportional to the net force and inversely proportional to its mass.",
                        subjectName = "Physics",
                        subjectColorHex = "#A855F7",
                        weekNumber = 2,
                        readTimeMinutes = 5,
                    ),
                    LibraryContract.NoteListItem(
                        id = 3L,
                        title = "Binary Search Algorithm",
                        summary = "Efficient O(log n) search algorithm for sorted arrays, with iterative and recursive implementations and edge cases.",
                        subjectName = "Computer Science",
                        subjectColorHex = "#135BEC",
                        weekNumber = 5,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 4L,
                        title = "Acids, Bases, and pH",
                        summary = "Brønsted-Lowry theory, conjugate pairs, and the logarithmic pH scale relating hydrogen ion concentration to acidity.",
                        subjectName = "Chemistry",
                        subjectColorHex = "#EF4444",
                        weekNumber = 3,
                        readTimeMinutes = 6,
                    ),
                    LibraryContract.NoteListItem(
                        id = 5L,
                        title = "Supply and Demand Curves",
                        summary = "How market equilibrium is determined by the intersection of supply and demand, and what shifts each curve.",
                        subjectName = "Economics",
                        subjectColorHex = "#F59E0B",
                        weekNumber = 2,
                        readTimeMinutes = 4,
                    ),
                    LibraryContract.NoteListItem(
                        id = 6L,
                        title = "Cell Division and Mitosis",
                        summary = "Overview of how eukaryotic cells divide through mitosis: prophase, metaphase, anaphase, and telophase.",
                        subjectName = "Biology",
                        subjectColorHex = "#22C55E",
                        weekNumber = 3,
                        readTimeMinutes = 5,
                    ),
                ),
                subjects = listOf(
                    LibraryContract.SubjectFilter("1", "Biology", "#22C55E", false),
                    LibraryContract.SubjectFilter("2", "Physics", "#A855F7", false),
                    LibraryContract.SubjectFilter("3", "Computer Science", "#135BEC", false),
                    LibraryContract.SubjectFilter("4", "Chemistry", "#EF4444", false),
                    LibraryContract.SubjectFilter("5", "Economics", "#F59E0B", false),
                ),
                selectedSubjectId = null,
                searchQuery = "",
            ),
            onIntent = {},
        )
    }
}
