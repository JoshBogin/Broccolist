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
            supabaseUrl = "https://hyditkiqwwjopzzzqllm.supabase.co",
            supabaseKey = "eyJhbGciOiJIUzI1NiIsInR5cCI6IkpXVCJ9.eyJpc3MiOiJzdXBhYmFzZSIsInJlZiI6Imh5ZGl0a2lxd3dqb3B6enpxbGxtIiwicm9sZSI6ImFub24iLCJpYXQiOjE3MzY4MDIwNzUsImV4cCI6MjA1MjM3ODA3NX0.WU83Gqtdb7kNlRO6qoX3slTcm6I57qMKDSeHzOVtfP8"
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
