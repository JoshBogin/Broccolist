package com.broccolist.login.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.safeDrawingPadding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TextFieldDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import broccolist.composeapp.generated.resources.Res
import broccolist.composeapp.generated.resources.broccolist_icon
import broccolist.composeapp.generated.resources.cd_broccolist_icon
import broccolist.composeapp.generated.resources.login_subtitle
import broccolist.composeapp.generated.resources.login_title
import com.broccolist.login.presentation.components.OTPDialogState
import com.broccolist.login.presentation.components.PasswordRecoveryDialog
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithApple
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.ui.ProviderButtonContent
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.password.PasswordField
import org.jetbrains.compose.resources.painterResource
import org.jetbrains.compose.resources.stringResource
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoute(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit
) {
    LoginScreen(
        onLoginSuccess = onLoginSuccess,
        supabaseAuth = viewModel.composeAuth,
        sessionStatus = viewModel.sessionStatus.collectAsState().value,
        signIn = viewModel::login,
        signUp = viewModel::signUp,
        signInWithGoogle = viewModel::signInWithGoogle,
        signInWithApple = viewModel::signInWithApple,
        resetPassword = viewModel::resetPassword,
        loginAlert = viewModel.alert.collectAsState().value,
        resetAlert = viewModel::resetAlert,
    )
}

