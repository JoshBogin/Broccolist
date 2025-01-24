package com.broccolist.di

import org.koin.core.KoinApplication
import org.koin.core.context.startKoin

fun initKoin(additionalConfiguration: KoinApplication.() -> Unit = {}) {
    startKoin {
        modules(sharedModule, platformModule, supabaseModule, dataModule)
        additionalConfiguration()
    }
}
