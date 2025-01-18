package com.broccolist.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.providers.Google
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.flow.Flow

sealed interface AuthApi {

    suspend fun signInWithGoogle()

    suspend fun signOut()

    fun sessionStatus(): Flow<SessionStatus>

}

internal class AuthApiImpl(
    client: SupabaseClient
) : AuthApi {

    private val auth = client.auth

    override fun sessionStatus(): Flow<SessionStatus> {
        return auth.sessionStatus
    }

    override suspend fun signInWithGoogle() {
        auth.signInWith(Google)
    }

    override suspend fun signOut() {
        auth.signOut()
    }
}
