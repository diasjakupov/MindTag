package io.diasjakupov.mindtag.core.config

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.di.authModule
import io.diasjakupov.mindtag.core.di.repositoryModule
import io.diasjakupov.mindtag.core.network.AuthManager
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform

@Composable
fun EnvironmentBanner(modifier: Modifier = Modifier) {
    val mode by EnvironmentStore.mode.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val isTestMode = mode == AppEnvironment.TEST
    val bgColor = if (isTestMode) Color(0xFFEF4444) else Color(0xFF22C55E)
    val label = if (isTestMode) "TEST MODE" else "NETWORK"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { showDialog = true }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$label — Tap to switch",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            )
        }
    }

    if (showDialog) {
        EnvironmentSwitcherDialog(onDismiss = { showDialog = false })
    }
}

@Composable
fun EnvironmentSwitcherDialog(onDismiss: () -> Unit) {
    val mode by EnvironmentStore.mode.collectAsState()
    val isTestMode = mode == AppEnvironment.TEST
    val targetLabel = if (isTestMode) "NETWORK" else "TEST"
    val authManager: AuthManager = koinInject()

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
                    text = "Switch to $targetLabel mode? All screens will reset.",
                    color = Color(0xFF92A4C9),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    EnvironmentStore.switchMode(
                        reloadModules = {
                            val koin = KoinPlatform.getKoin()
                            koin.loadModules(
                                listOf(authModule, repositoryModule),
                                allowOverride = true,
                            )
                        },
                        onSwitched = { newMode ->
                            when (newMode) {
                                AppEnvironment.TEST -> authManager.login("stub-jwt-token-for-test-mode", 1L)
                                AppEnvironment.NETWORK -> authManager.logout()
                            }
                        },
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135BEC)),
            ) {
                Text("Switch to $targetLabel", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("Cancel", color = Color(0xFF92A4C9))
            }
        },
    )
}
