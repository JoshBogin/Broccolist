# Broccolist — Product Requirements Document

**Status:** Draft v1 (Phase 0 implementation in progress)
**Last updated:** 2026-06-02
**Owner:** Josh Bogin

## Phase 0 progress (this session)

Code-level changes landed:

- **Apple sign-in hidden.** The button was removed from `LoginScreen.kt` and the helper `appleSignIn(...)` composable was deleted. `AuthApi.signInWithApple` and the Supabase `appleNativeLogin()` config remain in place so the provider can be re-enabled without a data-layer change.
- **Google sign-in on iOS wired.** `iOSApp.swift`'s `AppDelegate.application(_:open:)` now hands the callback URL to `GIDSignIn.sharedInstance.handle(url)`. The Info.plist already had `GIDClientID`, `GIDServerClientID`, and the reversed-client-ID URL scheme; the Podfile already pulls in `GoogleSignIn`. The broken `handleDeepLink` private method (which called a nonexistent `AppleKt.handleDeeplinks`) was removed; Supabase deeplinks flow through SwiftUI's `onOpenURL` → `MainViewController` as before.
- **Email/password polish.** `PasswordRecoveryDialog` copy and keyboard type fixed (was asking for an email with a numeric keypad and "enter your new password" text). `LoginViewModel.signUp` now surfaces a "check your email" alert. `LoginViewModel.login` detects Supabase's `Email not confirmed` error and shows a clear "verify your email" message. `LoginViewModel.resetPassword` reports success via the alert channel. A new `AuthApi.resendVerificationEmail(email)` method calls Supabase's `auth.resendEmail(OtpType.Email.SIGNUP, ...)`; `LoginViewModel.resendVerificationEmail` wraps it.
- **Guest mode.** New `GuestModeRepository` (interface + `InMemoryGuestModeRepository`) lives in `core/data/` and is registered as a singleton in `DataModule.kt`. A new `AppAuthState` sealed interface (`Initializing | NotAuthenticated | Authenticated(session) | Guest | RefreshFailure`) is produced by `AppViewModel` via `combine(authApi.sessionStatus(), guestModeRepository.isGuest)`. `AuthenticatedContent` now takes an `AppAuthState` and emits a nullable `UserSession?` to its content lambda — `null` means guest. `CheckListScreenRoute` accepts a nullable `UserSession?`. `CheckListViewModel` takes the new repo and short-circuits every Supabase call when `isGuest` is true; section/item adds get hand-rolled negative IDs from a local counter. Sign-out in guest mode just drops the in-memory state. A "Continue as Guest" button lives on `LoginScreen` below the Google button.

**Still needs your hands (cannot be done from this session):**

- **Verify Google sign-in works on iOS** by running on a real device or Simulator. If the OAuth round-trip still fails, common culprits are: the URL scheme in Info.plist not matching the reversed client ID exactly, or the Supabase project's "Authorized Client IDs" for Google not including the iOS bundle's client ID alongside the web client ID currently passed to `googleNativeLogin(...)` in `SupabaseModule.kt`.
- **Verify the email-verification deep link path** in the Supabase dashboard. The verification email's redirect URL must match `io.supabase.broccolist://login-callback`. Once verified, signing in should succeed and the new "Email not confirmed" alert should stop appearing.
- **Verify the password-reset deep link** the same way. After tapping the reset link, the app needs to surface a "set a new password" screen. That screen isn't built yet — `LoginViewModel.changePassword(password)` exists in the data layer, but no UI route invokes it. Add a `Route.ResetPassword` and wire it up when the deep link arrives in a `recovery` state. (Tracking this as a follow-up in Phase 0.)
- **Test that the existing in-memory guest data is correctly cleared on sign-out** and that adding sections/items as a guest behaves correctly (no crashes on the `_checkLists` null seed path).

**Deferred to Phase 3 (already noted in §4.9):**

- Persisting guest data across app restarts (currently in-memory only).
- Migrating guest data to a real account on sign-up / sign-in.

### Phase 0 follow-up: code-review fixes (this session)

Copilot reviewed the Phase 0 PR and flagged five issues. Status:

