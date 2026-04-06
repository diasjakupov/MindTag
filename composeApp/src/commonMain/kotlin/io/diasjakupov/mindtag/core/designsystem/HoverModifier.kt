package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.tween
import androidx.compose.foundation.border
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.ExperimentalComposeUiApi
import androidx.compose.ui.Modifier
import androidx.compose.ui.composed
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.input.pointer.PointerEventType
import androidx.compose.ui.input.pointer.onPointerEvent
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp

@OptIn(ExperimentalComposeUiApi::class)
fun Modifier.hoverBorder(
    hoverColor: Color = MindTagColors.Primary.copy(alpha = 0.4f),
    defaultColor: Color = Color.Transparent,
    borderWidth: Dp = 1.dp,
    shape: Shape = MindTagShapes.lg,
): Modifier = composed {
    var isHovered by remember { mutableStateOf(false) }
    val borderColor by animateColorAsState(
        targetValue = if (isHovered) hoverColor else defaultColor,
        animationSpec = tween(150),
    )

    this
        .onPointerEvent(PointerEventType.Enter) { isHovered = true }
        .onPointerEvent(PointerEventType.Exit) { isHovered = false }
        .border(borderWidth, borderColor, shape)
}
