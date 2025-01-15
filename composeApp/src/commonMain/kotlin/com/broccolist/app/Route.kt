package com.broccolist.app

import kotlinx.serialization.Serializable

sealed interface Route {

    @Serializable
    data object ListGraph: Route

    @Serializable
    data object CheckList: Route
}
