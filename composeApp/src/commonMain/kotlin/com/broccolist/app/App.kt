package com.broccolist.app

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.lifecycle.ViewModel
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.broccolist.checklist.presentation.CheckListScreenRoute
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.login.presentation.LoginScreenRoute
import com.broccolist.login.presentation.LoginViewModel
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    viewModel: AppViewModel = koinViewModel()
) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()

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
                    AuthenticatedContent(
                        sessionStatus = sessionStatus,
                        navController = navController
                    ) {
                        val checkListViewModel = koinViewModel<CheckListViewModel>()

                        CheckListScreenRoute(
                            viewModel = checkListViewModel,
                        )
                    }
                }

                composable<Route.Login>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    val loginViewModel = koinViewModel<LoginViewModel>()

                    // TODO login screen should navigate back to list screen when authenticated
                    LoginScreenRoute(
                        viewModel = loginViewModel,
                        onLoginSuccess = {
                            navController.navigate(Route.CheckList) {
                                popUpTo(Route.Login) { inclusive = true }
                            }
                        }
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
