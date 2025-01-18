package com.broccolist.app

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import io.github.jan.supabase.auth.status.SessionStatus

@Composable
fun AuthenticatedContent(
    sessionStatus: SessionStatus,
    navController: NavController,
    content: @Composable () -> Unit
) {
    when (sessionStatus) {
        is SessionStatus.NotAuthenticated -> {
            LaunchedEffect(Unit) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.CheckList) { inclusive = true }
                }
            }
        }
        is SessionStatus.Authenticated -> {
            content()
        }
        is SessionStatus.Initializing -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
            }
        }
        is SessionStatus.RefreshFailure -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Error refreshing session")
            }
        }
    }
}
