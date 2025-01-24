package com.broccolist.di

import io.github.jan.supabase.SupabaseClient
import org.koin.core.component.KoinComponent
import org.koin.core.component.get

object DependencyProvider : KoinComponent {
    fun provideSupabaseClient() : SupabaseClient = get()
}
