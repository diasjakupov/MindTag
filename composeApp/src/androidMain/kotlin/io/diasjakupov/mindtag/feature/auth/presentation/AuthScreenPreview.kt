package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.compose.runtime.Composable
import androidx.compose.ui.tooling.preview.Preview
import io.diasjakupov.mindtag.core.designsystem.MindTagTheme

@Preview(showBackground = true, backgroundColor = 0xFF101622)
@Composable
private fun AuthScreenPreview() {
    MindTagTheme {
        AuthScreenContent(
            state = AuthState(
                email = "student@university.edu",
                password = "",
                isLoginMode = true,
                isLoading = false,
                error = null,
            ),
            onIntent = {},
        )
    }
}
