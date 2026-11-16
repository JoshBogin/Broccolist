@file:OptIn(io.github.jan.supabase.annotations.SupabaseInternal::class)

package com.broccolist.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import io.ktor.client.plugins.HttpTimeout
import org.koin.dsl.module

/**
 * Platform-specific auth tweaks. Sets the deep link scheme/host on both
 * platforms; also pins the OAuth flow type per platform (PKCE on Android,
 * IMPLICIT on iOS — see the actuals for the reasoning).
 */
expect fun AuthConfig.platformAuthConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://sraqwtqvadmfzmypjgfn.supabase.co",
            // Publishable key — safe to ship in client apps; respects RLS.
            // The previous embedded key was a service_role JWT, which has
            // been rotated. NEVER commit a service_role key.
            supabaseKey = "sb_publishable_rVUbd-F0wEkDUMarDYnyNg_c8YVvztb"
        ) {
            install(Auth) {
                platformAuthConfig()
            }
            install(ComposeAuth) {
                googleNativeLogin("299296791038-in8oqrmivn8f7a096lgn7if70v32qsth.apps.googleusercontent.com") // Web Client ID
                appleNativeLogin()
            }
            defaultLogLevel = LogLevel.DEBUG
            install(Postgrest)
            install(Realtime)
            // supabase-kt creates its own internal Ktor client (separate
            // from `HttpClientFactory.create()`), so the timeouts we set
            // there don't apply to auth/postgrest calls. Configure them
            // here so a no-internet sign-in surfaces a failure in ~10s
            // instead of hanging on the default open-ended timeout.
            httpConfig {
                install(HttpTimeout) {
                    connectTimeoutMillis = 10_000L
                    socketTimeoutMillis = 15_000L
                    requestTimeoutMillis = 15_000L
                }
            }
        }
    }
}
