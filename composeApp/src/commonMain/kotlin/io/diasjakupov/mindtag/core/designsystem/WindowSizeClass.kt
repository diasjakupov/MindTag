package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.runtime.staticCompositionLocalOf

enum class WindowSizeClass { Compact, Medium, Expanded }

val LocalWindowSizeClass = staticCompositionLocalOf { WindowSizeClass.Compact }
