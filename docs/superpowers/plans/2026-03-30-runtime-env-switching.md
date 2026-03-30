# Runtime Environment Switching Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** Make the environment toggle (TEST/NETWORK) work at runtime via a tap-to-switch banner, reloading Koin modules and resetting auth state so all screens get fresh repositories.

**Architecture:** `EnvironmentStore` gets a `switchMode()` method that flips the mode, reloads Koin's `authModule` and `repositoryModule` with `allowOverride = true`, and transitions auth state. `EnvironmentBanner` becomes always-visible with a tap-to-confirm-switch dialog.

**Tech Stack:** Kotlin/KMP, Koin DI, Compose Multiplatform, kotlinx StateFlow

---

### Task 1: Add `switchMode()` to EnvironmentStore

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/AppEnvironment.kt`

- [ ] **Step 1: Add the switchMode method**

`EnvironmentStore` needs to accept a Koin reload callback and an `AuthManager` reference. Since `EnvironmentStore` is a plain `object` (not Koin-managed), the cleanest approach is to pass a lambda at switch time. Add this method:

```kotlin
// In AppEnvironment.kt, inside object EnvironmentStore:

/**
 * Switch to the other environment mode at runtime.
 *
 * @param reloadModules lambda that reloads the Koin modules with allowOverride=true
 * @param onSwitched lambda called after mode is set — use to transition auth state
 */
fun switchMode(
    reloadModules: () -> Unit,
    onSwitched: (newMode: AppEnvironment) -> Unit,
) {
    val newMode = if (current == AppEnvironment.TEST) AppEnvironment.NETWORK else AppEnvironment.TEST
    _mode.value = newMode
    reloadModules()
    onSwitched(newMode)
}
```

- [ ] **Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/AppEnvironment.kt
git commit -m "feat: add switchMode() to EnvironmentStore for runtime env switching"
```

---

### Task 2: Rewrite EnvironmentBanner to always-visible with tap-to-switch

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/EnvironmentBanner.kt`

- [ ] **Step 1: Replace EnvironmentBanner composable**

Remove the `AnimatedVisibility` TEST-only gate. Make the banner always visible with mode-dependent colors. Replace `combinedClickable` with a simple `clickable` that opens the dialog.

```kotlin
@Composable
fun EnvironmentBanner(modifier: Modifier = Modifier) {
    val mode by EnvironmentStore.mode.collectAsState()
    var showDialog by remember { mutableStateOf(false) }

    val isTestMode = mode == AppEnvironment.TEST
    val bgColor = if (isTestMode) Color(0xFFEF4444) else Color(0xFF22C55E)
    val label = if (isTestMode) "TEST MODE" else "NETWORK"

    Box(
        modifier = modifier
            .fillMaxWidth()
            .background(bgColor)
            .clickable { showDialog = true }
            .padding(vertical = 4.dp, horizontal = 12.dp),
        contentAlignment = Alignment.Center,
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(Color.White),
            )
            Spacer(Modifier.width(6.dp))
            Text(
                text = "$label — Tap to switch",
                color = Color.White,
                fontSize = 11.sp,
                fontWeight = FontWeight.Medium,
                letterSpacing = 0.5.sp,
            )
        }
    }

    if (showDialog) {
        EnvironmentSwitcherDialog(onDismiss = { showDialog = false })
    }
}
```

- [ ] **Step 2: Replace EnvironmentSwitcherDialog with confirm-to-switch dialog**

The dialog shows the current mode and a button to switch. On confirm it calls `EnvironmentStore.switchMode()` with the Koin reload lambda and auth transition lambda.

```kotlin
@Composable
fun EnvironmentSwitcherDialog(onDismiss: () -> Unit) {
    val mode by EnvironmentStore.mode.collectAsState()
    val isTestMode = mode == AppEnvironment.TEST
    val targetLabel = if (isTestMode) "NETWORK" else "TEST"
    val authManager: AuthManager = koinInject()

    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = Color(0xFF1C2333),
        title = {
            Text(
                text = "Environment",
                color = Color.White,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
            )
        },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
                // Current mode badge
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(
                            if (isTestMode) Color(0x33EF4444) else Color(0x3322C55E),
                        )
                        .padding(horizontal = 12.dp, vertical = 8.dp),
                ) {
                    Text(
                        text = if (isTestMode) "TEST" else "NETWORK",
                        color = if (isTestMode) Color(0xFFEF4444) else Color(0xFF22C55E),
                        fontWeight = FontWeight.Bold,
                        fontSize = 16.sp,
                        letterSpacing = 1.sp,
                    )
                }

                Text(
                    text = "Switch to $targetLabel mode? All screens will reset.",
                    color = Color(0xFF92A4C9),
                    style = MaterialTheme.typography.bodyMedium,
                )
            }
        },
        confirmButton = {
            Button(
                onClick = {
                    EnvironmentStore.switchMode(
                        reloadModules = {
                            val koin = KoinPlatform.getKoin()
                            koin.loadModules(
                                listOf(authModule, repositoryModule),
                                allowOverride = true,
                            )
                        },
                        onSwitched = { newMode ->
                            when (newMode) {
                                AppEnvironment.TEST -> authManager.login("stub-jwt-token-for-test-mode", 1L)
                                AppEnvironment.NETWORK -> authManager.logout()
                            }
                        },
                    )
                    onDismiss()
                },
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF135BEC)),
            ) {
                Text("Switch to $targetLabel", color = Color.White)
            }
        },
        dismissButton = {
            Button(
                onClick = onDismiss,
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
            ) {
                Text("Cancel", color = Color(0xFF92A4C9))
            }
        },
    )
}
```

- [ ] **Step 3: Update imports**

The file needs these new imports (remove any unused old ones):

```kotlin
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import io.diasjakupov.mindtag.core.di.authModule
import io.diasjakupov.mindtag.core.di.repositoryModule
import io.diasjakupov.mindtag.core.network.AuthManager
import org.koin.compose.koinInject
import org.koin.mp.KoinPlatform
```

- [ ] **Step 4: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 5: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/EnvironmentBanner.kt
git commit -m "feat: make EnvironmentBanner always visible with tap-to-switch dialog"
```

