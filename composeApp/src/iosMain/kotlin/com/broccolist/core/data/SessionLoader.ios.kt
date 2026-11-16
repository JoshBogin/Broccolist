package com.broccolist.core.data

/**
 * supabase-kt has no lifecycle hooks on Apple platforms (its `setupPlatform`
 * is a no-op there), so the previous behavior was a single load at startup.
 * Keep that: load once, let the auto-refresh timer run.
 */
internal actual fun bindToAppLifecycle(loader: SessionLoader) {
    loader.start()
}
