package com.broccolist.di

import io.github.jan.supabase.auth.Auth
import io.github.jan.supabase.auth.AuthConfig
import io.github.jan.supabase.auth.FlowType
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.appleNativeLogin
import io.github.jan.supabase.compose.auth.googleNativeLogin
import io.github.jan.supabase.createSupabaseClient
import io.github.jan.supabase.logging.LogLevel
import io.github.jan.supabase.postgrest.Postgrest
import io.github.jan.supabase.realtime.Realtime
import org.koin.dsl.module

expect fun AuthConfig.platformAuthConfig()

val supabaseModule = module {
    single {
        createSupabaseClient(
            supabaseUrl = "https://sraqwtqvadmfzmypjgfn.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6InNyYXF3dHF2YWRtZnpteXBqZ2ZuIiwicm9sZSI6InNlcnZpY2Vfcm9sZSIsImlhdCI6MTc1MzQxMjc1MSwiZXhwIjoyMDY4OTg4NzUxfQ.ro_Xwr5bGYRfNgDGol_bG9Cj7UmJB9nRUCVWbh6V0lg"
        ) {
            install(Auth) {
                platformAuthConfig()
                flowType = FlowType.PKCE
            }
            install(ComposeAuth) {
                googleNativeLogin("299296791038-in8oqrmivn8f7a096lgn7if70v32qsth.apps.googleusercontent.com") // Web Client ID
                appleNativeLogin()
            }
            defaultLogLevel = LogLevel.DEBUG
            install(Postgrest)
            install(Realtime)
        }
    }
}
