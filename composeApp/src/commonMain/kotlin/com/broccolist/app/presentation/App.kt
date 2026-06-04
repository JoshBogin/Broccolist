package com.broccolist.app.presentation

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
import androidx.navigation.toRoute
import com.broccolist.app.data.Route
import com.broccolist.checklist.presentation.CheckListScreenRoute
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.login.presentation.LoginScreenRoute
import com.broccolist.login.presentation.LoginViewModel
import com.broccolist.login.presentation.ResetPasswordCreateScreen
import com.broccolist.login.presentation.ResetPasswordRequestScreen
import com.broccolist.login.presentation.ResetPasswordSentScreen
import com.broccolist.login.presentation.ResetPasswordSuccessScreen
import org.jetbrains.compose.ui.tooling.preview.Preview
import org.koin.compose.viewmodel.koinViewModel

@Composable
@Preview
fun App(
    viewModel: AppViewModel = koinViewModel()
) {
    val authState by viewModel.authState.collectAsState()

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
                        authState = authState,
                        navController = navController
                    ) { userSession ->
                        val checkListViewModel = koinViewModel<CheckListViewModel>()

                        CheckListScreenRoute(
                            viewModel = checkListViewModel,
                            userSession = userSession,
                            navController = navController,
                            onLogout = {
                                viewModel.exitGuestMode()
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
                        onLoginSuccess = {
                            navController.navigate(Route.CheckList) {
                                popUpTo(Route.Login) { inclusive = true }
                            }
                        },
                        onForgotPassword = {
                            navController.navigate(Route.ResetPasswordRequest)
                        }
                    )
                }

                composable<Route.ResetPasswordRequest>(
                    enterTransition = { slideInHorizontally { it } },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    val loginViewModel = koinViewModel<LoginViewModel>()
                    ResetPasswordRequestScreen(
                        onBack = { navController.popBackStack() },
                        onBackToLogin = {
                            navController.popBackStack(Route.Login, inclusive = false)
                        },
                        onSendResetLink = { email ->
                            loginViewModel.resetPassword(email)
                            navController.navigate(Route.ResetPasswordSent(email))
                        },
                    )
                }

                composable<Route.ResetPasswordSent>(
                    enterTransition = { slideInHorizontally { it } },
                    popExitTransition = { slideOutHorizontally { it } },
                ) { backStackEntry ->
                    val args: Route.ResetPasswordSent = backStackEntry.toRoute()
                    val loginViewModel = koinViewModel<LoginViewModel>()
                    ResetPasswordSentScreen(
                        email = args.email,
                        onBack = { navController.popBackStack() },
                        onContinue = {
                            navController.navigate(Route.ResetPasswordCreate)
                        },
                        onResend = { loginViewModel.resetPassword(args.email) },
                        onBackToLogin = {
                            navController.popBackStack(Route.Login, inclusive = false)
                        },
                    )
                }

                composable<Route.ResetPasswordCreate>(
                    enterTransition = { slideInHorizontally { it } },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    val loginViewModel = koinViewModel<LoginViewModel>()
                    ResetPasswordCreateScreen(
                        onBack = { navController.popBackStack() },
                        onUpdatePassword = { newPassword ->
                            loginViewModel.changePassword(newPassword)
                            // Optimistic forward-navigation — if Supabase
                            // rejects the change, the user will see an alert
                            // via the existing loginAlert channel.
                            navController.navigate(Route.ResetPasswordSuccess) {
                                popUpTo(Route.Login) { inclusive = false }
                            }
                        },
                    )
                }

                composable<Route.ResetPasswordSuccess>(
                    enterTransition = { slideInHorizontally { it } },
                ) {
                    ResetPasswordSuccessScreen(
                        onBackToLogin = {
                            navController.popBackStack(Route.Login, inclusive = false)
                        },
                    )
                }
            }
        }
    }
}
