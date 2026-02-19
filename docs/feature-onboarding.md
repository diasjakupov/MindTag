# Feature: Onboarding

## Status: REMOVED

This feature was removed from the codebase in commit `92d94d6` ("refactor: delete home, planner, profile, onboarding features for MVP") on 2026-02-14.

The Onboarding flow is not part of the current MVP. The app now starts with the `AuthScreen` (login/register) when unauthenticated, and transitions directly to the main app (`MainApp()` with `Route.Library`) upon successful authentication. There is no intermediate onboarding flow.

## What Was Removed

The following files were deleted:

| Layer | Former Path |
|-------|-------------|
| MVI Contract | `feature/onboarding/presentation/OnboardingContract.kt` |
| ViewModel | `feature/onboarding/presentation/OnboardingViewModel.kt` |
| Screen | `feature/onboarding/presentation/OnboardingScreen.kt` |

The Onboarding had no domain or data layer -- it was a presentation-only feature with a 4-page `HorizontalPager`.

## Current Authentication Flow

```
App()
  |
  +-- AuthState.Unauthenticated --> AuthScreen (login/register)
  |
  +-- AuthState.Authenticated   --> MainApp() (Route.Library as start destination)
```

There is no onboarding step between authentication and the main app.

## Restoration

To restore this feature, the deleted code can be recovered from git history using:

```shell
git show 92d94d6~1:composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/feature/onboarding/<file>
```

The onboarding flow would need to be integrated into the authentication flow in `App.kt`, likely using a preference flag (e.g., via `AppPreferences`) to track whether the user has completed onboarding.
