package com.broccolist.app.presentation

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
import com.broccolist.app.data.Route
import io.github.jan.supabase.auth.user.UserSession

/**
 * Renders [content] when the user is signed in or in guest mode. For guest
 * sessions, [content] receives `null` for the [UserSession] — downstream
 * ViewModels branch on null to use local-only data paths.
 */
@Composable
fun AuthenticatedContent(
    authState: AppAuthState,
    navController: NavController,
    content: @Composable (UserSession?) -> Unit
) {
    when (authState) {
        is AppAuthState.NotAuthenticated -> {
            println("Not authenticated")
            LaunchedEffect(Unit) {
                navController.navigate(Route.Login) {
                    popUpTo(Route.CheckList) { inclusive = true }
                }
            }
        }
        is AppAuthState.Authenticated -> {
            println("Authenticated")
            content(authState.session)
        }
        is AppAuthState.Guest -> {
            println("Guest session")
            content(null)
        }
        is AppAuthState.Initializing -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                CircularProgressIndicator(modifier = Modifier.size(64.dp))
            }
        }
        is AppAuthState.RefreshFailure -> {
            Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                Text("Error refreshing session")
            }
        }
    }
}
