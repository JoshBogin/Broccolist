package com.broccolist.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.cancel

actual open class MPViewModel actual constructor() : ViewModel() {
    actual val coroutineScope: CoroutineScope
        get() = createCoroutineScope()

    actual fun clear() {
        coroutineScope.cancel()
    }
}