- **Security: service_role key rotated.** The Supabase key embedded in `SupabaseModule.kt` was decoded as a `service_role` JWT (admin, bypasses RLS). Rotated in the Supabase dashboard and replaced with the new **publishable key** (`sb_publishable_...` format — the successor to legacy `anon`). Anything that was relying on service-role bypass is now subject to RLS.
- **Auto-clear guest mode on sign-in.** `AppViewModel.init` now observes `authApi.sessionStatus()` and calls `guestModeRepository.exitGuestMode()` when an `Authenticated` session arrives. Without this, `CheckListViewModel.isGuest` stayed `true` after a guest signed in and silently dropped all Supabase writes.
- **`FlowType` is now platform-specific.** Removed from the shared `install(Auth)` block. `SupabaseModule.android.kt` pins `FlowType.PKCE` (Custom Tabs keeps the app foregrounded; the in-memory verifier survives). `SupabaseModule.ios.kt` pins `FlowType.IMPLICIT` (external Safari may kill the app and lose the verifier; tokens-in-fragment side-steps the issue). Re-evaluate when supabase-kt 3.1+ brings `ASWebAuthenticationSession` to iOS.
- **`handleSupabaseDeeplink` is now Koin-safe.** Guards `initKoin()` with the same `koinStarted` flag `MainViewController` uses, so a deep link arriving before the first `configure` block runs won't crash `DependencyProvider`. KDoc rewritten to reflect that SwiftUI's `.onOpenURL` (not `AppDelegate.application(_:open:)`) is the actual entry point under the SwiftUI `App` lifecycle.
- **Skipped: hardcoded `DEVELOPMENT_TEAM` in `project.pbxproj`.** Valid concern for multi-contributor / CI setups, irrelevant for a solo personal project today. Revisit if collaborators or CI are added.

### Open work from this round

- **RLS policies — DONE.** Per-user policies applied to `lists`, `sections`, and `items` via the parent-`user_id` traversal. Verified end-to-end with sign-in / CRUD on both platforms.
- **Reset-password flow — DONE.** Four-screen flow built per the designs: `ResetPasswordRequest` → `ResetPasswordSent(email)` → `ResetPasswordCreate` → `ResetPasswordSuccess`. Replaces the old `PasswordRecoveryDialog`. The user manually advances from the "Check your email" screen via "I've got the link — continue" after tapping the recovery link in their inbox (which silently creates a recovery session via `handleSupabaseDeeplink`). `ResetPasswordCreate` enforces three live rules: ≥8 characters, mixed case, ≥1 digit — and a matching-confirm-field check before submission is enabled. Rules live in a `PasswordRules` data class that's pure Kotlin (testable without Compose).
- **Upgrade supabase-kt to 3.1+** (still pending). Unblocks `ExternalAuthAction.WebAuthenticationSession` on iOS, lets us return to PKCE on iOS, and removes the "Open in Broccolist?" prompt.

### Phase 0 status: complete

Auth surface is functionally green on both platforms: email/password with verification + reset, Google sign-in (Android Credential Manager + iOS OAuth web fallback), guest mode with auto-clear on sign-in, Apple hidden until dev account is available, publishable key with RLS-enforced data access. Ready to move into Phase 1 (Dashboard + multi-list + inline rename).


## 1. Vision

Broccolist is a Kotlin Multiplatform grocery list app for Android and iOS. The current build is a single per-user list organized by store sections. The next phase turns it into a **social, recipe-aware, multi-list grocery planner** with real-time shared lists between friends, recipes that auto-sort their ingredients into store sections, and an offline-first sync model so the app works in the store regardless of signal.

## 2. Current State

### 2.1 Tech Stack

- **Platform:** Kotlin Multiplatform (KMP) targeting Android (`minSdk 24`, `targetSdk 35`) and iOS (Arm64 + Simulator Arm64). Shared code lives in `composeApp/src/commonMain`.
- **UI:** Compose Multiplatform 1.7.0 with Material 3.
- **Navigation:** JetBrains Navigation Compose 2.8.0-alpha10, type-safe routes via `@Serializable` sealed interface in `app/data/Route.kt`.
- **DI:** Koin 4.0 (`koin-compose-viewmodel`). Entry points: `BroccolistApplication.kt` (Android), `MainViewController.kt` (iOS).
- **State:** `androidx.lifecycle.ViewModel` + `MutableStateFlow`, collected with `collectAsState()`.
- **Backend:** Supabase 3.0.3 — Postgrest for CRUD, `auth-kt` + `ComposeAuth` for auth, `realtime-kt` installed but **not yet wired up**.
- **Auth:** Email/password, Google native (Android only — broken on iOS), Apple native (not functional on either platform — requires an Apple developer account). PKCE flow. Deep link scheme `io.supabase.broccolist://login-callback`.
- **HTTP:** Ktor 3.0.3 — OkHttp engine on Android, Darwin engine on iOS.
- **Serialization:** kotlinx.serialization 1.7.3.
- **Build:** Gradle 8.6.1, AGP 8.5.2, Kotlin 2.1.0, detekt 1.23.0. No convention plugins yet; single `composeApp` module.
- **Testing:** Test source sets exist, no actual tests written.

