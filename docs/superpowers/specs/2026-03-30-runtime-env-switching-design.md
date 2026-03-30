# Runtime Environment Switching

## Problem

The environment switching system (`EnvironmentStore`, stubs, banner) exists but is effectively a compile-time toggle. Koin `single` bindings are resolved once at startup, so calling `EnvironmentStore.setMode()` at runtime has no effect on already-created repository singletons. The banner is only visible in TEST mode, making it impossible to switch to TEST without editing `DevConfig.kt` and restarting.

## Solution

Tap-to-switch banner always visible in both modes. On toggle, reload Koin modules and reset auth state to force a full screen recomposition.

## Components

### 1. EnvironmentBanner (modify existing)

**File:** `core/config/EnvironmentBanner.kt`

- Always visible (remove the `AnimatedVisibility` TEST-only gate)
- Green banner with "NETWORK" label when in NETWORK mode
- Red banner with "TEST MODE" label when in TEST mode
- Tap opens a confirmation dialog: "Switch to TEST/NETWORK mode?"
- On confirm: calls `EnvironmentStore.switchMode()`

### 2. EnvironmentStore.switchMode() (new method)

**File:** `core/config/AppEnvironment.kt`

New method on `EnvironmentStore` that:
1. Flips the mode (`TEST ↔ NETWORK`)
2. Reloads Koin modules with `allowOverride = true`
3. Handles auth state transition:
   - To TEST: calls `authManager.login("stub-jwt-token-for-test-mode", 1L)`
   - To NETWORK: calls `authManager.logout()`

This triggers `AuthState` change, which causes `App()` to recompose (showing AuthScreen or MainApp), effectively resetting all screens with freshly-injected repositories.

### 3. Koin Module Reload

**Mechanism:** `KoinPlatform.getKoin().loadModules(listOf(authModule, repositoryModule), allowOverride = true)`

Only `authModule` and `repositoryModule` need reloading — they contain the `if (isTestMode)` branches. `databaseModule`, `networkModule`, `useCaseModule`, and `viewModelModule` don't depend on the environment mode.

### 4. EnvironmentSwitcherDialog (modify existing)

Replace the read-only info dialog with a confirmation dialog:
- Shows current mode
- "Switch to {other mode}?" prompt
- Confirm / Cancel buttons
- On confirm: calls `EnvironmentStore.switchMode()`

## Flow

```
Tap banner
  → EnvironmentSwitcherDialog opens
  → User taps "Switch"
  → EnvironmentStore.switchMode():
      1. _mode.value = newMode
      2. koin.loadModules([authModule, repositoryModule], allowOverride=true)
      3. if TEST: authManager.login(stub) → AuthState.Authenticated
         if NETWORK: authManager.logout() → AuthState.Unauthenticated
  → App recomposes due to AuthState change
  → All screens recreated with new repository bindings
```

## Files Changed

| File | Change |
|------|--------|
| `core/config/AppEnvironment.kt` | Add `switchMode()` with Koin reload + auth transition |
| `core/config/EnvironmentBanner.kt` | Always visible, tap to open dialog, replace dialog with confirm action |
| `core/di/Modules.kt` | No change needed — `isTestMode` is already read dynamically in the `single` lambdas; reloading re-executes them |

## What Stays Unchanged

- `StubAuthRepository`, `StubNoteRepository`, `StubBackendQuizRepository` — already fully implemented
- `DevConfig.DEFAULT_ENVIRONMENT` — still controls cold-start default
- `AuthManager` — existing `login()`/`logout()` methods are sufficient
- All ViewModels, use cases, API classes — untouched

## Edge Cases

- **Mid-quiz switch:** User is in a quiz attempt and switches. Navigation resets (auth state change), so the attempt is abandoned. This is acceptable for a dev tool.
- **Token persistence:** `StubAuthRepository` calls `authManager.login()` which persists the stub token via `TokenStorage`. On next cold start with NETWORK mode, the stub token would be restored but fail backend calls. This is fine — the user would just need to re-login. The `DEFAULT_ENVIRONMENT` constant controls cold-start behavior independently.
