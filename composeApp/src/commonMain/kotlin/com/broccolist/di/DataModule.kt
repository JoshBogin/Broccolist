package com.broccolist.di

import com.broccolist.checklist.data.CheckListRepository
import com.broccolist.checklist.data.CheckListRepositoryImpl
import com.broccolist.core.data.AuthApi
import com.broccolist.core.data.AuthApiImpl
import org.koin.dsl.module

val dataModule = module {
    single<AuthApi> { AuthApiImpl(get()) }
    single<CheckListRepository> { CheckListRepositoryImpl(get()) }
}