### 2.2 Current Features

- Email/password sign-in (works, but email verification and password reset paths are not polished).
- Google native sign-in **on Android only** — iOS path is not functional.
- Apple native sign-in scaffolded but **non-functional on both platforms** (blocked on Apple developer account).
- One auto-created grocery list per user (named "New List").
- Add a store section to the list from a fixed catalog of 10 (`Produce`, `Deli`, `Meat`, `Bakery`, `Pantry`, `Snacks`, `Beverages`, `Frozen`, `Dairy`, `Other`) plus a `Custom(name)` variant.
- Add items inside a section, check/uncheck them, delete them (swipe).
- **Rename sections** (already supported via `updateSection`).
- Sign out.

### 2.3 Known Limitations (the things this PRD fixes)

- **Items cannot be renamed inline** — `updateItem` exists in the repository but only handles `isChecked`/`name` round-trips; the UI has no edit affordance. Users currently delete and re-add to rename.
- **Items cannot be reordered or moved between sections** — no `position`/`order` column anywhere.
- **Only one list per user** — `getCheckList` returns `lists.firstOrNull()` and auto-creates if none exist. No multi-list support.
- **No sharing, no friends, no recipes.**
- **No offline support** — every read/write goes straight to Postgrest. `_checkLists` is in-memory only; restart = full reload.
- **Realtime is dead code** — the dependency is on the classpath but no subscriptions exist.
- **Nav drawer has only a "Home" tab** — there is no dashboard.
- **Auth gaps:** Google sign-in fails on iOS, Apple sign-in is broken on both platforms, email verification and password-reset flows are unfinished, and there is no guest / local-only mode.

### 2.4 Current Data Model

```
lists (CheckListEntity)
  id, name, created_at, edited_at, user_id

sections (SectionEntity)
  id, name, list_id

items (ItemEntity)
  id, name, is_checked, section_id
```

Domain models mirror the entities. `GrocerySection` is a sealed class with 10 predefined objects and a `Custom(displayName)` data class; persisted as plain `name: String` and rehydrated via `GrocerySection.fromString(name)`.

### 2.5 Current Package Layout

```
com.broccolist/
├── app/           — Root composable, AppViewModel, Route
├── checklist/     — data / domain / presentation for the single list feature
├── login/         — Auth screens + LoginViewModel
├── core/          — AuthApi, HttpClientFactory
├── theme/         — Material3 theme
└── di/            — Koin modules + expect/actual platform bindings
```

## 3. Goals

1. **Polish authentication** on both platforms — Google sign-in working on iOS, Apple sign-in hidden until a developer account is in place, email/password with proper verification and reset, and a guest mode for local-only use.
2. Restructure the entry experience around a **Dashboard** that lets users pick between Personal Lists, Shared Lists, and My Recipes.
3. Make list editing **dynamic and inline** — tap a row to rename items and sections without delete/re-add.
4. Support **drag-and-drop** for reordering items within a section and moving items between sections.
5. Introduce a **friends system** with in-app invites and notifications.
6. Support **shared lists** that two (or more) friends can view and edit at the same time in real time.
7. Add **recipes** with structured ingredients and steps, where ingredients are tagged with store sections so the recipe can be merged into any list with one tap.
8. Add **offline-first** local persistence with background sync to Supabase.

## 4. Functional Requirements

### 4.1 Authentication polish

The auth surface needs to be reliable on both platforms before anything else ships. The required end state:

**Providers visible to the user:**

- **Email / password** — primary path. Must support email verification on signup and password reset via email.
- **Google** — working on both Android and iOS.
- **Guest** — new path; no Supabase account, purely local.
- **Apple** — **hidden from the UI** until an Apple developer account is provisioned. Code path can stay in the repo but the button is removed from the login screen on both platforms.

**Google on iOS:**

The Android path works because `GoogleIdTokenCredential` + Credential Manager handles the native dance. iOS needs the equivalent via Google's Sign-In iOS SDK (or Supabase's `ComposeAuth` Google flow on iOS, if that path is working in 3.0.3). Fix involves:

