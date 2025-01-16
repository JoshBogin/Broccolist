package com.broccolist.app

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers

actual open class MPViewModel actual constructor() : ViewModel() {
    actual val coroutineScope: CoroutineScope
        get() = CoroutineScope(Dispatchers.Default)
}
