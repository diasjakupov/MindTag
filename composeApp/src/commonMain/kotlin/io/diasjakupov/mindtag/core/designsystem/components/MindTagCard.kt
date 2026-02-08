package io.diasjakupov.mindtag.core.designsystem.components

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing

@Composable
fun MindTagCard(
    modifier: Modifier = Modifier,
    contentPadding: Dp = MindTagSpacing.lg,
    onClick: (() -> Unit)? = null,
    content: @Composable ColumnScope.() -> Unit,
) {
    val cardModifier = modifier
    val border = BorderStroke(1.dp, MindTagColors.BorderSubtle)

    if (onClick != null) {
        Surface(
            onClick = onClick,
            modifier = cardModifier,
            shape = MindTagShapes.lg,
            color = MindTagColors.CardDark,
            border = border,
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    } else {
        Surface(
            modifier = cardModifier,
            shape = MindTagShapes.lg,
            color = MindTagColors.CardDark,
            border = border,
        ) {
            Column(modifier = Modifier.padding(contentPadding)) {
                content()
            }
        }
    }
}
