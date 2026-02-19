# Build Configuration & Platform Setup

## Version Catalog (`gradle/libs.versions.toml`)

### Versions

| Key | Version | Purpose |
|-----|---------|---------|
| `kotlin` | 2.3.0 | Kotlin language |
| `composeMultiplatform` | 1.10.0 | Compose Multiplatform UI framework |
| `agp` | 8.11.2 | Android Gradle Plugin |
| `sqldelight` | 2.0.2 | Cross-platform SQLite database |
| `koin` | 4.0.2 | Dependency injection |
| `ktor` | 3.1.1 | HTTP client |
| `kotlinx-coroutines` | 1.10.2 | Coroutines |
| `kotlinx-serialization` | 1.8.0 | JSON serialization |
| `kotlinx-datetime` | 0.6.2 | Date/time handling |
| `material3` | 1.10.0-alpha05 | Material 3 design system |
| `multiplatform-nav3-ui` | 1.0.0-alpha05 | Navigation 3 UI |
| `androidx-lifecycle-nav3` | 2.10.0-alpha08 | Lifecycle ViewModel Navigation 3 |
| `androidx-lifecycle` | 2.9.6 | ViewModel + Runtime Compose |
| `composeHotReload` | 1.0.0 | Compose Hot Reload |
| `androidx-activity` | 1.12.2 | Activity Compose |
| `androidx-appcompat` | 1.7.1 | AppCompat |
| `androidx-core` | 1.17.0 | AndroidX Core KTX |
| `junit` | 4.13.2 | JUnit 4 |
| `androidx-espresso` | 3.7.0 | Espresso testing |
| `androidx-testExt` | 1.3.0 | AndroidX Test Extensions |
| `turbine` | 1.2.0 | Flow testing |

### Android SDK

| Setting | Value |
|---------|-------|
| `android-compileSdk` | 36 |
| `android-targetSdk` | 36 |
| `android-minSdk` | 30 |

### Libraries

**Compose:**
- `compose-runtime` = `org.jetbrains.compose.runtime:runtime` (composeMultiplatform)
- `compose-foundation` = `org.jetbrains.compose.foundation:foundation` (composeMultiplatform)
- `compose-material3` = `org.jetbrains.compose.material3:material3` (material3 version)
- `compose-ui` = `org.jetbrains.compose.ui:ui` (composeMultiplatform)
- `compose-components-resources` = `org.jetbrains.compose.components:components-resources` (composeMultiplatform)
- `compose-uiToolingPreview` = `org.jetbrains.compose.ui:ui-tooling-preview` (composeMultiplatform)
- `compose-uiTooling` = `org.jetbrains.compose.ui:ui-tooling` (composeMultiplatform)

**AndroidX:**
- `androidx-activity-compose` = `androidx.activity:activity-compose` (1.12.2)
- `androidx-lifecycle-viewmodelCompose` = `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-compose` (2.9.6)
- `androidx-lifecycle-runtimeCompose` = `org.jetbrains.androidx.lifecycle:lifecycle-runtime-compose` (2.9.6)
- `androidx-core-ktx` = `androidx.core:core-ktx` (1.17.0)
- `androidx-appcompat` = `androidx.appcompat:appcompat` (1.7.1)

**Navigation:**
- `jetbrains-navigation3-ui` = `org.jetbrains.androidx.navigation3:navigation3-ui` (1.0.0-alpha05)
- `jetbrains-lifecycle-viewmodelNavigation3` = `org.jetbrains.androidx.lifecycle:lifecycle-viewmodel-navigation3` (2.10.0-alpha08)

**SQLDelight:**
- `sqldelight-runtime` = `app.cash.sqldelight:runtime` (2.0.2)
- `sqldelight-coroutines` = `app.cash.sqldelight:coroutines-extensions` (2.0.2)
- `sqldelight-android` = `app.cash.sqldelight:android-driver` (2.0.2)
- `sqldelight-native` = `app.cash.sqldelight:native-driver` (2.0.2)
- `sqldelight-jvm` = `app.cash.sqldelight:sqlite-driver` (2.0.2)

**Koin:**
- `koin-core` = `io.insert-koin:koin-core` (4.0.2)
- `koin-compose` = `io.insert-koin:koin-compose` (4.0.2)
- `koin-compose-viewmodel` = `io.insert-koin:koin-compose-viewmodel` (4.0.2)
- `koin-test` = `io.insert-koin:koin-test` (4.0.2)