---

### Task 3: Clean up — remove unused imports and ExperimentalFoundationApi

**Files:**
- Modify: `composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/EnvironmentBanner.kt`

- [ ] **Step 1: Remove unused imports from old implementation**

Remove these imports that are no longer needed (the old `AnimatedVisibility`, `fadeIn`/`fadeOut`, `ExperimentalFoundationApi`, `combinedClickable`):

```kotlin
// REMOVE these:
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.ui.text.style.TextAlign
```

Also remove the `@OptIn(ExperimentalFoundationApi::class)` annotation from `EnvironmentBanner`.

- [ ] **Step 2: Verify build**

Run: `./gradlew :composeApp:compileKotlinJvm`
Expected: BUILD SUCCESSFUL

- [ ] **Step 3: Run tests**

Run: `./gradlew :composeApp:jvmTest`
Expected: BUILD SUCCESSFUL, all tests pass

- [ ] **Step 4: Commit**

```bash
git add composeApp/src/commonMain/kotlin/io/diasjakupov/mindtag/core/config/EnvironmentBanner.kt
git commit -m "chore: remove unused animation/experimental imports from EnvironmentBanner"
```

---

### Task 4: Manual verification

- [ ] **Step 1: Run desktop app**

Run: `./gradlew :composeApp:run`

Verify:
1. Green "NETWORK — Tap to switch" banner is visible at the bottom of the screen
2. Tap the banner → dialog opens showing "NETWORK" badge and "Switch to TEST mode?" text
3. Tap "Switch to TEST" → dialog closes, banner turns red "TEST MODE — Tap to switch"
4. App shows the auth screen (because `authManager.logout()` was not called — in TEST mode `authManager.login()` is called, so the app should show main content with stub data)
5. Notes screen shows the 5 hardcoded stub notes (Machine Learning, Gradient Descent, etc.)
6. Tap banner again → dialog shows "TEST" badge, "Switch to NETWORK?"
7. Tap "Switch to NETWORK" → banner turns green, app shows auth login screen (because `authManager.logout()` was called)

- [ ] **Step 2: Commit final state if any tweaks were needed**
