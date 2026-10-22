# Broccolist 🥦

Broccolist is a Kotlin Multiplatform (KMP) and Compose Multiplatform grocery list application for Android and iOS. It allows users to organize grocery lists by store sections, manage items with optimistic UI updates, and authenticate securely using email, Google, or explore in Guest Mode.

---

## Features

- **Store-Section Organized Grocery Lists**
  - Categorize items under 10 predefined store sections (`Produce`, `Deli`, `Meat`, `Bakery`, `Pantry`, `Snacks`, `Beverages`, `Frozen`, `Dairy`, `Other`) or create custom sections.
  - Add, check off, and delete items with intuitive swipe gestures.
  - Section reordering and renaming support.
  - Optimistic UI updates for snappy, immediate user feedback.

- **Flexible Authentication**
  - **Email & Password**: Includes email verification enforcement and a complete 4-step password reset flow (`Request` → `Sent` → `Create` → `Success`).
  - **Google Sign-In**: Native authentication using Android Credential Manager & Play Services Auth; web OAuth fallback on iOS.
  - **Guest Mode**: Allows users to explore and manage lists locally without creating an account (in-memory persistence; automatically cleared upon signing in).

- **Deep Linking**
  - Handles auth callbacks (`io.supabase.broccolist://login-callback`) across Android and iOS for seamless email confirmation and password recovery.

- **Polished Material 3 Design**
  - Responsive layout, custom color tokens (`BroccolistColors`) with dark mode support, and smooth navigation animations.

---

## Tech Stack

| Domain | Technology | Version |
| :--- | :--- | :--- |
| **Language & Platform** | Kotlin Multiplatform | `2.1.0` |
| **UI Framework** | Compose Multiplatform | `1.7.0` |
| **Design System** | Material 3 | `1.3.1` |
| **Navigation** | JetBrains Navigation Compose | `2.8.0-alpha10` |
| **Dependency Injection** | Koin | `4.0.0` |
| **Backend & Auth** | Supabase (`postgrest-kt`, `auth-kt`, `realtime-kt`) | `3.0.3` |
| **Networking** | Ktor Client (OkHttp on Android, Darwin on iOS) | `3.0.3` |
| **Serialization** | kotlinx.serialization | `1.7.3` |
| **Async Programming** | Kotlin Coroutines & StateFlow | `1.10.1` |
| **Build System** | Gradle / AGP | `8.6.1` / `8.5.2` |
| **Linting** | Detekt | `1.23.0` |

---

## Architecture

The project adopts **Clean Architecture** principles structured by feature (`checklist/`, `login/`, `core/`, `app/`):

```
composeApp/src/commonMain/kotlin/com/broccolist/
├── app/          # App entry point, AppViewModel, AppAuthState, NavGraph & Routes
├── checklist/    # Main feature: domain models, repository, ViewModel & Composable screens
├── login/        # Auth screens, LoginViewModel, multi-step reset password flow
├── core/         # AuthApi, GuestModeRepository, HttpClientFactory
├── theme/        # Material3 BroccolistTheme, Color palette, Typography
└── di/           # Koin DI modules (DataModule, SupabaseModule, platform bindings)
```

### Key Architectural Concepts
- **Shared Business Logic & UI**: Nearly all UI, view models, domain rules, and data repositories reside in `commonMain`.
- **Platform Interoperability**: Platform-specific logic (e.g., Ktor engines, PKCE vs IMPLICIT auth flows, credential managers) is implemented using `expect`/`actual` declarations.
- **State Management**: ViewModels utilize `MutableStateFlow` exposing immutable state to Composables via `collectAsStateWithLifecycle()`.
- **Row Level Security (RLS)**: Database tables (`lists`, `sections`, `items`) are protected by Supabase RLS policies scoped to `auth.uid()`.

---

## Project Structure

- **`composeApp/`**: Main Kotlin Multiplatform module containing shared code and platform-specific entries.
  - `src/commonMain`: Shared Composables, ViewModels, Supabase integration, and domain logic.
  - `src/androidMain`: Android-specific setup (`BroccolistApplication`, `MainActivity`, Google Credential Manager).
  - `src/iosMain`: iOS platform bindings and `MainViewController`.
- **`iosApp/`**: SwiftUI wrapper application for iOS, managing lifecycle events and deep link handling (`.onOpenURL`).

---

## Prerequisites

Before building Broccolist, ensure you have installed:

- **JDK 17** or higher
- **Android Studio** (Ladybug / Jellyfish or newer with Kotlin Multiplatform plugin)
- **Xcode 15+** (for building iOS target; macOS required)
- **CocoaPods** (`sudo gem install cocoapods`)

---

## Getting Started & How to Run

### 1. Clone the Repository
```bash
git clone https://github.com/your-username/Broccolist.git
cd Broccolist
```

### 2. Run on Android

You can build and run directly from Android Studio using the `composeApp` run configuration, or via the command line:

```bash
# Build debug APK
./gradlew assembleDebug

# Install and launch on an attached Android emulator/device
./gradlew installDebug
```

### 3. Run on iOS

1. Compile the shared Kotlin framework:
   ```bash
   ./gradlew linkDebugFrameworkIosSimulatorArm64
   ```
2. Install CocoaPods dependencies:
   ```bash
   cd iosApp
   pod install
   cd ..
   ```
3. Open `iosApp/iosApp.xcworkspace` in **Xcode**.
4. Select an iOS Simulator or connected physical device and press **Run** (`Cmd + R`).

---

## Code Quality & Utilities

```bash
# Run Detekt static analysis
./gradlew detekt

# Clean build artifacts
./gradlew clean
```

---

## License

This project is licensed under the MIT License - see the repository details for more info.
