package com.broccolist.di

import com.broccolist.app.AppViewModel
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.login.presentation.LoginViewModel
import com.broccolist.core.data.HttpClientFactory
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }

    viewModelOf(::CheckListViewModel)
    viewModelOf(::AppViewModel)
    viewModelOf(::LoginViewModel)
//    singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
//    singleOf(::DefaultBookRepository).bind<BookRepository>()
//
//    single {
//        get<DatabaseFactory>().create()
//            .setDriver(BundledSQLiteDriver())
//            .build()
//    }
//    single { get<FavoriteBookDatabase>().favoriteBookDao }
}
