package com.broccolist.core.data

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.LifecycleOwner
import androidx.lifecycle.ProcessLifecycleOwner
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

/**
 * Replaces supabase-kt's own `enableLifecycleCallbacks` observer. Same
 * behavior (load on foreground, stop the refresh timer on background) but
 * routed through [SessionLoader]'s single-flight job. `addObserver` must run
 * on the main thread; if the process is already started, it dispatches
 * `onStart` immediately, which performs the cold-start load.
 */
internal actual fun bindToAppLifecycle(loader: SessionLoader) {
    CoroutineScope(Dispatchers.Main.immediate).launch {
        ProcessLifecycleOwner.get().lifecycle.addObserver(
            object : DefaultLifecycleObserver {
                override fun onStart(owner: LifecycleOwner) = loader.start()
                override fun onStop(owner: LifecycleOwner) = loader.pause()
            }
        )
    }
}