**Ktor:**
- `ktor-client-core` = `io.ktor:ktor-client-core` (3.1.1)
- `ktor-client-content-negotiation` = `io.ktor:ktor-client-content-negotiation` (3.1.1)
- `ktor-serialization-kotlinx-json` = `io.ktor:ktor-serialization-kotlinx-json` (3.1.1)
- `ktor-client-okhttp` = `io.ktor:ktor-client-okhttp` (3.1.1)
- `ktor-client-darwin` = `io.ktor:ktor-client-darwin` (3.1.1)
- `ktor-client-logging` = `io.ktor:ktor-client-logging` (3.1.1)

**Serialization / DateTime:**
- `kotlinx-serialization-json` = `org.jetbrains.kotlinx:kotlinx-serialization-json` (1.8.0)
- `kotlinx-datetime` = `org.jetbrains.kotlinx:kotlinx-datetime` (0.6.2)
- `kotlinx-coroutinesSwing` = `org.jetbrains.kotlinx:kotlinx-coroutines-swing` (1.10.2)

**Testing:**
- `kotlin-test` = `org.jetbrains.kotlin:kotlin-test` (2.3.0)
- `kotlin-testJunit` = `org.jetbrains.kotlin:kotlin-test-junit` (2.3.0)
- `kotlinx-coroutines-test` = `org.jetbrains.kotlinx:kotlinx-coroutines-test` (1.10.2)
- `turbine` = `app.cash.turbine:turbine` (1.2.0)

### Plugins

| Alias | Plugin ID | Version |
|-------|-----------|---------|
| `kotlinMultiplatform` | `org.jetbrains.kotlin.multiplatform` | 2.3.0 |
| `androidApplication` | `com.android.application` | 8.11.2 |
| `androidLibrary` | `com.android.library` | 8.11.2 |
| `composeMultiplatform` | `org.jetbrains.compose` | 1.10.0 |
| `composeCompiler` | `org.jetbrains.kotlin.plugin.compose` | 2.3.0 |
| `composeHotReload` | `org.jetbrains.compose.hot-reload` | 1.0.0 |
| `sqldelight` | `app.cash.sqldelight` | 2.0.2 |
| `kotlinx-serialization` | `org.jetbrains.kotlin.plugin.serialization` | 2.3.0 |

---

## Module Configuration (`composeApp/build.gradle.kts`)

### Plugins Applied

```kotlin
plugins {
    alias(libs.plugins.kotlinMultiplatform)
    alias(libs.plugins.androidApplication)
    alias(libs.plugins.composeMultiplatform)
    alias(libs.plugins.composeCompiler)
    alias(libs.plugins.composeHotReload)
    alias(libs.plugins.sqldelight)
    alias(libs.plugins.kotlinx.serialization)
}
```

### Platform Targets

| Target | Configuration |
|--------|---------------|
| `androidTarget` | JVM target: `JvmTarget.JVM_11` |
| `iosArm64()` | Static framework, `baseName = "ComposeApp"` |
| `iosSimulatorArm64()` | Static framework, `baseName = "ComposeApp"` |
| `jvm()` | Default JVM target |

### Source Set Dependencies

**`commonMain`:**
```kotlin
implementation(libs.compose.runtime)
implementation(libs.compose.foundation)
implementation(libs.compose.material3)
implementation(libs.compose.ui)
implementation(libs.compose.components.resources)
implementation(libs.compose.uiToolingPreview)
implementation(libs.androidx.lifecycle.viewmodelCompose)
implementation(libs.androidx.lifecycle.runtimeCompose)
implementation(libs.sqldelight.runtime)
implementation(libs.sqldelight.coroutines)
implementation(libs.koin.core)
implementation(libs.koin.compose)
implementation(libs.koin.compose.viewmodel)
implementation(libs.kotlinx.serialization.json)
implementation(libs.kotlinx.datetime)
implementation(libs.jetbrains.navigation3.ui)
implementation(libs.jetbrains.lifecycle.viewmodelNavigation3)
implementation(compose.materialIconsExtended)
implementation(libs.ktor.client.core)
implementation(libs.ktor.client.content.negotiation)
implementation(libs.ktor.serialization.kotlinx.json)
implementation(libs.ktor.client.logging)
```

**`androidMain`:**
```kotlin
implementation(libs.compose.uiToolingPreview)
implementation(libs.androidx.activity.compose)
implementation(libs.sqldelight.android)
implementation(libs.ktor.client.okhttp)
```

**`iosMain`:**
```kotlin
implementation(libs.sqldelight.native)
implementation(libs.ktor.client.darwin)
```

