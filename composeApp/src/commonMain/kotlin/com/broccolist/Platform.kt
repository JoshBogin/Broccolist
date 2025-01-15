package com.broccolist

interface Platform {
    val name: String
}

expect fun getPlatform(): Platform