package io.diasjakupov.mindtag.core.designsystem.components

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.BasicTextField
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.SolidColor
import androidx.compose.ui.unit.dp
import io.diasjakupov.mindtag.core.designsystem.MindTagColors
import io.diasjakupov.mindtag.core.designsystem.MindTagIcons
import io.diasjakupov.mindtag.core.designsystem.MindTagShapes
import io.diasjakupov.mindtag.core.designsystem.MindTagSpacing

@Composable
fun MindTagSearchBar(
    query: String,
    onQueryChange: (String) -> Unit,
    modifier: Modifier = Modifier,
    placeholder: String = "Search",
) {
    BasicTextField(
        value = query,
        onValueChange = onQueryChange,
        modifier = modifier
            .fillMaxWidth()
            .height(48.dp)
            .clip(MindTagShapes.md)
            .background(MindTagColors.SearchBarBg),
        singleLine = true,
        textStyle = MaterialTheme.typography.bodyLarge.copy(
            color = MindTagColors.TextPrimary,
        ),
        cursorBrush = SolidColor(MindTagColors.Primary),
        decorationBox = { innerTextField ->
            Row(
                modifier = Modifier.padding(horizontal = MindTagSpacing.lg),
                verticalAlignment = Alignment.CenterVertically,
            ) {
                Icon(
                    imageVector = MindTagIcons.Search,
                    contentDescription = null,
                    modifier = Modifier.size(24.dp),
                    tint = MindTagColors.TextSecondary,
                )
                Spacer(Modifier.width(MindTagSpacing.md))
                Box(modifier = Modifier.weight(1f)) {
                    if (query.isEmpty()) {
                        Text(
                            text = placeholder,
                            style = MaterialTheme.typography.bodyLarge,
                            color = MindTagColors.TextSecondary,
                        )
                    }
                    innerTextField()
                }
            }
        },
    )
}
