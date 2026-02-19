# Feature: Home Dashboard

## Status: REMOVED

This feature was removed from the codebase in commit `92d94d6` ("refactor: delete home, planner, profile, onboarding features for MVP") on 2026-02-14.

The Home Dashboard is not part of the current MVP. The app's main entry point after authentication is the Library screen (`Route.Library`), not a dedicated Home dashboard.

## What Was Removed

The following files were deleted:

| Layer | Former Path |
|-------|-------------|
| Data | `feature/home/data/repository/DashboardRepositoryImpl.kt` |
| Domain Model | `feature/home/domain/model/DashboardData.kt` |
| Domain Model | `feature/home/domain/model/ReviewCard.kt` |
| Domain Repository | `feature/home/domain/repository/DashboardRepository.kt` |
| Domain Use Case | `feature/home/domain/usecase/GetDashboardUseCase.kt` |
| MVI Contract | `feature/home/presentation/HomeContract.kt` |
| ViewModel | `feature/home/presentation/HomeViewModel.kt` |
| Screen | `feature/home/presentation/HomeScreen.kt` |

## Current Navigation

The app no longer has a Home route. After authentication (`AuthState.Authenticated`), the app launches into `MainApp()` with `Route.Library` as the start destination. The bottom navigation bar contains two top-level routes: `Route.Library` and `Route.Study`.

## Restoration

To restore this feature, the deleted code can be recovered from git history using:

```shell
git show 92d94d6~1:composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/home/<file>
```

A `Route.Home` would need to be added back to `Route.kt`, the navigation entry registered in `App.kt`, and the bottom bar updated in `NavConfig.kt` / `MindTagBottomBar.kt`.
