package com.broccolist.di

import com.broccolist.checklist.data.SupabaseRepository
import com.broccolist.checklist.data.SupabaseRepositoryImpl
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.AuthApiImpl
import com.broccolist.core.data.GuestModeRepository
import com.broccolist.core.data.InMemoryGuestModeRepository
import org.koin.dsl.module

val dataModule = module {
    single<AuthApi> { AuthApiImpl(get()) }
    single<SupabaseRepository> { SupabaseRepositoryImpl(get()) }
    single<GuestModeRepository> { InMemoryGuestModeRepository() }
}
