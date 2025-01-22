package com.broccolist.login.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.broccolist_icon
import broccolist.composeapp.generated.resources.cd_broccolist_icon
import broccolist.composeapp.generated.resources.login_subtitle
import broccolist.composeapp.generated.resources.login_title
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource

@Composable
fun LoginScreenRoute(
    supabaseAuth: ComposeAuth,
    onLoginSuccess: () -> Unit
) {
    LoginScreen(
        onLoginSuccess = onLoginSuccess,
        supabaseAuth = supabaseAuth
    )
}

@OptIn(AuthUiExperimental::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    supabaseAuth: ComposeAuth,
) {
    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(MaterialTheme.colorScheme.background),
    ) {
        val googleSignIn = googleSignIn(supabaseAuth, onLoginSuccess)
        val appleSignIn = appleSignIn(supabaseAuth, onLoginSuccess)

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .safeDrawingPadding()
                .padding(24.dp)
                .align(Alignment.TopCenter),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.login_title),
                style = MaterialTheme.typography.titleLarge,
                fontSize = 28.sp,
                color = MaterialTheme.colorScheme.primary,
            )
            HorizontalDivider(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                thickness = 1.dp,
                color = MaterialTheme.colorScheme.primary,
            )
            Icon(
                painter = painterResource(Res.drawable.broccolist_icon),
                contentDescription = stringResource(Res.string.cd_broccolist_icon),
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(48.dp)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            Text(
                text = stringResource(Res.string.login_subtitle),
                style = MaterialTheme.typography.titleSmall,
                fontSize = 16.sp,
                color = MaterialTheme.colorScheme.onBackground,
            )
            Spacer(Modifier.height(16.dp))
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                onClick = {
                    googleSignIn.startFlow()
                },
            ) {
                ProviderButtonContent(Google)
            }
            Spacer(Modifier.height(8.dp))
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = MaterialTheme.colorScheme.background,
                    contentColor = MaterialTheme.colorScheme.primary,
                ),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                onClick = {
                    appleSignIn.startFlow()
                },
            ) {
                ProviderButtonContent(Apple)
            }
        }
    }
}

@Composable
private fun googleSignIn(
    supabaseAuth: ComposeAuth,
    onLoginSuccess: () -> Unit
): NativeSignInState {
    val action = supabaseAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    onLoginSuccess()
                }

                NativeSignInResult.ClosedByUser -> {
                    println("User closed the login dialog")
                }

                is NativeSignInResult.Error -> {
                    println("Error logging in: ${result.message}")
                }

                is NativeSignInResult.NetworkError -> {
                    println("Network error logging in: ${result.message}")
                }
            }
        }
    )
    return action
}

@Composable
private fun appleSignIn( // TODO still need to setup apple oauth in supabase,
    //   but this is blocked by apple dev account
    supabaseAuth: ComposeAuth,
    onLoginSuccess: () -> Unit
): NativeSignInState {
    val action = supabaseAuth.rememberSignInWithApple(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    onLoginSuccess()
                }

                NativeSignInResult.ClosedByUser -> {
                    println("User closed the login dialog")
                }

                is NativeSignInResult.Error -> {
                    println("Error logging in: ${result.message}")
                }

                is NativeSignInResult.NetworkError -> {
                    println("Network error logging in: ${result.message}")
                }
            }
        }
    )
    return action
}
