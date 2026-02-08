package io.diasjakupov.mindtag.core.designsystem

import androidx.compose.ui.graphics.Color

object MindTagColors {
    // Core theme
    val Primary = Color(0xFF135BEC)
    val PrimaryDark = Color(0xFF0F4BC4)
    val BackgroundDark = Color(0xFF101622)
    val BackgroundLight = Color(0xFFF6F6F8)
    val SurfaceDark = Color(0xFF1C2333)
    val SurfaceDarkAlt = Color(0xFF1E2736)
    val SurfaceDarkAlt2 = Color(0xFF1F2937)
    val SurfaceLight = Color(0xFFFFFFFF)
    val CardDark = Color(0xFF192233)
    val CardLight = Color(0xFFFFFFFF)

    // Text
    val TextPrimary = Color(0xFFFFFFFF)
    val TextPrimaryLight = Color(0xFF111418)
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
    val WarningBgDark = Color(0x4D7C2D12) // orange-900/30
    val WarningBgLight = Color(0xFFFFEDD5) // orange-100
    val Info = Color(0xFF3B82F6) // blue-500
    val InfoBg = Color(0x1A3B82F6) // blue-500/10
    val AccentPurple = Color(0xFFA855F7) // purple-500
    val AccentPurpleBg = Color(0x1AA855F7) // purple-500/10
    val AccentPurpleBgLight = Color(0xFFF3E8FF) // purple-100
    val AccentTealDark = Color(0xFF2DD4BF) // teal-400
    val AccentTealLight = Color(0xFF0D9488) // teal-600
    val ProgressYellow = Color(0xFFEAB308) // yellow-500
    val ProgressRed = Color(0xFFEF4444) // red-500

    // Surface & Border
    val BorderSubtle = Color(0x0DFFFFFF) // white/5
    val BorderMedium = Color(0xFF1E293B) // slate-800
    val BorderLight = Color(0xFFE2E8F0) // slate-200
    val Divider = Color(0x1AFFFFFF) // white/10
    val OverlayBg = Color(0x99000000) // black/60
    val OverlayBgLight = Color(0x66000000) // black/40
    val InactiveDot = Color(0xFF324467)

    // Graph visualization
    val GraphBg = Color(0xFF0F1115)
    val GraphGrid = Color(0x33334155) // slate-700 at 20%
    val NodeBg = Color(0xFF1E293B) // slate-800
    val NodeBorder = Color(0xFF334155) // slate-700
    val NodeBorderLight = Color(0xFF475569) // slate-600
    val NodeSelectedGlow = Color(0x4D135BEC) // primary/30
    val EdgeDefault = Color(0xFF334155) // slate-700
    val EdgeActive = Color(0xCC135BEC) // primary at 0.8 opacity
    val EdgeWeak = Color(0xFF334155)

    // Component-specific
    val SearchBarBg = Color(0xFF232F48)
    val BottomNavBg = Color(0xF2111722) // #111722/95
    val SegmentedControlBg = Color(0xFF232F48)
    val SegmentedControlActiveBg = Color(0xFF111722)
    val QuizProgressTrack = Color(0xFF324467)
}
