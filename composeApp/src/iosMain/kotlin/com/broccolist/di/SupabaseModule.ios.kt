package com.broccolist.di

import io.github.jan.supabase.auth.AuthConfig

actual fun AuthConfig.platformAuthConfig() {
    scheme = "io.supabase.broccolist"
    host = "login-callback"
}
