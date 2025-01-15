package com.broccolist

import android.app.Application
import com.broccolist.di.initKoin
import org.koin.android.ext.koin.androidContext

class BroccolistApplication : Application() {
    override fun onCreate() {
        super.onCreate()
        initKoin {
            androidContext(this@BroccolistApplication)
        }
    }
}
