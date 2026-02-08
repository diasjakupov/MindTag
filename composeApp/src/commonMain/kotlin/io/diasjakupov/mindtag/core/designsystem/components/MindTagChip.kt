package io.diasjakupov.mindtag.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing

@Composable
fun MindTagChip(
    text: String,
    modifier: Modifier = Modifier,
    variant: MindTagChipVariant = MindTagChipVariant.Metadata,
) {
    val (bgColor, textColor, shape, hPad, vPad, style, uppercase) = when (variant) {
        MindTagChipVariant.SubjectTag -> ChipConfig(
            bgColor = MindTagColors.OverlayBgLight,
            textColor = Color.White,
            shape = MindTagShapes.default,
            hPad = MindTagSpacing.md,
            vPad = MindTagSpacing.xxs,
            style = MaterialTheme.typography.labelMedium,
            uppercase = false,
        )
        MindTagChipVariant.Metadata -> ChipConfig(
            bgColor = MindTagColors.SearchBarBg,
            textColor = Color.White,
            shape = MindTagShapes.md,
            hPad = MindTagSpacing.lg,
            vPad = MindTagSpacing.xs,
            style = MaterialTheme.typography.labelMedium.copy(fontWeight = FontWeight.SemiBold),
            uppercase = true,
        )
        MindTagChipVariant.Status -> ChipConfig(
            bgColor = Color.White.copy(alpha = 0.2f),
            textColor = Color.White,
            shape = MindTagShapes.default,
            hPad = MindTagSpacing.md,
            vPad = MindTagSpacing.xs,
            style = MaterialTheme.typography.labelMedium,
            uppercase = false,
        )
        MindTagChipVariant.WeekLabel -> ChipConfig(
            bgColor = MindTagColors.Primary.copy(alpha = 0.1f),
            textColor = MindTagColors.Primary,
            shape = MindTagShapes.full,
            hPad = MindTagSpacing.md,
            vPad = MindTagSpacing.xxs,
            style = MaterialTheme.typography.labelSmall.copy(fontWeight = FontWeight.Bold),
            uppercase = true,
        )
    }

    Box(
        modifier = modifier
            .clip(shape)
            .background(bgColor)
            .padding(horizontal = hPad, vertical = vPad),
    ) {
        Text(
            text = if (uppercase) text.uppercase() else text,
            style = style,
            color = textColor,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

enum class MindTagChipVariant {
    SubjectTag,
    Metadata,
    Status,
    WeekLabel,
}

private data class ChipConfig(
    val bgColor: Color,
    val textColor: Color,
    val shape: androidx.compose.ui.graphics.Shape,
    val hPad: androidx.compose.ui.unit.Dp,
    val vPad: androidx.compose.ui.unit.Dp,
    val style: androidx.compose.ui.text.TextStyle,
    val uppercase: Boolean,
)
