package com.broccolist

import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.window.ComposeUIViewController
import com.broccolist.app.presentation.App
import com.broccolist.core.createCoroutineScope
import com.broccolist.di.initKoin
import kotlinx.coroutines.cancel

fun MainViewController() = ComposeUIViewController(
    configure = {
        initKoin()
    }
) {
    val scope = createCoroutineScope()

    DisposableEffect(Unit) {
        onDispose {
            scope.cancel()
        }
    }

    App()
}
