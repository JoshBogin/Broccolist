package com.broccolist.app.presentation

import androidx.lifecycle.Lifecycle
import androidx.navigation.NavBackStackEntry

/**
 * Runs [block] only while this destination is the settled, on-screen one.
 *
 * `navigate()` / `popBackStack()` mutate the back stack immediately, but the
 * outgoing screen stays visible and tappable for the length of the slide
 * transition. Without a guard, a double-tap pushes the same screen twice (or
 * pops two screens). A back-stack entry is only RESUMED once its enter
 * transition has finished and drops out of RESUMED as soon as navigation away
 * from it starts, so checking it ignores exactly the taps that land
 * mid-transition — no timers to tune.
 *
 * Wrap the whole tap handler, not just the navigate call, so side effects
 * that accompany navigation (sending a reset email, changing a password) are
 * de-duplicated too.
 *
 * Don't use this for state-driven navigation (auth redirects): those must
 * happen even if a transition is in flight.
 */
inline fun NavBackStackEntry.ifResumed(block: () -> Unit) {
    if (lifecycle.currentState == Lifecycle.State.RESUMED) block()
}
