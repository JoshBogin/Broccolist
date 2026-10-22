package com.broccolist.di

import com.broccolist.app.presentation.AppViewModel
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.core.data.HttpClientFactory
import com.broccolist.dashboard.presentation.DashboardViewModel
import com.broccolist.login.presentation.LoginViewModel
import com.broccolist.personallists.presentation.PersonalListsViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }

    viewModelOf(::CheckListViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::LoginViewModel)
    viewModelOf(::DashboardViewModel)
    viewModelOf(::PersonalListsViewModel)
}