- Adding the `GIDClientID` to the iOS `Info.plist` and registering the reversed-client-ID URL scheme.
- Wiring an `actual` implementation of the iOS Google sign-in handler that obtains an ID token and hands it to `supabase.auth.signInWith(IDToken)` with `provider = Google`.
- Verifying the deep-link callback path (`io.supabase.broccolist://login-callback`) is still honored from `ContentView.swift` → `MainViewController`.

**Email verification:**

- Supabase issues a verification email on signup (already configured server-side). The app needs a **post-signup "check your email" screen** that explains what to do and lets the user resend the email.
- The deep-link confirmation URL must route into the app and show a "verified — please sign in" state.
- Block sign-in for unverified accounts with a clear error and a "resend verification" action.

**Password reset:**

- Login screen exposes "Forgot password?" → user enters email → Supabase sends a reset email.
- Reset link deep-links back into the app to a new-password screen.
- On success, user is signed in and routed to the Dashboard.

**Guest mode:**

A user can tap "Continue as Guest" on the login screen and enter the app without a Supabase session.

- **What works for guests:** create, rename, edit, and delete **personal lists**; create, edit, and delete **recipes**. All data is stored locally only (in the same SQLDelight DB introduced in §4.9).
- **What is disabled for guests:** the Shared Lists tab and the Friends/Profile screen are visible but show an upsell state ("Create an account to share lists with friends"). Sync to Supabase is a no-op.
- **Upgrade path:** a guest can sign up or sign in at any time. On successful auth, the local data must be **migrated to the user's account** — local lists and recipes are pushed up to Supabase and bound to the new `user_id`. This needs an explicit migration step in the sync coordinator (see §4.9): walk the local tables, insert remote rows, and update local rows with the returned remote IDs.
- **Identifying guest data locally:** a sentinel `user_id` value (e.g. the string `"guest"` or a stable device-bound UUID stored in shared prefs / `NSUserDefaults`) on local rows. The sync coordinator skips uploads while in guest mode.

**Login screen layout (post-change):**

```
[ Broccolist header ]

Email          [_______________]
Password       [_______________]
               [ Sign in ]
               Forgot password?

— or —

[ Continue with Google ]
[ Continue as Guest    ]

Don't have an account? Sign up
```

Apple is intentionally absent. Email-verification and reset dialogs reuse the existing `OTPDialog` / `PasswordRecoveryDialog` components where possible.

### 4.2 Dashboard (new entry point)

When an authenticated user opens the app, they land on a **Dashboard** instead of a single list. The dashboard has three top-level destinations:

- **Personal Lists** — lists owned only by the current user.
- **Shared Lists** — lists the user has access to via a friend link.
- **My Recipes** — the user's saved recipes.

Each destination shows a list of items (lists / recipes) with create, rename, and delete affordances. Tapping a list opens the existing checklist UI. The dashboard replaces `Route.CheckList` as the start destination; `CheckList` becomes a child route that takes a `listId`.

**Nav graph changes:**

```
Route.Dashboard          — new start destination after auth
Route.PersonalLists      — tab content
Route.SharedLists        — tab content
Route.Recipes            — tab content
Route.CheckList(listId)  — existing screen, now parameterized
Route.Recipe(recipeId)   — new screen
Route.Friends            — new profile / friends screen
Route.Login              — unchanged
```

### 4.3 Multi-list support

Drop the "one list per user" assumption. `lists` rows are no longer auto-created; users explicitly create lists from the Personal Lists tab. List names are user-supplied. The repository gains `getListsForUser(userId)`, `createList(name, userId)`, `renameList(id, name)`, and `deleteList(id)`.

### 4.4 Inline rename

Tapping an item row or a section header turns it into an editable text field. Pressing return or losing focus commits the rename through the existing `updateItem` / `updateSection` paths. The optimistic-update pattern stays: local `_checkLists` updates immediately, then the network call fires.

Empty names should not be allowed to commit — revert to the previous value if the field is left blank.

### 4.5 Drag-and-drop reordering

Users can long-press an item row to enter drag mode and drop it elsewhere in the same section **or in a different section in the same list**. Reordering also works at the section level — drag a section header to reorder sections within the list.

This requires a new `position: Int` column on both `sections` and `items`. On reorder, the moved row gets a new `position` (and `section_id` if it crossed sections) and the change is persisted. The simplest sufficient strategy is fractional/sparse integer ordering; ties can be re-balanced on the server if drift becomes a problem.

Drag-and-drop is **list-scoped only** — users cannot drag items between different lists in this phase.

