package com.broccolist.di

import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.ExternalAuthAction

actual fun AuthConfig.platformAuthConfig() {
    scheme = "io.supabase.broccolist"
    host = "login-callback"
    defaultExternalAuthAction = ExternalAuthAction.CustomTabs()
}
