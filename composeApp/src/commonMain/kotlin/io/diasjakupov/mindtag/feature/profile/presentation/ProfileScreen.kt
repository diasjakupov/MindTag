package io.diasjakupov.mindtag.feature.profile.presentation

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.outlined.Logout
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material.icons.outlined.Info
import androidx.compose.material.icons.outlined.Notifications
import androidx.compose.material.icons.outlined.Palette
import androidx.compose.material.icons.outlined.Person
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing
import io.diasjakupov.mindtag.core.designsystem.components.MindTagCard
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(MindTagColors.BackgroundDark)
            .verticalScroll(rememberScrollState())
            .padding(horizontal = MindTagSpacing.screenHorizontalPadding),
    ) {
        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        Text(
            text = "Profile",
            style = MaterialTheme.typography.headlineMedium,
            color = Color.White,
        )

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Box(
                modifier = Modifier
                    .size(80.dp)
                    .clip(CircleShape)
                    .background(MindTagColors.Primary),
                contentAlignment = Alignment.Center,
            ) {
                Text(
                    text = state.userName.split(" ")
                        .take(2)
                        .mapNotNull { it.firstOrNull()?.uppercase() }
                        .joinToString(""),
                    style = MaterialTheme.typography.headlineMedium,
                    color = Color.White,
                )
            }

            Spacer(modifier = Modifier.height(MindTagSpacing.xl))

            Text(
                text = state.userName,
                style = MaterialTheme.typography.titleLarge,
                color = Color.White,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xs))

            Text(
                text = state.email,
                style = MaterialTheme.typography.bodyMedium,
                color = MindTagColors.TextSecondary,
            )

            Spacer(modifier = Modifier.height(MindTagSpacing.xs))

            Text(
                text = "Member since ${state.memberSince}",
                style = MaterialTheme.typography.bodySmall,
                color = MindTagColors.TextTertiary,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        MindTagCard(modifier = Modifier.fillMaxWidth()) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceEvenly,
            ) {
                StatItem(value = state.totalNotes.toString(), label = "Notes")
                StatItem(value = state.totalStudySessions.toString(), label = "Sessions")
                StatItem(value = state.currentStreak.toString(), label = "Streak", valueColor = Color(0xFFFF9800))
                StatItem(value = formatNumber(state.totalXp), label = "XP")
            }
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.xxxl))

        MindTagCard(
            modifier = Modifier.fillMaxWidth(),
            contentPadding = 0.dp,
        ) {
            SettingsRow(
                icon = Icons.Outlined.Person,
                label = "Edit Profile",
                onClick = { viewModel.onIntent(ProfileContract.Intent.TapEditProfile) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Notifications,
                label = "Notifications",
                onClick = { viewModel.onIntent(ProfileContract.Intent.TapNotifications) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Palette,
                label = "Appearance",
                onClick = { viewModel.onIntent(ProfileContract.Intent.TapAppearance) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.Outlined.Info,
                label = "About MindTag",
                onClick = { viewModel.onIntent(ProfileContract.Intent.TapAbout) },
            )
            SettingsDivider()
            SettingsRow(
                icon = Icons.AutoMirrored.Outlined.Logout,
                label = "Log Out",
                onClick = { viewModel.onIntent(ProfileContract.Intent.TapLogout) },
                tint = MindTagColors.Error,
            )
        }

        Spacer(modifier = Modifier.height(MindTagSpacing.bottomContentPadding))
    }
}

@Composable
private fun StatItem(value: String, label: String, valueColor: Color = Color.White) {
    Column(horizontalAlignment = Alignment.CenterHorizontally) {
        Text(
            text = value,
            style = MaterialTheme.typography.titleLarge,
            color = valueColor,
        )
        Spacer(modifier = Modifier.height(MindTagSpacing.xs))
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MindTagColors.TextSecondary,
            textAlign = TextAlign.Center,
        )
    }
}

@Composable
private fun SettingsRow(
    icon: ImageVector,
    label: String,
    onClick: () -> Unit,
    tint: Color = MindTagColors.TextSecondary,
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = MindTagSpacing.xl, vertical = MindTagSpacing.lg),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            modifier = Modifier.size(22.dp),
            tint = tint,
        )
        Spacer(modifier = Modifier.width(MindTagSpacing.xl))
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = if (tint == MindTagColors.Error) MindTagColors.Error else Color.White,
            modifier = Modifier.weight(1f),
        )
        Icon(
            imageVector = Icons.Outlined.ChevronRight,
            contentDescription = null,
            modifier = Modifier.size(20.dp),
            tint = MindTagColors.TextTertiary,
        )
    }
}

@Composable
private fun SettingsDivider() {
    HorizontalDivider(
        modifier = Modifier.padding(horizontal = MindTagSpacing.xl),
        thickness = 0.5.dp,
        color = MindTagColors.Divider,
    )
}

private fun formatNumber(n: Int): String {
    return if (n >= 1000) {
        val thousands = n / 1000
        val remainder = (n % 1000) / 100
        if (remainder > 0) "${thousands}.${remainder}k" else "${thousands}k"
    } else {
        n.toString()
    }
}
