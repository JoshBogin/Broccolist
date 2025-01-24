package com.broccolist

import androidx.compose.ui.window.ComposeUIViewController
import com.broccolist.app.presentation.App
import com.broccolist.di.initKoin

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) { App() }
