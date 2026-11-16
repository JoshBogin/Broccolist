package com.broccolist.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Apple
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.providers.builtin.Email
import io.github.jan.supabase.auth.status.SessionStatus
import io.github.jan.supabase.compose.auth.ComposeAuth
import io.github.jan.supabase.compose.auth.composeAuth
import kotlinx.coroutines.flow.Flow

sealed interface AuthApi {

    suspend fun signIn(email: String, password: String)

    /**
     * Creates a new Supabase account. The Supabase project is configured to
     * require email confirmation, so the returned session is null until the
     * user clicks the verification link in their inbox.
     */
    suspend fun signUp(email: String, password: String)

    /**
     * Re-sends the verification email for an existing unverified account.
     * Safe to call repeatedly; Supabase enforces its own rate limiting.
     */
    suspend fun resendVerificationEmail(email: String)

    suspend fun signInWithGoogle()

    /**
     * Apple sign-in is intentionally hidden from the UI until an Apple
     * developer account is in place. The API surface is preserved so the
     * provider can be re-enabled without touching the data layer.
     */
    suspend fun signInWithApple()

    suspend fun signOut()

    /**
     * Force-refresh the current session using the stored refresh token.
     * Throws if no refresh token exists or Supabase rejects the request —
     * callers should catch and route the user back to sign-in.
     */
    suspend fun refreshSession()

    suspend fun resetPassword(email: String)

    suspend fun changePassword(newPassword: String)

    fun sessionStatus(): Flow<SessionStatus>

    fun composeAuth(): ComposeAuth

}

internal class AuthApiImpl(
    client: SupabaseClient
) : AuthApi {

    private val auth = client.auth
    private val composeAuth = client.composeAuth

    override fun composeAuth(): ComposeAuth {
        return composeAuth
    }

    override fun sessionStatus(): Flow<SessionStatus> {
        return auth.sessionStatus
    }

    override suspend fun signIn(email: String, password: String) {
        auth.signInWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun signUp(email: String, password: String) {
        auth.signUpWith(Email) {
            this.email = email
            this.password = password
        }
    }

    override suspend fun resendVerificationEmail(email: String) {
        // Supabase's `resend` API takes an OtpType; signup verifications use
        // the `signup` type. Wrapping in a try/catch keeps the caller simple —
        // a failure here surfaces as a user-visible alert.
        auth.resendEmail(
            type = io.github.jan.supabase.auth.OtpType.Email.SIGNUP,
            email = email,
        )
    }

    override suspend fun signInWithGoogle() {
        try {
            val result = auth.signInWith(Google)
            println("Google sign-in result: $result")
        } catch (e: Exception) {
            println("Error in signInWithGoogle: ${e.message}")
            throw e
        }
    }

    override suspend fun signInWithApple() {
         auth.signInWith(Apple)
    }

    override suspend fun signOut() {
        auth.signOut()
    }

    override suspend fun refreshSession() {
        auth.refreshCurrentSession()
    }

    override suspend fun changePassword(newPassword: String) {
        auth.updateUser {
            this.password = newPassword
        }
    }

    override suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email)
    }
}
