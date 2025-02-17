package com.broccolist

import androidx.compose.ui.window.ComposeUIViewController
import com.broccolist.app.presentation.App
import com.broccolist.di.DependencyProvider
import com.broccolist.di.initKoin
import io.github.jan.supabase.auth.handleDeeplinks
import platform.Foundation.NSURL

fun MainViewController(
    url: NSURL? = null,
) = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    val client = DependencyProvider.provideSupabaseClient()
    println("Handling deeplink: $url")
    if (url != null) {
        client.handleDeeplinks(url)
    }
    App()
}