**`jvmMain`:**
```kotlin
implementation(compose.desktop.currentOs)
implementation(libs.kotlinx.coroutinesSwing)
implementation(libs.sqldelight.jvm)
implementation(libs.ktor.client.okhttp)
```

**`commonTest`:**
```kotlin
implementation(libs.kotlin.test)
implementation(libs.kotlinx.coroutines.test)
implementation(libs.turbine)
implementation(libs.koin.test)
```

**`jvmTest`:**
```kotlin
implementation(libs.sqldelight.jvm)
implementation(libs.kotlinx.datetime)
implementation(libs.kotlinx.serialization.json)
```

**Debug dependencies (Android):**
```kotlin
debugImplementation(libs.compose.uiTooling)
```

### Android Configuration

```kotlin
android {
    namespace = "io.diasjakupov.mindtag"
    compileSdk = 36

    defaultConfig {
        applicationId = "io.diasjakupov.mindtag"
        minSdk = 30
        targetSdk = 36
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources { excludes += "/META-INF/{AL2.0,LGPL2.1}" }
    }

    buildTypes {
        getByName("release") { isMinifyEnabled = false }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }

    testOptions {
        unitTests.isReturnDefaultValues = true
    }
}
```

### Android Manifest

```xml
<uses-permission android:name="android.permission.INTERNET" />

<application
    android:allowBackup="true"
    android:icon="@mipmap/ic_launcher"
    android:label="@string/app_name"
    android:networkSecurityConfig="@xml/network_security_config"
    android:roundIcon="@mipmap/ic_launcher_round"
    android:supportsRtl="true"
    android:theme="@android:style/Theme.Material.Light.NoActionBar">
    <activity android:exported="true" android:name=".MainActivity">
        <intent-filter>
            <action android:name="android.intent.action.MAIN" />
            <category android:name="android.intent.category.LAUNCHER" />
        </intent-filter>
    </activity>
</application>
```

### SQLDelight Configuration

```kotlin
sqldelight {
    databases {
        create("MindTagDatabase") {
            packageName.set("io.diasjakupov.mindtag.data.local")
        }
    }
}
```

Schema files in: `composeApp/src/commonMain/sqldelight/io/diasjakupov/mindtag/data/local/`

### Resolution Strategy

```kotlin
configurations.all {
    resolutionStrategy {
        force("org.jetbrains.kotlinx:kotlinx-datetime:0.6.2")
        force("org.jetbrains.kotlinx:kotlinx-datetime-jvm:0.6.2")
    }
}
```

Forces `kotlinx-datetime` to 0.6.2 to resolve transitive version conflicts.

### Desktop Distribution

```kotlin
compose.desktop {
    application {
        mainClass = "io.diasjakupov.mindtag.MainKt"
        nativeDistributions {
            targetFormats(TargetFormat.Dmg, TargetFormat.Msi, TargetFormat.Deb)
            packageName = "io.diasjakupov.mindtag"
            packageVersion = "1.0.0"
        }
    }
}
```

| Format | Platform |
|--------|----------|
| DMG | macOS |
| MSI | Windows |
| DEB | Linux |

---

## Root Build File (`build.gradle.kts`)

```kotlin
plugins {
    alias(libs.plugins.androidApplication) apply false
    alias(libs.plugins.androidLibrary) apply false
    alias(libs.plugins.composeHotReload) apply false
    alias(libs.plugins.composeMultiplatform) apply false
    alias(libs.plugins.composeCompiler) apply false
    alias(libs.plugins.kotlinMultiplatform) apply false
}
```

All plugins declared but not applied at root level (applied in `:composeApp` subproject).

---

## Gradle Settings (`settings.gradle.kts`)

```kotlin
rootProject.name = "Mindtag"
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")
```

**Plugin Management repositories:** Google Maven (filtered to `androidx`, `com.android`, `com.google` groups), Maven Central, Gradle Plugin Portal

**Dependency Resolution repositories:** Google Maven (same filter), Maven Central

**JVM Toolchain:** `foojay-resolver-convention` 1.0.0

**Modules:** Single module `:composeApp`

---

## Gradle Properties (`gradle.properties`)

```properties
kotlin.code.style=official
kotlin.daemon.jvmargs=-Xmx3072M

org.gradle.jvmargs=-Xmx4096M -Dfile.encoding=UTF-8
org.gradle.configuration-cache=true
org.gradle.caching=true

android.nonTransitiveRClass=true
android.useAndroidX=true
```

---

## Platform Entry Points

### Android (`androidMain`)

**File:** `composeApp/src/androidMain/kotlin/io/diasjakupov/mindtag/MainActivity.kt`

