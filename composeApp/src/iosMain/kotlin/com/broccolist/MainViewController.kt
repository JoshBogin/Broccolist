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
 * AppDelegate `application(_:open:)` for warm-path deep links (OAuth callback,
 * email verification, password reset). Cold-launch URLs are also routed
 * through here via [MainViewController].
 */
fun handleSupabaseDeeplink(url: NSURL) {
    println("handleSupabaseDeeplink: $url")
    val client = DependencyProvider.provideSupabaseClient()
    client.handleDeeplinks(url)
}
