package io.diasjakupov.mindtag.feature.auth.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material.icons.outlined.Lock
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextField
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButton
import io.diasjakupov.mindtag.core.designsystem.components.MindTagButtonVariant
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun AuthScreen(
    onNavigateToHome: () -> Unit,
    viewModel: AuthViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    LaunchedEffect(Unit) {
        viewModel.effect.collect { effect ->
            when (effect) {
                AuthEffect.NavigateToHome -> onNavigateToHome()
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState()),
    ) {
        AuthGradientBanner()

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        AuthFormSection(
            state = state,
            onEmailChange = { viewModel.onIntent(AuthIntent.UpdateEmail(it)) },
            onPasswordChange = { viewModel.onIntent(AuthIntent.UpdatePassword(it)) },
            onSubmit = { viewModel.onIntent(AuthIntent.Submit) },
            onToggleMode = { viewModel.onIntent(AuthIntent.ToggleMode) },
        )
    }
}

@Composable
private fun AuthGradientBanner() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(220.dp)
            .background(
                Brush.linearGradient(
                    colors = listOf(
                        MindTagColors.Primary.copy(alpha = 0.8f),
                        Color(0xFF1E3A8A).copy(alpha = 0.8f),
                    ),
                ),
            ),
        contentAlignment = Alignment.Center,
    ) {
        // Decorative background circle
        Box(
            modifier = Modifier
                .size(160.dp)
                .clip(MindTagShapes.full)
                .background(Color.White.copy(alpha = 0.05f)),
            contentAlignment = Alignment.Center,
        ) {
            Icon(
                imageVector = Icons.Outlined.AutoAwesome,
                contentDescription = null,
                modifier = Modifier.size(64.dp),
                tint = Color.White.copy(alpha = 0.15f),
            )
        }

        // Title + tagline
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "MindTag",
                style = MaterialTheme.typography.displayLarge,
                color = Color.White,
            )
            Spacer(modifier = Modifier.height(MindTagSpacing.md))
            Text(
                text = "Your knowledge, connected.",
                style = MaterialTheme.typography.bodyLarge,
                color = Color(0xFFBFDBFE),
                textAlign = TextAlign.Center,
            )
        }
    }
}

@Composable
private fun AuthFormSection(
    state: AuthState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onSubmit: () -> Unit,
    onToggleMode: () -> Unit,
) {
    var passwordVisible by remember { mutableStateOf(false) }

    val filledFieldColors = TextFieldDefaults.colors(
        focusedContainerColor = MindTagColors.CardDark,
        unfocusedContainerColor = MindTagColors.CardDark,
        disabledContainerColor = MindTagColors.CardDark,
        focusedIndicatorColor = Color.Transparent,
        unfocusedIndicatorColor = Color.Transparent,
        disabledIndicatorColor = Color.Transparent,
        cursorColor = MindTagColors.Primary,
        focusedTextColor = Color.White,
        unfocusedTextColor = Color.White,
        focusedPlaceholderColor = MindTagColors.TextSecondary,
        unfocusedPlaceholderColor = MindTagColors.TextSecondary,
        focusedLeadingIconColor = MindTagColors.TextSecondary,
        unfocusedLeadingIconColor = MindTagColors.TextSecondary,
        focusedTrailingIconColor = MindTagColors.TextSecondary,
        unfocusedTrailingIconColor = MindTagColors.TextSecondary,
    )

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = MindTagSpacing.xxxl),
        verticalArrangement = Arrangement.spacedBy(MindTagSpacing.xl),
    ) {
        Text(
            text = if (state.isLoginMode) "Welcome back" else "Create your account",
            style = MaterialTheme.typography.headlineSmall,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Email field
        TextField(
            value = state.email,
            onValueChange = onEmailChange,
            placeholder = { Text("Email address") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Email,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
            singleLine = true,
            colors = filledFieldColors,
            shape = MindTagShapes.lg,
            modifier = Modifier.fillMaxWidth(),
        )

        // Password field
        TextField(
            value = state.password,
            onValueChange = onPasswordChange,
            placeholder = { Text("Password") },
            leadingIcon = {
                Icon(
                    imageVector = Icons.Outlined.Lock,
                    contentDescription = null,
                    modifier = Modifier.size(20.dp),
                )
            },
            trailingIcon = {
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(
                        imageVector = if (passwordVisible) Icons.Outlined.VisibilityOff
                                      else Icons.Outlined.Visibility,
                        contentDescription = if (passwordVisible) "Hide password" else "Show password",
                        modifier = Modifier.size(20.dp),
                    )
                }
            },
            visualTransformation = if (passwordVisible) VisualTransformation.None
                                   else PasswordVisualTransformation(),
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
            singleLine = true,
            colors = filledFieldColors,
            shape = MindTagShapes.lg,
            modifier = Modifier.fillMaxWidth(),
        )

        // Error message
        state.error?.let { errorMessage ->
            Text(
                text = errorMessage,
                color = MindTagColors.Error,
                style = MaterialTheme.typography.bodySmall,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.md))

        // Submit button
        if (state.isLoading) {
            Box(
                modifier = Modifier.fillMaxWidth().height(56.dp),
                contentAlignment = Alignment.Center,
            ) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MindTagColors.Primary,
                    strokeWidth = 2.dp,
                )
            }
        } else {
            MindTagButton(
                text = if (state.isLoginMode) "Log in" else "Register",
                onClick = onSubmit,
                variant = MindTagButtonVariant.PrimaryLarge,
            )
        }

        // Toggle login/register
        Box(
            modifier = Modifier.fillMaxWidth(),
            contentAlignment = Alignment.Center,
        ) {
            TextButton(onClick = onToggleMode) {
                Text(
                    text = if (state.isLoginMode) "Don't have an account? Register"
                           else "Already have an account? Log in",
                    color = MindTagColors.TextSecondary,
                    style = MaterialTheme.typography.bodySmall,
                )
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))
    }
}
