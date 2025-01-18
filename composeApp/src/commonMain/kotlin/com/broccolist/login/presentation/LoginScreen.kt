package com.broccolist.login.presentation

import BroccolistTheme.BroccolistTheme
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ShoppingCart
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.broccolist.core.domain.UserState
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoute(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    LoginScreen(
        userState = viewModel.userState.collectAsStateWithLifecycle().value,
        googleLogin = viewModel::loginWithGoogle,
        onLoginSuccess = onLoginSuccess
    )
}

@Composable
fun LoginScreen(
    userState: UserState,
    googleLogin: () -> Unit,
    onLoginSuccess: () -> Unit
) {

    BroccolistTheme {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            OutlinedButton(
                shape = RoundedCornerShape(24.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.primary,
                ),
                onClick = {
                    googleLogin()
                    onLoginSuccess()
                },
            ) {
                Icon(
                    imageVector = Icons.Default.ShoppingCart,
                    contentDescription = "Google Login Icon",
                    tint = MaterialTheme.colorScheme.onPrimary
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = "Login with Google",
                    color = MaterialTheme.colorScheme.onPrimary
                )
            }
        }
    }

    when (userState) {
        UserState.NotAuthenticated -> {
            // Show login screen
        }
        UserState.Authenticated -> {
            // Show home screen
        }
        UserState.Loading -> {
            // Show loading screen

        }
        UserState.Error -> {
            // Show error screen
        }
    }
}
