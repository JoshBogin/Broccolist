package com.broccolist.app

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.navigation
import androidx.navigation.compose.rememberNavController
import com.broccolist.checklist.presentation.CheckListScreenRoute
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.login.presentation.LoginScreenRoute
import io.github.jan.supabase.compose.auth.composeAuth
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    viewModel: AppViewModel = koinViewModel()
) {
    val sessionStatus by viewModel.sessionStatus.collectAsState()
    val supabaseClient = viewModel.supabaseClient

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
                            onLogout = {
                                navController.navigate(Route.Login) {
                                    popUpTo(Route.CheckList) { inclusive = true }
                                }
                            }
                        )
                    }
                }

                composable<Route.Login>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() }
                ) {
                    LoginScreenRoute(
                        supabaseAuth = supabaseClient.composeAuth,
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