```kotlin
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        enableEdgeToEdge()
        super.onCreate(savedInstanceState)

        initKoin(module {
            single { DatabaseDriverFactory(this@MainActivity) }
            single { TokenStorage(this@MainActivity) }
        })

        setContent {
            App()
        }
    }
}
```

**Database driver:** `AndroidSqliteDriver(MindTagDatabase.Schema, context, "mindtag.db")`

**Token storage:** `SharedPreferences("mindtag_auth")` via `Context`

**Platform-specific files:**
- `DatabaseDriverFactory.android.kt` -- `actual class DatabaseDriverFactory(private val context: Context)`
- `TokenStorage.android.kt` -- `actual class TokenStorage(context: Context)` using `SharedPreferences`
- `Logger.android.kt` -- `actual object Logger` using `android.util.Log`
- `Platform.android.kt` -- platform identifier
- Preview files for each screen in `feature/*/presentation/`

### iOS (`iosMain`)

**File:** `composeApp/src/iosMain/kotlin/io/diasjakupov/mindtag/MainViewController.kt`

```kotlin
fun MainViewController(): platform.UIKit.UIViewController {
    initKoin(module {
        single { DatabaseDriverFactory() }
        single { TokenStorage() }
    })
    return ComposeUIViewController { App() }
}
```

**Database driver:** `NativeSqliteDriver(MindTagDatabase.Schema, "mindtag.db")`

**Token storage:** `NSUserDefaults.standardUserDefaults` with keys `mindtag_access_token`, `mindtag_user_id`

**Platform-specific files:**
- `DatabaseDriverFactory.ios.kt` -- `actual class DatabaseDriverFactory` (no constructor params)
- `TokenStorage.ios.kt` -- `actual class TokenStorage` using `NSUserDefaults`
- `Logger.ios.kt` -- `actual object Logger` using `println`
- `Platform.ios.kt` -- platform identifier

Built via Xcode from `iosApp/` directory.

### Desktop / JVM (`jvmMain`)

**File:** `composeApp/src/jvmMain/kotlin/io/diasjakupov/mindtag/main.kt`

```kotlin
fun main() {
    initKoin(module {
        single { DatabaseDriverFactory() }
        single { TokenStorage() }
    })
    application {
        Window(
            onCloseRequest = ::exitApplication,
            title = "Mindtag",
            icon = painterResource("icon.png"),
        ) {
            App()
        }
    }
}
```

**Database driver:** `JdbcSqliteDriver("jdbc:sqlite:mindtag.db")` with explicit schema creation on first run (`if (!dbFile.exists()) MindTagDatabase.Schema.create(driver)`)

**Token storage:** `java.util.prefs.Preferences.userNodeForPackage(TokenStorage::class.java)`

**Platform-specific files:**
- `DatabaseDriverFactory.jvm.kt` -- `actual class DatabaseDriverFactory` (checks for existing `mindtag.db` file)
- `TokenStorage.jvm.kt` -- `actual class TokenStorage` using Java Preferences API
- `Logger.jvm.kt` -- `actual object Logger` using `println`
- `Platform.jvm.kt` -- platform identifier

Window icon: `icon.png` loaded via `painterResource` from JVM resources.

---

## expect/actual Summary

| Declaration | commonMain | androidMain | iosMain | jvmMain |
|-------------|------------|-------------|---------|---------|
| `DatabaseDriverFactory` | `expect class` | `AndroidSqliteDriver` | `NativeSqliteDriver` | `JdbcSqliteDriver` |
| `TokenStorage` | `expect class` | `SharedPreferences` | `NSUserDefaults` | `java.util.prefs.Preferences` |
| `Logger` | `expect object` | `android.util.Log` | `println` | `println` |

---

## Compose Resources

**Location:** `composeApp/src/commonMain/composeResources/`

**Fonts** (`font/`):
- `lexend_light.ttf`
- `lexend_regular.ttf`
- `lexend_medium.ttf`
- `lexend_semibold.ttf`
- `lexend_bold.ttf`

**Drawables** (`drawable/`):
- `compose-multiplatform.xml` (default vector drawable)

---

## Build Commands

```shell
# Android debug APK
./gradlew :composeApp:assembleDebug

# Desktop (JVM) run
./gradlew :composeApp:run

# All tests (common + all platforms)
./gradlew :composeApp:allTests

# Common tests only (runs on JVM)
./gradlew :composeApp:jvmTest

# Full build check
./gradlew build
```

iOS is built via Xcode from `iosApp/`.