### 4.6 Friends & invites

A new **Profile** screen surfaces the user's friend list and pending invites.

**Mechanics:**

- Users find each other by email or username (final lookup field TBD; see §7).
- Sender taps "Add friend" → backend creates a pending friend request and pushes a notification to the receiver.
- Receiver sees the invite on their profile screen, accepts or declines.
- On accept, a bidirectional friendship row is created.

**New tables:**

```
profiles
  user_id (pk, fk -> auth.users), username, display_name, avatar_url

friend_requests
  id, sender_id, receiver_id, status (pending|accepted|declined), created_at

friendships
  id, user_a_id, user_b_id, created_at
  -- enforce user_a_id < user_b_id to avoid duplicates
```

Notifications are delivered in-app via a Supabase Realtime subscription on `friend_requests` and (later) on `list_members`. Push notifications via FCM/APNs are a follow-up, not in this phase.

### 4.7 Shared lists

Once two users are friends, either of them can create a shared list and add the other as an editor. Both users have **editor** role — they can read and write items, sections, and the list itself. The list owner can remove members and delete the list.

**New tables:**

```
shared_lists  -- or reuse `lists` with an `is_shared` flag; see §7
  id, name, created_at, owner_id

list_members
  list_id, user_id, role (owner|editor|viewer), added_at
  primary key (list_id, user_id)
```

Reads in the Shared Lists tab join `list_members` on the current `user_id`. Editor mutations go through the same Postgrest paths as personal lists, gated by row-level security.

**Real-time editing:** subscribe to `lists`, `sections`, and `items` change feeds filtered by `list_id` for every shared list the user has open. Incoming changes merge into the local state. Conflict resolution is **last-write-wins** at the row level for v1 — fine for low-frequency grocery list edits but should be flagged for revisit if jitter becomes visible.

### 4.8 Recipes

Recipes are a new top-level entity under **My Recipes**.

**Recipe contents:**

- Name
- Optional photo
- Ingredients (list of `RecipeIngredient`: name, quantity, unit, `GrocerySection` tag)
- Steps (ordered list of plain-text instructions)
- Optional notes / servings

**Input methods:**

1. **Manual entry** — add ingredients and steps via form.
2. **Paste text** — user pastes a recipe blob; the app parses it into ingredients and steps.
3. **Image import (stretch within recipes)** — user attaches a photo of a recipe; OCR + parser produce the structured form.

Both (2) and (3) need a parser. The parser should produce a canonical format: each ingredient has a name, an optional quantity/unit, and an inferred `GrocerySection`. A first-pass implementation can use a Supabase Edge Function backed by an LLM call; deterministic regex-based parsing is a fallback for offline. The section inference uses a curated keyword → section dictionary (e.g. "tomato" → Produce, "ground beef" → Meat) seeded from the existing `GrocerySection` catalog.

**Adding a recipe to a list:**

- From a recipe detail screen, "Add to list" prompts the user to pick a target list (personal or shared).
- All ingredients are inserted, **merged by section** — existing sections gain new items; missing sections are created.
- **Deduplication:** if an ingredient with the same case-insensitive name already exists in the target list, the existing item is kept (no duplicate row). Quantity merging is **out of scope** for v1 — we don't try to sum "2 tomatoes" with "1 tomato".

**New tables:**

```
recipes
  id, name, photo_url, servings, notes, owner_id, created_at

recipe_ingredients
  id, recipe_id, name, quantity, unit, section_name, position

recipe_steps
  id, recipe_id, body, position
```

### 4.9 Offline-first & sync

The app moves to a **local-first** model. SQLDelight is the recommended choice for KMP: it generates type-safe Kotlin from SQL and works in `commonMain`.

**Architecture:**

- SQLDelight is the source of truth the UI reads from. Repositories expose `Flow`s backed by SQLDelight queries.
- Writes go to SQLDelight first (synchronous, optimistic), then a sync worker pushes the change to Supabase.
- Supabase Realtime subscriptions feed back into the local DB so other devices' changes propagate.
- Every mutable row needs an `updated_at` timestamp; conflicts resolve by last-write-wins on that timestamp.
- Writes that fail to reach Supabase (no signal, transient error) sit in an outbox table and retry with backoff.

**Implications for the existing code:** `SupabaseRepositoryImpl` becomes one half of a two-layer repository — a `LocalListRepository` (SQLDelight) and a `RemoteListRepository` (current Postgrest code), with a `ListSyncCoordinator` brokering between them. ViewModels depend only on the local repository.

