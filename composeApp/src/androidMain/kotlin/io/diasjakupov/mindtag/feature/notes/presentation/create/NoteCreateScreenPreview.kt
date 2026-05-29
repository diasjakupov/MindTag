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
                title = "Newton's Second Law of Motion",
                subjectName = "Physics",
                content = "Newton's second law states that the acceleration of an object is directly proportional to the net force acting on it and inversely proportional to its mass.\n\nThe law is expressed by the equation:\n\n    F = m × a\n\nwhere F is the net force (in newtons), m is the mass of the object (in kilograms), and a is the acceleration (in meters per second squared).\n\nKey implications:\n\n1. A larger force produces a larger acceleration on the same object.\n\n2. For the same applied force, a more massive object accelerates more slowly.\n\n3. Force and acceleration are vectors — they share the same direction.\n\nExample: Pushing a 2 kg cart with a net force of 10 N produces an acceleration of 5 m/s².",
                subjects = listOf(
                    Subject(id = "1", name = "Biology", colorHex = "#22C55E"),
                    Subject(id = "2", name = "Physics", colorHex = "#3B82F6"),
                    Subject(id = "3", name = "Chemistry", colorHex = "#F59E0B"),
                    Subject(id = "4", name = "Mathematics", colorHex = "#A855F7"),
                ),
                isSaving = false,
                isEditMode = false,
            ),
            onIntent = {},
            onNavigateBack = {},
        )
    }
}