@OptIn(AuthUiExperimental::class, ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    supabaseAuth: ComposeAuth,
    sessionStatus: SessionStatus,
    signIn: (String, String) -> Unit,
    signUp: (String, String) -> Unit,
    signInWithGoogle: () -> Unit,
    signInWithApple: () -> Unit,
    resetPassword: (String) -> Unit,
    loginAlert: String?,
    resetAlert: () -> Unit,
) {
    val googleSignIn = googleSignIn(supabaseAuth, signInWithGoogle)
    val appleSignIn = appleSignIn(supabaseAuth, signInWithApple)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var otpDialogState by remember { mutableStateOf<OTPDialogState>(OTPDialogState.Invisible) }
    var showPasswordRecoveryDialog by remember { mutableStateOf(false) }
    var password by remember { mutableStateOf("") }
    val colorScheme = MaterialTheme.colorScheme

    LaunchedEffect(sessionStatus) {
        when (sessionStatus) {
            is SessionStatus.Authenticated -> {
                onLoginSuccess()
            }

            is SessionStatus.RefreshFailure -> {
                println("Error refreshing session")
            }

            is SessionStatus.Initializing -> {
                println("Initializing")
            }

            is SessionStatus.NotAuthenticated -> {
                println("Not authenticated")
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .clickable(
                onClick = { focusManager.clearFocus() },
                indication = null,
                interactionSource = null,
            )
            .safeDrawingPadding()
            .padding(top = 32.dp)
            .padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text(
            text = stringResource(Res.string.login_title),
            style = MaterialTheme.typography.titleLarge,
            fontSize = 28.sp,
            color = colorScheme.primary,
        )
        HorizontalDivider(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            thickness = 1.dp,
            color = colorScheme.primary,
        )
        Icon(
            painter = painterResource(Res.drawable.broccolist_icon),
            contentDescription = stringResource(Res.string.cd_broccolist_icon),
            tint = colorScheme.primary,
            modifier = Modifier.size(80.dp)
        )

        Spacer(Modifier.height(64.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            singleLine = true,
            label = { Text("E-Mail") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Email,
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(onNext = { focusRequester.requestFocus() }),
            leadingIcon = { Icon(Icons.Outlined.Email, "Mail") },
        )
        PasswordField(
            value = password,
            onValueChange = { password = it },
            modifier = Modifier.focusRequester(focusRequester)
                .padding(top = 10.dp),
            label = { Text("Password") },
            keyboardOptions = KeyboardOptions(
                keyboardType = KeyboardType.Password,
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(onDone = {
                authenticate(isSignUp, signIn, signUp, email, password)
                focusManager.clearFocus()
            }),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = colorScheme.background,
                unfocusedContainerColor = colorScheme.background,
            )
        )
        Button(
            onClick = {
                authenticate(isSignUp, signIn, signUp, email, password)
                focusManager.clearFocus()
            },
            modifier = Modifier.padding(top = 10.dp),
            enabled = email.isNotBlank() && password.isNotBlank()
        ) {
            Text(if (isSignUp) "Register" else "Login")
        }

        // Divider with text
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(32.dp)
                .background(colorScheme.background),
            verticalAlignment = Alignment.CenterVertically
        ) {
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = colorScheme.primary
            )
            Text(
                text = "or",
                modifier = Modifier
                    .padding(vertical = 4.dp, horizontal = 8.dp),
                color = colorScheme.primary,
            )
            HorizontalDivider(
                modifier = Modifier
                    .weight(1f)
                    .padding(horizontal = 16.dp),
                thickness = 1.dp,
                color = colorScheme.primary
            )
        }

        Text(
            text = stringResource(Res.string.login_subtitle),
            style = MaterialTheme.typography.titleSmall,
            fontSize = 16.sp,
            color = colorScheme.onBackground,
        )
        Spacer(Modifier.height(16.dp))
        OutlinedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.background,
                contentColor = colorScheme.primary,
            ),
            border = BorderStroke(1.dp, colorScheme.primary),
            onClick = {
                googleSignIn.startFlow()
            },
        ) {
            ProviderButtonContent(Google)
        }
        Spacer(Modifier.height(8.dp))
        OutlinedButton(
            colors = ButtonDefaults.buttonColors(
                containerColor = colorScheme.background,
                contentColor = colorScheme.primary,
            ),
            border = BorderStroke(1.dp, colorScheme.primary),
            onClick = {
                appleSignIn.startFlow()
            },
        ) {
            ProviderButtonContent(Apple)
        }

        TextButton(onClick = { isSignUp = !isSignUp }) {
            Text(if (isSignUp) "Already have an account? Login" else "Not registered? Register")
        }
        TextButton(onClick = { showPasswordRecoveryDialog = true }) {
            Text("Forgot password?")
        }

        if (showPasswordRecoveryDialog) {
            PasswordRecoveryDialog(
                onDismiss = { showPasswordRecoveryDialog = false },
                onConfirm = { email ->
                    resetPassword(email)
                    otpDialogState = OTPDialogState.Visible(
                        title = "Password recovery",
                        email = email,
                        resetFlow = true
                    )
                }
            )
        }

        if (loginAlert != null) {
            AlertDialog(
                onDismissRequest = {
                    resetAlert()
                },
                text = {
                    Text(loginAlert)
                },
                confirmButton = {
                    TextButton(onClick = {
                        resetAlert()
                    }) {
                        Text("Ok")
                    }
                }
            )
        }
    }
}

@Composable
private fun googleSignIn(
    supabaseAuth: ComposeAuth,
    signInWithGoogle: () -> Unit,
): NativeSignInState {
    val action = supabaseAuth.rememberSignInWithGoogle(
        onResult = { result ->
            when (result) {
                NativeSignInResult.Success -> {
                    println("Logged in successfully!")
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
        },
        fallback = {
            println("Using fallback")
            signInWithGoogle()
        }
    )
    return action
}

@Composable
private fun appleSignIn( // TODO still need to setup apple oauth in supabase,
    //   but this is blocked by apple dev account
    supabaseAuth: ComposeAuth,
    signInWithApple: () -> Unit,
): NativeSignInState {
    val action = supabaseAuth.rememberSignInWithApple(
        onResult = { result ->
            when (result) {
                is NativeSignInResult.Success -> {
                    println("Logged in successfully!")
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
        },
        fallback = {
            signInWithApple()
        }
    )
    return action
}

fun authenticate(
    isSignUp: Boolean,
    signIn: (String, String) -> Unit,
    signUp: (String, String) -> Unit,
    email: String,
    password: String
) {
    if (isSignUp) {
        signUp(email, password)
    } else {
        signIn(email, password)
    }
}
