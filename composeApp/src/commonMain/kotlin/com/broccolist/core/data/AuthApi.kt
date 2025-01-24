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

    suspend fun signUp(email: String, password: String)

    suspend fun signInWithGoogle()

    suspend fun signInWithApple()

    suspend fun signOut()

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

    override suspend fun changePassword(newPassword: String) {
        auth.updateUser {
            this.password = newPassword
        }
    }

    override suspend fun resetPassword(email: String) {
        auth.resetPasswordForEmail(email)
    }
}
