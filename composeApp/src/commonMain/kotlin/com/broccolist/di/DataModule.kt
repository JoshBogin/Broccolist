package com.broccolist.di

import com.broccolist.core.data.network.AuthApi
import com.broccolist.core.data.network.AuthApiImpl
import org.koin.dsl.module

val dataModule = module {
    single<AuthApi> { AuthApiImpl(get()) }
}
