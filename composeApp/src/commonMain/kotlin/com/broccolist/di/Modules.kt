package com.broccolist.di

import com.broccolist.core.data.HttpClientFactory
import com.broccolist.checklist.presentation.CheckListViewModel
import org.koin.core.module.Module
import org.koin.core.module.dsl.viewModelOf
import org.koin.dsl.module

expect val platformModule: Module

val sharedModule = module {
    single { HttpClientFactory.create(get()) }

    viewModelOf(::CheckListViewModel)
//    singleOf(::KtorRemoteBookDataSource).bind<RemoteBookDataSource>()
//    singleOf(::DefaultBookRepository).bind<BookRepository>()
//
//    single {
//        get<DatabaseFactory>().create()
//            .setDriver(BundledSQLiteDriver())
//            .build()
//    }
//    single { get<FavoriteBookDatabase>().favoriteBookDao }
//
//    viewModelOf(::BookListViewModel)
//    viewModelOf(::BookDetailViewModel)
}
