package com.broccolist.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.FlowType

actual fun AuthConfig.platformAuthConfig() {
    scheme = "io.supabase.broccolist"
    host = "login-callback"
    // IMPLICIT (not PKCE): supabase-kt 3.0.3 opens OAuth flows in external
    // Safari on iOS, which can background and kill the app long enough to
    // lose the in-memory PKCE code verifier. Implicit returns tokens
    // directly in the redirect URL fragment, so the round-trip survives an
    // app restart. Revisit when upgrading to supabase-kt 3.1+ (which adds
    // in-app ASWebAuthenticationSession support and makes PKCE viable
    // here too). Android stays on PKCE — see SupabaseModule.android.kt.
    flowType = FlowType.IMPLICIT
}
