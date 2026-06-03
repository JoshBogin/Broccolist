# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Build Commands

```bash
# Android
./gradlew assembleDebug
./gradlew installDebug
./gradlew assembleRelease

# iOS framework (build from Xcode or via Gradle)
./gradlew linkDebugFrameworkIosSimulatorArm64
./gradlew linkDebugFrameworkIosArm64

# Run tests
./gradlew testDebugUnitTest

# Lint
./gradlew detekt

# Clean
./gradlew clean
```

> Note: There are currently no unit test files in the project; test tasks exist but are unused.

## Architecture

This is a **Kotlin Multiplatform + Compose Multiplatform** app targeting Android and iOS. All shared UI, business logic, and data access live in `composeApp/src/commonMain`. Platform-specific code uses `expect/actual`.

### Layer Structure (per feature)

Each feature (`checklist/`, `login/`, `core/`) follows clean architecture:

- **`data/`** — `@Serializable` entities, repository interfaces, Supabase/Postgrest implementations
- **`domain/`** — pure Kotlin domain models, state machines (sealed classes), enums
- **`presentation/`** — `ViewModel` (StateFlow-based state), Screen composables, UI components

### Dependency Injection

Koin 4.0 is used throughout. Modules are in `di/`:
- `SupabaseModule.kt` — single `SupabaseClient` (with platform-specific auth config via `expect/actual`)
- `DataModule.kt` — repositories
- `Modules.kt` / `Modules.android.kt` / `Modules.ios.kt` — platform-specific bindings (e.g., OkHttp vs Darwin HTTP engine)

Entry point: `initKoin()` called from `BroccolistApplication` (Android) and `MainViewController` (iOS).

### Navigation

Type-safe navigation via JetBrains Navigation Compose (alpha). Routes are defined as a `@Serializable` sealed interface in `app/data/Route.kt`. The nav graph is set up in `App.kt` with slide transition animations.

### State Management

ViewModels extend `androidx.lifecycle.ViewModel` and use `MutableStateFlow` + `viewModelScope`. Screens collect state via `collectAsStateWithLifecycle()`.

### Supabase Integration

- **Auth**: Email/password, Google (native), Apple (native). Uses PKCE flow + `ComposeAuth`. Deep link scheme: `io.supabase.broccolist://login-callback`.
- **Database**: Postgrest for CRUD on `lists`, `sections`, and `items` tables. Entities are `@Serializable` and decoded automatically.
- **Realtime**: Installed but not yet fully wired up.

`CheckListViewModel` uses **optimistic updates** — local state is updated immediately before the DB call. Items without a DB id are compared via custom `checkEqualityWithNullId()` helpers to prevent duplicates.

### iOS Entry Point

`iosApp/iosApp/` is a SwiftUI app. `ContentView.swift` embeds the shared Compose UI via `ComposeView` and forwards deep links to `MainViewController` (Kotlin).

### Key Packages

```
com.broccolist/
├── app/           # Root composable, AppViewModel, Route definitions
├── checklist/     # Main feature: checklist/section/item management
├── login/         # Auth screens and LoginViewModel
├── core/          # AuthApi, HttpClientFactory
├── theme/         # Material3 theme, colors, typography
└── di/            # Koin modules and platform bindings
```

### GrocerySection

`GrocerySection` is a sealed class in `checklist/domain/StoreSections.kt` with 10 predefined sections (Produce, Deli, Meat, Bakery, Pantry, Snacks, Beverages, Frozen, Dairy, Other) plus a `Custom` variant.