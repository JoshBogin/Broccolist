package com.broccolist.login.presentation

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.outlined.Email
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme.colorScheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.OutlinedTextFieldDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.style.TextDecoration.Companion.Underline
import androidx.compose.ui.text.withStyle
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.broccolist.login.presentation.components.BroccolistHeader
import io.github.jan.supabase.annotations.SupabaseInternal
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composable.NativeSignInResult
import io.github.jan.supabase.compose.auth.composable.NativeSignInState
import io.github.jan.supabase.compose.auth.composable.rememberSignInWithGoogle
import io.github.jan.supabase.compose.auth.ui.ProviderIcon
import io.github.jan.supabase.compose.auth.ui.annotations.AuthUiExperimental
import io.github.jan.supabase.compose.auth.ui.password.PasswordField
import org.koin.compose.viewmodel.koinViewModel

@Composable
fun LoginScreenRoute(
    viewModel: LoginViewModel = koinViewModel(),
    onLoginSuccess: () -> Unit,
    onForgotPassword: () -> Unit,
) {
    LoginScreen(
        onLoginSuccess = onLoginSuccess,
        supabaseAuth = viewModel.composeAuth,
        sessionStatus = viewModel.sessionStatus.collectAsState().value,
        signIn = viewModel::login,
        signUp = viewModel::signUp,
        signInWithGoogle = viewModel::signInWithGoogle,
        continueAsGuest = viewModel::continueAsGuest,
        onForgotPassword = onForgotPassword,
        loginAlert = viewModel.alert.collectAsState().value,
        isAuthenticating = viewModel.isAuthenticating.collectAsState().value,
        resetAlert = viewModel::resetAlert,
    )
}

@OptIn(AuthUiExperimental::class, ExperimentalMaterial3Api::class, SupabaseInternal::class)
@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    supabaseAuth: ComposeAuth,
    sessionStatus: SessionStatus,
    signIn: (String, String) -> Unit,
    signUp: (String, String) -> Unit,
    signInWithGoogle: () -> Unit,
    continueAsGuest: () -> Unit,
    onForgotPassword: () -> Unit,
    loginAlert: String?,
    isAuthenticating: Boolean,
    resetAlert: () -> Unit,
) {
    val googleSignIn = googleSignIn(supabaseAuth, signInWithGoogle)
    val focusRequester = remember { FocusRequester() }
    val focusManager = LocalFocusManager.current
    var isSignUp by remember { mutableStateOf(false) }
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }

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

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(colorScheme.background)
            .clickable(
                onClick = { focusManager.clearFocus() },
                indication = null,
                interactionSource = null,
            )
            .navigationBarsPadding()
    ) {
        Column(
            modifier = Modifier
                .padding(top = 32.dp)
                .padding(24.dp)
                .align(Alignment.Center),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally,
        ) {
            BroccolistHeader()

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
                modifier = Modifier.fillMaxWidth(),
            )

            Spacer(Modifier.height(10.dp))

            PasswordField(
                value = password,
                onValueChange = { password = it },
                modifier = Modifier
                    .focusRequester(focusRequester)
                    .fillMaxWidth(),
                label = { Text("Password") },
                keyboardOptions = KeyboardOptions(
                    keyboardType = KeyboardType.Password,
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(onDone = {
                    authenticate(isSignUp, signIn, signUp, email, password)
                    focusManager.clearFocus()
                }),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedContainerColor = colorScheme.background,
                    unfocusedContainerColor = colorScheme.background,
                ),
                shape = OutlinedTextFieldDefaults.shape,
                supportingText = null,
            )

            if (!isSignUp) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.End,
                ) {
                    TextButton(
                        onClick = onForgotPassword,
                        contentPadding = ButtonDefaults.TextButtonContentPadding,
                        shape = ButtonDefaults.textShape,
                    ) {
                        Text(
                            text = "Forgot password?",
                            color = colorScheme.primary,
                            fontWeight = FontWeight.SemiBold,
                        )
                    }
                }
            } else {
                Spacer(Modifier.height(8.dp))
            }

            Button(
                onClick = {
                    authenticate(isSignUp, signIn, signUp, email, password)
                    focusManager.clearFocus()
                },
                // Disable while the auth call is in flight so a double-tap
                // doesn't fire a second request. The button still shows the
                // "active" color so the user can see something is happening
                // — the spinner replaces the label.
                enabled = email.isNotBlank() && password.isNotBlank() && !isAuthenticating,
                shape = RoundedCornerShape(28.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.primary,
                    contentColor = colorScheme.onPrimary,
                    // Light-green disabled state per design — uses
                    // primaryContainer so it reads as "available but not
                    // yet active" rather than greyed-out neutral. When
                    // `isAuthenticating` is true we override below so the
                    // button stays dark green with a spinner.
                    disabledContainerColor =
                        if (isAuthenticating) colorScheme.primary else colorScheme.primaryContainer,
                    disabledContentColor =
                        if (isAuthenticating) colorScheme.onPrimary else colorScheme.primary,
                ),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                if (isAuthenticating) {
                    CircularProgressIndicator(
                        modifier = Modifier.size(24.dp),
                        color = colorScheme.onPrimary,
                        strokeWidth = 2.5.dp,
                    )
                } else {
                    Text(
                        text = if (isSignUp) "Register" else "Login",
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 16.sp,
                    )
                }
            }

            // Centered "or login with" — no dividers, sentence-case copy.
            Text(
                text = if (isSignUp) "Or sign up with" else "Or login with",
                modifier = Modifier.padding(vertical = 24.dp),
                color = colorScheme.primary,
                fontWeight = FontWeight.SemiBold,
            )

            // Apple sign-in is intentionally hidden until an Apple developer
            // account is provisioned (see project_docs/PRD.md §4.1).
            OutlinedButton(
                colors = ButtonDefaults.buttonColors(
                    containerColor = colorScheme.background,
                    contentColor = colorScheme.primary,
                ),
                border = BorderStroke(1.dp, colorScheme.primary),
                onClick = {
                    googleSignIn.startFlow()
                },
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
            ) {
                ProviderIcon(
                    provider = Google,
                    contentDescription = "Google",
                    modifier = Modifier.size(28.dp),
                )
                Spacer(Modifier.size(12.dp))
                Text(
                    text = "Continue with Google",
                    fontWeight = FontWeight.SemiBold,
                )
            }

            TextButton(
                onClick = {
                    continueAsGuest()
                    onLoginSuccess()
                },
                modifier = Modifier.padding(top = 8.dp),
            ) {
                Text("Continue as Guest")
            }
        }

        TextButton(
            onClick = { isSignUp = !isSignUp },
            interactionSource = null, // TODO figure out how to control this
            modifier = Modifier.align(Alignment.BottomCenter),
        ) {
            // annotated string where the login or register text is underlined
            val annotatedString = buildAnnotatedString {
                append(if (isSignUp) "Already have an account? " else "Not registered? ")
                withStyle(style = SpanStyle(textDecoration = Underline)) {
                    append(if (isSignUp) "Login" else "Register")
                }
            }
            Text(annotatedString)
        }
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
