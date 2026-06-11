package com.broccolist.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.ExternalAuthAction
import io.github.jan.supabase.auth.FlowType

actual fun AuthConfig.platformAuthConfig() {
    scheme = "io.supabase.broccolist"
    host = "login-callback"
    defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
    // Android's Custom Tabs path keeps the app in the foreground while the
    // user authenticates, so the in-memory PKCE code verifier survives the
    // round-trip. PKCE > IMPLICIT for security; keep it.
    flowType = FlowType.PKCE
}
