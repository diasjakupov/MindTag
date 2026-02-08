package io.diasjakupov.mindtag.core.designsystem.components

import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.widthIn
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes

@Composable
fun MindTagButton(
    text: String,
    onClick: () -> Unit,
    modifier: Modifier = Modifier,
    enabled: Boolean = true,
    variant: MindTagButtonVariant = MindTagButtonVariant.PrimaryLarge,
) {
    val config = when (variant) {
        MindTagButtonVariant.PrimaryLarge -> ButtonConfig(
            height = 56.dp,
            shape = MindTagShapes.lg,
            containerColor = MindTagColors.Primary,
            contentColor = Color.White,
            textStyle = MaterialTheme.typography.titleMedium,
        )
        MindTagButtonVariant.PrimaryMedium -> ButtonConfig(
            height = 40.dp,
            shape = MindTagShapes.md,
            containerColor = MindTagColors.Primary,
            contentColor = Color.White,
            textStyle = MaterialTheme.typography.labelLarge,
        )
        MindTagButtonVariant.Secondary -> ButtonConfig(
            height = 36.dp,
            shape = MindTagShapes.md,
            containerColor = MindTagColors.SurfaceDark,
            contentColor = Color.White,
            textStyle = MaterialTheme.typography.labelLarge,
        )
        MindTagButtonVariant.Pill -> ButtonConfig(
            height = 40.dp,
            shape = MindTagShapes.full,
            containerColor = MindTagColors.Primary,
            contentColor = Color.White,
            textStyle = MaterialTheme.typography.labelLarge,
        )
    }

    val finalModifier = when (variant) {
        MindTagButtonVariant.PrimaryLarge -> modifier.fillMaxWidth().height(config.height)
        MindTagButtonVariant.Pill -> modifier.widthIn(max = 200.dp).height(config.height)
        else -> modifier.height(config.height)
    }

    Button(
        onClick = onClick,
        modifier = finalModifier,
        enabled = enabled,
        shape = config.shape,
        colors = ButtonDefaults.buttonColors(
            containerColor = config.containerColor,
            contentColor = config.contentColor,
        ),
        contentPadding = PaddingValues(horizontal = 24.dp, vertical = 0.dp),
    ) {
        Text(text = text, style = config.textStyle)
    }
}

enum class MindTagButtonVariant {
    PrimaryLarge,
    PrimaryMedium,
    Secondary,
    Pill,
}

private class ButtonConfig(
    val height: Dp,
    val shape: Shape,
    val containerColor: Color,
    val contentColor: Color,
    val textStyle: TextStyle,
)