**Guest-mode interaction:** the sync coordinator checks for an authenticated session before pushing changes. In guest mode it short-circuits and only writes to the local DB. When a guest later signs up or signs in, the coordinator runs a one-time migration that uploads every local row tagged with the guest sentinel `user_id`, rewrites those rows with the returned remote IDs, and then resumes normal sync.

## 5. Non-Functional Requirements

- **Targets unchanged:** Android `minSdk 24` / `targetSdk 35`, iOS Arm64 + Simulator Arm64.
- **Real-time latency:** changes from one shared-list editor should appear on the other within 2 seconds under good network conditions.
- **Offline:** all read paths and writes to personal lists must work fully offline; shared-list writes queue and replay when connectivity returns.
- **Auth:** Google sign-in must work on Android and iOS; email/password must support verification and reset; guest mode must be fully usable without a Supabase session; Apple sign-in is hidden until further notice.
- **Privacy:** users only appear in friend-search results by exact email/username match — no fuzzy directory browsing.

## 6. Stretch Goals

These are explicitly out of scope for the next phase but should be designed around.

### 6.1 Item autocomplete from a grocery database

While the user types an item name, suggest completions from a shared catalog of grocery items. The catalog can be:

- Aggregated from user-entered items over time (privacy-friendly, no third party).
- Sourced from an external dataset (e.g. USDA FoodData Central) for higher quality.
- Sourced from a grocery chain's product API (Kroger, Walmart) for store-accurate names.

Auto-complete should also auto-suggest a `GrocerySection` for the item.

### 6.2 Grocery store integration & aisle locations

Connect to a chain's API to (a) verify the user's selected store and (b) look up the **aisle / location** for each item. Items in the list could then sort or visually group by physical store layout, not just by section.

Realistic candidates: Kroger Developer API, Walmart Open API, Instacart partner APIs. Each has its own gotchas around store-specific catalogs and rate limits. Worth a separate spike before committing.

## 7. Open Questions

These need to be answered before or during implementation:

- **Friend lookup field:** email-only, username-only, or both? Usernames need a uniqueness constraint and a public `profiles` table — email lookup leaks fewer identifiers but is harder to remember.
- **Shared list table strategy:** add an `is_shared` flag to the existing `lists` table and let `list_members` drive access, or keep a separate `shared_lists` table? The former is simpler; the latter matches the original ask. Recommendation: single `lists` table + `list_members`.
- **Realtime scope:** subscribe per-open-list, or maintain a persistent subscription to every shared list the user is a member of? The former is cheaper; the latter enables background notifications.
- **Recipe parsing backend:** LLM-via-edge-function vs deterministic parser vs both? Likely both — deterministic first pass, LLM fallback for ambiguous input.
- **Push notifications:** in-scope for friends invites, or in-app only for v1? Current plan is in-app only.
- **Item position storage:** integer with periodic rebalancing, or `lexorank`-style strings? Integer is simpler; lexorank scales better.
- **Guest identifier:** stable per-install UUID stored in shared prefs / `NSUserDefaults`, or a fixed sentinel string? Per-install UUID is safer if a device ever hosts multiple guest users in sequence, but it complicates the migration query.
- **Guest data on sign-out:** when an authenticated user signs out, do we surface a "stay signed in as guest with your data?" prompt, or wipe the local DB? Default proposal: wipe, since the data is tied to a real account that will sync back on next sign-in.

## 8. Phased Rollout (proposal)

Rough implementation order, to be confirmed:

1. **Phase 0 — Auth polish:** fix Google on iOS, hide Apple, finish email verification + password reset, add guest mode (initially backed by in-memory or simple key-value storage; full local persistence lands in Phase 3). Migration step from guest → account is stubbed until Phase 3.
2. **Phase 1 — Foundations:** Dashboard + multi-list support + inline rename. No schema changes beyond removing the "auto-create one list" behavior.
3. **Phase 2 — Drag & drop:** add `position` columns, build the drag-and-drop UI, persist order.
4. **Phase 3 — Offline-first:** introduce SQLDelight, refactor repositories, add the sync coordinator. Complete the guest → account migration path now that the local store exists.
5. **Phase 4 — Friends & shared lists:** profiles, friend requests, list members, Realtime subscriptions.
6. **Phase 5 — Recipes:** recipe CRUD, paste-parse, add-to-list flow.
7. **Phase 6 — Stretch:** autocomplete, store integration.
