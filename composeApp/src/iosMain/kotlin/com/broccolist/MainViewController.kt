package com.broccolist

import androidx.compose.ui.window.ComposeUIViewController
import com.broccolist.app.presentation.App
import com.broccolist.di.DependencyProvider
import com.broccolist.di.initKoin
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

// SwiftUI may re-create the UIViewController when its props change, which
// would re-invoke `configure` and call `initKoin()` again — that throws
// `KoinAppAlreadyStartedException`. A simple module-level flag is enough to
// keep DI startup idempotent; we don't need Koin's internal context API.
private var koinStarted: Boolean = false

fun MainViewController(
    url: NSURL? = null,
) = ComposeUIViewController(
    configure = {
        if (!koinStarted) {
            initKoin()
            koinStarted = true
        }
    }
) {
    println("Handling deeplink (cold launch): $url")
    if (url != null) {
        handleSupabaseDeeplink(url)
    }
    App()
}

/**
 * Forwards a deep-link URL into Supabase's auth handler. Called from Swift's
 * `ContentView.onOpenURL` for every inbound URL (OAuth callback, email
 * verification, password reset). The SwiftUI `App` lifecycle uses scene-based
 * URL routing, so `AppDelegate.application(_:open:)` is never invoked under
 * this setup — `.onOpenURL` is the single entry point. Cold-launch URLs
 * also flow through here via [MainViewController].
 *
 * Defensive Koin guard: if `.onOpenURL` fires before [MainViewController]'s
 * `configure` block runs (race on cold launch), `DependencyProvider` would
 * fail. Starting Koin here keeps the call site safe in either order.
 */
fun handleSupabaseDeeplink(url: NSURL) {
    println("handleSupabaseDeeplink: $url")
    if (!koinStarted) {
        initKoin()
        koinStarted = true
    }
    val client = DependencyProvider.provideSupabaseClient()
    client.handleDeeplinks(url)
}
