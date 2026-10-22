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

Phase 1 routes:

- `Route.Dashboard` — post-auth start destination; tile grid + Jump Back In.
- `Route.PersonalLists` — user's personal lists with CRUD.
- `Route.SharedLists`, `Route.Recipes`, `Route.Profile` — placeholders for Phase 4 / 5 features.
- `Route.CheckList(listId: Int)` — single list view, parameterized by id.
- `Route.Login` and the four `Route.ResetPassword*` routes — unchanged from Phase 0.

Reset-flow composables (and the Phase-1 inner-graph composables) explicitly set `popEnterTransition = { EnterTransition.None }`. Without that override, nav-compose falls back to the screen's `enterTransition` when popping back to it, producing a visible crisscross as both screens slide in from the right simultaneously.

### State Management

ViewModels extend `androidx.lifecycle.ViewModel` and use `MutableStateFlow` + `viewModelScope`. Screens collect state via `collectAsStateWithLifecycle()`.

### Supabase Integration

- **Auth**: Email/password (with verification + reset deep link), Google (native on Android via Credential Manager + `credentials-play-services-auth`; iOS uses Supabase's OAuth web fallback), guest mode (in-memory, gates shared features). Apple is hidden in UI until a developer account is available. Flow type is platform-specific: `FlowType.PKCE` on Android, `FlowType.IMPLICIT` on iOS (supabase-kt 3.0.3 limitation — external Safari can kill the app and lose the PKCE verifier; tokens-in-fragment is robust to that). Deep link scheme: `io.supabase.broccolist://login-callback`.
- **Client key**: a **publishable key** (`sb_publishable_...`). RLS is enforced — every table (`lists`, `sections`, `items`) has policies scoped to `auth.uid() = user_id` via the parent traversal. Never embed a `service_role` key.
- **Database**: Postgrest for CRUD on `lists`, `sections`, and `items` tables. Entities are `@Serializable` and decoded automatically.
- **Realtime**: Installed but not yet fully wired up.

`CheckListViewModel` uses **optimistic updates** — local state is updated immediately before the DB call. Items without a DB id are compared via custom `checkEqualityWithNullId()` helpers to prevent duplicates. In guest mode, the same ViewModel short-circuits Supabase calls and assigns negative integer IDs locally.

### Auth State

`AppViewModel` exposes a combined `AppAuthState` (`Initializing | NotAuthenticated | Authenticated(session) | Guest | RefreshFailure`) that fuses Supabase's `SessionStatus` with `GuestModeRepository.isGuest`. `AuthenticatedContent` consumes this and emits a nullable `UserSession?` to its content lambda — `null` means guest. `AppViewModel.init` also auto-clears guest mode when a real session arrives, so downstream code never sees the "guest + authenticated" combination.

### iOS Entry Point

`iosApp/iosApp/` is a SwiftUI app using the `App` lifecycle (UIScene-based URL routing). **`AppDelegate.application(_:open:)` is never invoked** under this setup — inbound deep links flow through SwiftUI's `.onOpenURL` on `ContentView`, which forwards them to `MainViewControllerKt.handleSupabaseDeeplink(url:)`. That function calls `client.handleDeeplinks(url)` and guards `initKoin()` with a module-level `koinStarted` flag so a deep link arriving before the first `configure` block runs won't crash `DependencyProvider`. GoogleSignIn is still set up as a fallback URL handler in `.onOpenURL`.

### Reset Password Flow

Four screens in `login/presentation/`: `ResetPasswordRequest` → `ResetPasswordSent(email)` → `ResetPasswordCreate` → `ResetPasswordSuccess`. The "I've got the link — continue" button on the Sent screen lets the user manually advance after tapping the recovery link in their inbox (which silently establishes a recovery session via `handleSupabaseDeeplink`). The Create screen enforces live password rules via the pure-Kotlin `PasswordRules` data class (length ≥8, mixed case, at least one digit). Each reset-flow composable explicitly sets `popEnterTransition = { EnterTransition.None }` to keep the revealed previous screen still while the current one slides off.

### Theme

`BroccolistTheme` in `theme/Theme.kt` defines Light and Dark color schemes from `BroccolistColors`. Key tokens to know:

- `primary` — brand green. PrimaryGreen (#378A49) in light, PrimaryGreenLight (#81C784) in dark.
- `primaryContainer` — light-green surface used by disabled buttons and icon badges. SelectionGreen (#A5D6A7, Material Green 200) in light, PrimaryGreenDark (#1F4F2A) in dark. Disabled-state buttons read as "available but inactive" instead of disappearing into the background.
- `onPrimary` — content over `primary`. White in light, Black in dark.

Any CTA that gates on user input (Login, Send reset link, Update password) uses the pattern `containerColor = primary` / `disabledContainerColor = primaryContainer` so it reads "available but inactive" until the user fills in what's needed. Always-enabled buttons in the reset flow (the Sent screen's continue / Success screen's Back to login) just use `primary` outright.

### Key Packages

```
com.broccolist/
├── app/             # Root composable, AppViewModel, AppAuthState, Route definitions
├── dashboard/       # Phase 1 entry: Dashboard, placeholder Shared/Recipes/Profile screens
├── personallists/   # Personal Lists CRUD index screen + ViewModel
├── checklist/       # Single list view: sections/items, optimistic edits
├── login/           # Auth screens, LoginViewModel, reset-password flow screens
├── core/            # AuthApi, HttpClientFactory, GuestModeRepository, GuestListStore
├── theme/           # Material3 theme, colors, typography
└── di/              # Koin modules and platform bindings
```

### Multi-list data flow

`SupabaseRepository` is the single auth-path data source: `getListsForUser(userId)`, `getListById(listId)`, `createList(name, userId)`, `renameList(listId, name)`, `deleteList(listId)`, plus the section/item CRUD that already existed. Authenticated screens refresh on entry (`LaunchedEffect(userId)`); they don't keep a Realtime subscription open yet.

Guest mode uses a parallel `GuestListStore` (in-memory, `MutableStateFlow<List<CheckList>>`) registered as a Koin singleton. Both `DashboardViewModel` and `PersonalListsViewModel` `combine(isGuest, GuestListStore.lists, _authenticatedLists)` so the UI reacts to whichever side is the source of truth. `CheckListViewModel` writes through to `GuestListStore` for guest sessions and to `SupabaseRepository` for authenticated sessions. Phase 3 will collapse both paths once SQLDelight is the single source of truth on the client.

### Login Screen Layout

Email field, password field, "Forgot password?" (right-aligned in its own row), Login button, "Or login with" (centered sentence-case, no dividers), Google button, "Continue as Guest". All controls share the same width via `fillMaxWidth()` under the column's 24.dp horizontal padding; text fields use `singleLine = true` so long input scrolls instead of expanding the field. The Login button is `height(56.dp)` with `RoundedCornerShape(28.dp)` and uses `primary`/`onPrimary` when enabled, `primaryContainer`/`primary` when disabled (the light-green "available but inactive" treatment).

### GrocerySection

`GrocerySection` is a sealed class in `checklist/domain/StoreSections.kt` with 10 predefined sections (Produce, Deli, Meat, Bakery, Pantry, Snacks, Beverages, Frozen, Dairy, Other) plus a `Custom` variant.

### Item rename + section delete UX

Two different patterns by design:

- **Items rename inline via an always-rendered `BasicTextField`.** `ItemRow` does NOT swap between Text and TextField — the field is always there. Unfocused, it visually matches plain text (no border / cursor). Tap focuses it and the keyboard appears; Done or blur commits via `commitRename()`. Empty / whitespace-only inputs revert. The previous "swap on tap" approach was rejected because the parent `Row.draggable` was eating tap events before the inner clickable could see them — letting the TextField own tap-to-focus from the start avoids the gesture fight entirely. Swipe-to-delete is gated on `!isFocused` so cursor / selection drags work while editing. External name changes are synced into the draft buffer via `LaunchedEffect(item.name, isFocused)`, but only when not focused, so an in-progress edit isn't clobbered by a server-side update.
- **Sections still rename via swipe → edit-icon → dialog** (the existing `SwipeableBox` behavior). Inline rename was scoped to items only.
- **Section delete is always confirmation-gated.** All three entry points in `SwipeableBox` (tapping the red swipe button, full-swipe past `fullDeleteThreshold` mid-drag, releasing past `deleteThreshold`) flip a `showDeleteConfirmation` flag that opens an `AlertDialog`. Confirm calls `removeSection`; Cancel resets the row offset. Item delete still removes immediately on swipe — sections are heavier and contain other rows, so the extra friction is intentional there.

### Current phase

Phase 0 (auth polish) is complete. Phase 1 (Dashboard + multi-list + inline rename) hasn't started. See `project_docs/PRD.md` for the full roadmap and rationale.
