package com.dat.dathighlight.lifecycle

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleEventObserver
import androidx.lifecycle.LifecycleOwner
import com.dat.dathighlight.HighLight

/**
 * Lifecycle-aware wrapper for HighLight that automatically handles cleanup
 * when the lifecycle owner is destroyed, preventing memory leaks.
 */
class HighlightLifecycleManager(
    private val lifecycleOwner: LifecycleOwner,
    context: () -> HighLight
) : LifecycleEventObserver {

    private var highLight: HighLight? = context()

    init {
        lifecycleOwner.lifecycle.addObserver(this)
    }

    override fun onStateChanged(source: LifecycleOwner, event: Lifecycle.Event) {
        if (event == Lifecycle.Event.ON_DESTROY) {
            highLight?.remove()
            highLight = null
            lifecycleOwner.lifecycle.removeObserver(this)
        }
    }

    /**
     * Gets the managed HighLight instance.
     * Returns null if the lifecycle owner has been destroyed.
     */
    fun getHighlight(): HighLight? = highLight

    /**
     * Manually release resources and remove highlight if needed
     */
    fun release() {
        highLight?.remove()
        highLight = null
        lifecycleOwner.lifecycle.removeObserver(this)
    }
}
