package io.diasjakupov.mindtag.feature.study.presentation.hub

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun StudyHubScreenPreview() {
    MindTagTheme {
        StudyHubScreenContent(
            state = StudyHubState(
                subjects = listOf(
                    SubjectUi("1", "Biology"),
                    SubjectUi("2", "Economics"),
                    SubjectUi("3", "Computer Science"),
                ),
                selectedSubjectId = "1",
                questionCount = 10,
                timerEnabled = false,
                timerMinutes = 15,
                cardsDueCount = 12,
            ),
            onIntent = {},
        )
    }
}
