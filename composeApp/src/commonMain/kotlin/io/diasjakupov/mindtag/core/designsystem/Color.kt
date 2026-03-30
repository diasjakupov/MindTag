package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.ui.graphics.Color

object MindTagColors {
    // Core theme
    val Primary = Color(0xFF135BEC)
    val BackgroundDark = Color(0xFF101622)
    val SurfaceDark = Color(0xFF1C2333)
    val SurfaceDarkAlt = Color(0xFF1E2736)
    val SurfaceDarkAlt2 = Color(0xFF1F2937)
    val CardDark = Color(0xFF192233)

    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextSecondary = Color(0xFF92A4C9)
    val TextTertiary = Color(0xFF94A3B8) // slate-400
    val TextSlate300 = Color(0xFFCBD5E1) // slate-300
    val TextSlate500 = Color(0xFF64748B) // slate-500

    // Semantic / Status
    val Success = Color(0xFF22C55E) // green-500
    val SuccessBg = Color(0x1A22C55E) // green-500/10
    val Error = Color(0xFFEF4444) // red-500
    val ErrorBg = Color(0x1AEF4444) // red-500/10
    val Warning = Color(0xFFF97316) // orange-500
    val Info = Color(0xFF3B82F6) // blue-500
    val AccentPurple = Color(0xFFA855F7) // purple-500
    val ProgressYellow = Color(0xFFEAB308) // yellow-500

    // Surface & Border
    val BorderSubtle = Color(0x0DFFFFFF) // white/5
    val BorderMedium = Color(0xFF1E293B) // slate-800
    val OverlayBgLight = Color(0x66000000) // black/40
    val InactiveDot = Color(0xFF324467)

    // Graph visualization
    val GraphBg = Color(0xFF0F1115)
    val GraphGrid = Color(0x33334155) // slate-700 at 20%
    val NodeBorder = Color(0xFF334155) // slate-700
    val EdgeDefault = Color(0xFF334155) // slate-700
    val EdgeActive = Color(0xCC135BEC) // primary at 0.8 opacity

    // Component-specific
    val SearchBarBg = Color(0xFF232F48)
    val BottomNavBg = Color(0xF2111722) // #111722/95
    val SegmentedControlBg = Color(0xFF232F48)
    val SegmentedControlActiveBg = Color(0xFF111722)
    val QuizProgressTrack = Color(0xFF324467)
}
