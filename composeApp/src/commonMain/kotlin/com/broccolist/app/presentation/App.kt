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
import com.broccolist.app.data.Route
import com.broccolist.checklist.presentation.CheckListScreenRoute
import com.broccolist.checklist.presentation.CheckListViewModel
import com.broccolist.login.presentation.LoginScreenRoute
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
                    ) { userSession ->
                        val listId = (0..1000).random()
                        viewModel.createList(userSession, listId)

                        val checkListViewModel = koinViewModel<CheckListViewModel>()

                        CheckListScreenRoute(
                            viewModel = checkListViewModel,
                            userSession = userSession,
                            listId = listId,
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
