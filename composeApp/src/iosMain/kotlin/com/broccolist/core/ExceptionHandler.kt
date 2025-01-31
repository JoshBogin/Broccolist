package com.broccolist.core

import kotlinx.cinterop.BetaInteropApi
import kotlinx.cinterop.autoreleasepool
import platform.Foundation.NSLog
import kotlin.native.concurrent.ThreadLocal

@ThreadLocal
object ExceptionHandler {
    @OptIn(BetaInteropApi::class)
    fun handle(exception: Throwable) {
        autoreleasepool {
            NSLog("KMP Exception: ${exception.message}")
            exception.printStackTrace()
        }
    }
}
