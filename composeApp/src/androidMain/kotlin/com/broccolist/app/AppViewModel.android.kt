package com.broccolist.app

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope

actual open class MPViewModel actual constructor() : ViewModel() {
    actual val coroutineScope: CoroutineScope
        get() = viewModelScope
}
