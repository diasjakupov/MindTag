# Feature: Profile

## Overview

The Profile screen displays user information, study statistics, and settings links. Currently a static UI shell with hardcoded data; awaiting authentication and user service integration.

## Implementation Status

**Shell only.** All state values are hardcoded defaults. The ViewModel's `onIntent()` is a no-op. No repository or domain layer.

## MVI Contract (`ProfileContract`)

### State

```kotlin
data class State(
    val userName: String = "Alex Johnson",
    val email: String = "alex.johnson@university.edu",
    val totalNotes: Int = 15,
    val totalStudySessions: Int = 8,
    val currentStreak: Int = 7,
    val totalXp: Int = 4030,
    val memberSince: String = "January 2026",
)
```

### Intents

| Intent | Status |
|--------|--------|
| `TapEditProfile` | No-op |
| `TapNotifications` | No-op |
| `TapAppearance` | No-op |
| `TapAbout` | No-op |
| `TapLogout` | No-op |

### Effects

None defined (empty sealed interface).

## Screen Components

### Layout

```
Column (verticalScroll)
+-- Header: "Profile"
+-- Avatar Circle (initials from first two name parts)
+-- User Info: name, email, "Member since {date}"
+-- Stats Card (4 items)
+-- Settings Card (5 rows)
```

### Stats Card

| Stat | Value Source | Color |
|------|-------------|-------|
| Total Notes | `state.totalNotes` | Default |
| Total Sessions | `state.totalStudySessions` | Default |
| Current Streak | `state.currentStreak` | Orange (Warning) |
| Total XP | `formatNumber(state.totalXp)` | Default |

`formatNumber()` converts 1000+ to "k" notation (e.g., 4030 -> "4k").

### Settings Rows

| Setting | Icon | Special |
|---------|------|---------|
| Edit Profile | Person | Chevron right |
| Notifications | Notifications | Chevron right |
| Appearance | Palette | Chevron right |
| About MindTag | Info | Chevron right |
| Log Out | Logout | Red tint, no chevron |

Dividers separate each row. All rows fire their respective intent on tap.

## File Paths

| Layer | File |
|-------|------|
| MVI Contract | `feature/profile/presentation/ProfileContract.kt` |
| ViewModel | `feature/profile/presentation/ProfileViewModel.kt` |
| Screen | `feature/profile/presentation/ProfileScreen.kt` |
