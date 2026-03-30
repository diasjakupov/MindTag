package io.diasjakupov.mindtag.core.config

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp

/**
 * A slim, non-intrusive banner shown at the very bottom of the screen when
 * [AppEnvironment.TEST] mode is active (it is invisible in NETWORK mode).
 *
 * Long-press the banner to open the [EnvironmentSwitcherDialog] which shows the
 * current mode. To change it, update [DevConfig.DEFAULT_ENVIRONMENT] and restart.
 *
 * Place this composable at the root level so it overlays all content:
 *
 * ```kotlin
 * Box(Modifier.fillMaxSize()) {
 *     MainContent()
 *     EnvironmentBanner(Modifier.align(Alignment.BottomCenter))
 * }
 * ```
 */
@OptIn(ExperimentalFoundationApi::class)
@Composable
fun EnvironmentBanner(modifier: Modifier = Modifier) {
    val mode by EnvironmentStore.mode.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    AnimatedVisibility(
        visible = mode == AppEnvironment.TEST,
        enter = fadeIn(),
        exit = fadeOut(),
        modifier = modifier,
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = { /* single-tap does nothing */ },
                    onLongClick = { showDialog = true },
                )
                .background(Color(0xFFEF4444)) // red-500
                .padding(vertical = 4.dp, horizontal = 12.dp),
            contentAlignment = Alignment.Center,
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                // Pulsing dot
                Box(
                    modifier = Modifier
                        .size(8.dp)
                        .clip(CircleShape)
                        .background(Color.White),
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = "TEST MODE — Long-press for info",
                    color = Color.White,
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Medium,
                    letterSpacing = 0.5.sp,
                )
            }
        }
    }

    if (showDialog) {
        EnvironmentSwitcherDialog(onDismiss = { showDialog = false })
    }
}

/**
 * Read-only dialog that shows the current environment mode.
 *
 * The active mode is set at cold start via [DevConfig.DEFAULT_ENVIRONMENT] and cannot
 * be toggled at runtime (Koin `single` bindings are resolved once and cannot be re-wired
 * without a process restart). To change the mode, update [DevConfig.DEFAULT_ENVIRONMENT]
 * and restart the app.
 */
@Composable
fun EnvironmentSwitcherDialog(onDismiss: () -> Unit) {
    val mode by EnvironmentStore.mode.collectAsState()
    val isTestMode = mode == AppEnvironment.TEST

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C2333),
        title = {
            Text(
                text = "Environment",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Status badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isTestMode) Color(0x33EF4444) else Color(0x3322C55E),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (isTestMode) "TEST" else "NETWORK",
                        color = if (isTestMode) Color(0xFFEF4444) else Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                    )
                }

                Text(
                    text = if (isTestMode)
                        "Stub data — no HTTP calls are made."
                    else
                        "Real backend API is active.",
                    color = Color(0xFF92A4C9),
                    style = MaterialTheme.typography.bodyMedium,
                )

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(Color(0x22FFFFFF))
                        .padding(10.dp),
                ) {
                    Text(
                        text = "To switch modes, change `DEFAULT_ENVIRONMENT` in `DevConfig.kt` and restart the app.",
                        color = Color(0xFF92A4C9),
                        fontSize = 11.sp,
                        textAlign = TextAlign.Start,
                    )
                }
            }
        },
        confirmButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135BEC)),
            ) {
                Text("Done", color = Color.White)
            }
        },
    )
}
