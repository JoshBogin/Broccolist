package com.broccolist.core.data

import io.github.jan.supabase.SupabaseClient
import io.github.jan.supabase.auth.auth
import io.github.jan.supabase.auth.status.SessionStatus
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancelAndJoin
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * The single owner of "load the saved session from storage (refreshing it if
 * expired) and start auto-refresh".
 *
 * supabase-kt 3.0.3 has two independent triggers for that work on Android:
 * `Auth.init()` (when `autoLoadFromStorage = true`) and the
 * `ProcessLifecycleOwner.onStart` callback (when `enableLifecycleCallbacks =
 * true`). On a cold start with an expired access token both fire at once,
 * because the "is auto-refresh running?" guard only becomes true *after* the
 * refresh succeeds. Two refreshes race on the same refresh token, and a
 * coroutine cancelled mid-refresh is swallowed by the library's
 * `catch (e: Exception)` as a "network error" — leaving the status stuck at
 * `RefreshFailure` with no retry loop alive. Both library triggers are
 * disabled in `SupabaseModule`; this class replaces them with one
 * single-flight job.
 *
 * All bookkeeping runs on [Dispatchers.Main], so `loadJob` is only ever
 * touched from one thread and calls from the ViewModel / lifecycle observer
 * are serialized.
 */
class SessionLoader(client: SupabaseClient) {

    private val auth = client.auth
    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main.immediate)
    private var loadJob: Job? = null

    private val _initialLoadComplete = MutableStateFlow(false)

    /**
     * False until the first load-from-storage attempt has finished. With
     * `autoLoadFromStorage = false` the library reports
     * `NotAuthenticated` right away; callers should treat that as
     * "still initializing" while this is false, or they'd bounce a signed-in
     * user to Login during the refresh.
     */
    val initialLoadComplete: StateFlow<Boolean> = _initialLoadComplete.asStateFlow()

    init {
        bindToAppLifecycle(this)
    }

    /**
     * Loads the saved session unless a load is already in flight or the
     * session is already live with auto-refresh running. Safe to call
     * repeatedly (cold start, every foreground).
     */
    fun start() {
        scope.launch {
            if (loadJob?.isActive == true || auth.isAutoRefreshRunning) return@launch
            launchLoad()
        }
    }

    /**
     * Backs "Try again" on the refresh-failure screen. If a load is stuck in
     * the library's network-retry delay, cancel it and attempt immediately.
     * Re-reads the session from storage rather than calling
     * `refreshCurrentSession()`: in `RefreshFailure` the in-memory session is
     * null (`currentSessionOrNull()` only returns it while `Authenticated`),
     * so that call can only ever throw "No refresh token found".
     */
    fun retry() {
        scope.launch {
            loadJob?.cancelAndJoin()
            launchLoad()
        }
    }

    /**
     * Abandon any in-flight load. Call before signing out from the
     * refresh-failure screen; otherwise the library's network-retry loop
     * could re-import the old session ~10s after the user signed out.
     */
    suspend fun cancel() {
        scope.launch { loadJob?.cancelAndJoin() }.join()
    }

    /** App went to the background: stop the refresh timer (mirrors the library's onStop). */
    fun pause() {
        scope.launch {
            if (auth.isAutoRefreshRunning) auth.stopAutoRefreshForCurrentSession()
        }
    }

    private fun launchLoad() {
        loadJob = scope.launch {
            try {
                // Suspends through the refresh (and the library's own 10s
                // network-retry loop) until the session is live or cleared.
                val found = auth.loadFromStorage()
                if (!found && auth.sessionStatus.value is SessionStatus.RefreshFailure) {
                    // Retry after the stored session vanished — nothing left
                    // to refresh, so drop to NotAuthenticated (→ Login).
                    auth.clearSession()
                }
                _initialLoadComplete.value = true
            } catch (e: CancellationException) {
                // Superseded by retry(); the new job owns completion.
                throw e
            } catch (e: Exception) {
                // Corrupt stored session etc. Status stays NotAuthenticated.
                println("SessionLoader: failed to load session from storage: $e")
                _initialLoadComplete.value = true
            }
        }
    }
}

/**
 * Hooks [SessionLoader.start] / [SessionLoader.pause] to the platform's
 * foreground/background signals, and kicks off the first load.
 */
internal expect fun bindToAppLifecycle(loader: SessionLoader)
