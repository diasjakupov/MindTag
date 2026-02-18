package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.runtime.compositionLocalOf

enum class WindowSizeClass { Compact, Medium, Expanded }

val LocalWindowSizeClass = compositionLocalOf { WindowSizeClass.Compact }
