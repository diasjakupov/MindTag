# Feature: Profile

## Status: REMOVED

This feature was removed from the codebase in commit `92d94d6` ("refactor: delete home, planner, profile, onboarding features for MVP") on 2026-02-14.

The Profile screen is not part of the current MVP. There is no `Route.Profile` in the navigation graph, and no profile-related code exists in the project.

## What Was Removed

The following files were deleted:

| Layer | Former Path |
|-------|-------------|
| MVI Contract | `feature/profile/presentation/ProfileContract.kt` |
| ViewModel | `feature/profile/presentation/ProfileViewModel.kt` |
| Screen | `feature/profile/presentation/ProfileScreen.kt` |

The Profile had no domain or data layer -- it was a static UI shell with all state values hardcoded in the ViewModel defaults.

## Current Navigation

The app does not include a Profile tab. The bottom navigation bar contains two top-level routes: `Route.Library` and `Route.Study`.

## Restoration

To restore this feature, the deleted code can be recovered from git history using:

```shell
git show 92d94d6~1:composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/profile/<file>
```

A `Route.Profile` would need to be added back to `Route.kt`, the navigation entry registered in `App.kt`, and the bottom bar updated in `NavConfig.kt` / `MindTagBottomBar.kt`.
