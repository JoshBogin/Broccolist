package com.broccolist.core

import androidx.lifecycle.ViewModel
import kotlinx.coroutines.CoroutineScope

expect open class MPViewModel() : ViewModel {
    val coroutineScope: CoroutineScope

    fun clear()
}
