package com.broccolist.app

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.broccolist.checklist.presentation.CheckListScreenRoot
import com.broccolist.checklist.presentation.CheckListViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App() {
    BroccolistTheme(
        darkTheme = isSystemInDarkTheme()
    ) {
        val navController = rememberNavController()
        NavHost(
            navController = navController,
            startDestination = Route.ListGraph
        ) {
            navigation<Route.ListGraph>(
                startDestination = Route.CheckList
            ) {
                composable<Route.CheckList>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    val viewModel = koinViewModel<CheckListViewModel>()

                    CheckListScreenRoot(
                        viewModel = viewModel,
                    )
                }
            }
        }

    }
}

@Composable
private inline fun <reified T: ViewModel> NavBackStackEntry.sharedKoinViewModel(
    navController: NavController
): T {
    val navGraphRoute = destination.parent?.route ?: return koinViewModel<T>()
    val parentEntry = remember(this) {
        navController.getBackStackEntry(navGraphRoute)
    }
    return koinViewModel(
        viewModelStoreOwner = parentEntry
    )
}
