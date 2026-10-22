package com.broccolist.app.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.animation.EnterTransition
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
import com.broccolist.dashboard.presentation.DashboardScreenRoute
import com.broccolist.dashboard.presentation.ProfileScreen
import com.broccolist.dashboard.presentation.RecipesScreen
import com.broccolist.dashboard.presentation.SharedListsScreen
import com.broccolist.login.presentation.LoginScreenRoute
import com.broccolist.login.presentation.LoginViewModel
import com.broccolist.login.presentation.ResetPasswordCreateScreen
import com.broccolist.login.presentation.ResetPasswordRequestScreen
import com.broccolist.login.presentation.ResetPasswordSentScreen
import com.broccolist.login.presentation.ResetPasswordSuccessScreen
import com.broccolist.personallists.presentation.PersonalListsScreenRoute
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
            startDestination = Route.ListGraph,
        ) {
            navigation<Route.ListGraph>(
                startDestination = Route.Dashboard,
            ) {
                // ---------- Dashboard ----------
                composable<Route.Dashboard>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() },
                ) {
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { userSession ->
                        DashboardScreenRoute(
                            userSession = userSession,
                            onOpenMenu = {
                                // Phase 1 stub: tapping the menu opens the
                                // Profile screen too — the design's "side
                                // drawer" can come once we have multiple
                                // global actions worth grouping.
                                navController.navigate(Route.Profile)
                            },
                            onOpenProfile = { navController.navigate(Route.Profile) },
                            onPersonalLists = { navController.navigate(Route.PersonalLists) },
                            onSharedLists = { navController.navigate(Route.SharedLists) },
                            onRecipes = { navController.navigate(Route.Recipes) },
                            onOpenList = { listId ->
                                navController.navigate(Route.CheckList(listId))
                            },
                        )
                    }
                }

                // ---------- Personal Lists ----------
                composable<Route.PersonalLists>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { userSession ->
                        PersonalListsScreenRoute(
                            userSession = userSession,
                            onBack = { navController.popBackStack() },
                            onOpenList = { listId ->
                                navController.navigate(Route.CheckList(listId))
                            },
                        )
                    }
                }

                // ---------- Shared Lists (placeholder) ----------
                composable<Route.SharedLists>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { userSession ->
                        SharedListsScreen(
                            isGuest = userSession == null,
                            onBack = { navController.popBackStack() },
                        )
                    }
                }

                // ---------- Recipes (placeholder) ----------
                composable<Route.Recipes>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { _ ->
                        RecipesScreen(onBack = { navController.popBackStack() })
                    }
                }

                // ---------- Profile (placeholder) ----------
                composable<Route.Profile>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { _ ->
                        ProfileScreen(onBack = { navController.popBackStack() })
                    }
                }

                // ---------- CheckList (now parameterized by listId) ----------
                composable<Route.CheckList>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) { backStackEntry ->
                    val args: Route.CheckList = backStackEntry.toRoute()
                    AuthenticatedContent(
                        authState = authState,
                        appViewModel = viewModel,
                        navController = navController,
                    ) { _ ->
                        val checkListViewModel = koinViewModel<CheckListViewModel>()
                        CheckListScreenRoute(
                            viewModel = checkListViewModel,
                            listId = args.listId,
                            navController = navController,
                            onLogout = {
                                viewModel.exitGuestMode()
                                navController.navigate(Route.Login) {
                                    popUpTo(Route.Dashboard) { inclusive = true }
                                }
                            },
                        )
                    }
                }

                // ---------- Login ----------
                composable<Route.Login>(
                    exitTransition = { slideOutHorizontally() },
                    popEnterTransition = { slideInHorizontally() },
                ) {
                    LoginScreenRoute(
                        onLoginSuccess = {
                            navController.navigate(Route.Dashboard) {
                                popUpTo(Route.Login) { inclusive = true }
                            }
                        },
                        onForgotPassword = {
                            navController.navigate(Route.ResetPasswordRequest)
                        },
                    )
                }

                // ---------- Reset password flow ----------
                composable<Route.ResetPasswordRequest>(
                    enterTransition = { slideInHorizontally { it } },
                    popEnterTransition = { EnterTransition.None },
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
                    popEnterTransition = { EnterTransition.None },
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
                    popEnterTransition = { EnterTransition.None },
                    popExitTransition = { slideOutHorizontally { it } },
                ) {
                    val loginViewModel = koinViewModel<LoginViewModel>()
                    ResetPasswordCreateScreen(
                        onBack = { navController.popBackStack() },
                        onUpdatePassword = { newPassword ->
                            loginViewModel.changePassword(newPassword)
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
