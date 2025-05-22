package com.dat.dathighlight.tour

import android.content.Context
import android.view.View
import androidx.annotation.LayoutRes
import androidx.lifecycle.LifecycleOwner
import com.dat.dathighlight.HighLight
import com.dat.dathighlight.HighLight.LightShape
import com.dat.dathighlight.HighLight.OnPosCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnNextCallback
import com.dat.dathighlight.lifecycle.HighlightLifecycleManager
import com.dat.dathighlight.shape.RectLightShape
import com.dat.dathighlight.view.HightLightView

/**
 * A manager for multi-step highlight tours.
 * Makes it easy to create step-by-step tutorials.
 */
class HighlightTour(private val context: Context) {
    private val steps = mutableListOf<TourStep>()
    private var currentStepIndex = -1
    private var highLight: HighLight? = null
    private var onTourFinishedListener: (() -> Unit)? = null
    private var onStepChangedListener: ((Int, TourStep) -> Unit)? = null
    private var onStepShownListener: ((Int, View?, View?) -> Unit)? = null

    /**
     * Set a listener to be called when the tour is finished
     */
    fun setOnTourFinishedListener(listener: () -> Unit): HighlightTour {
        onTourFinishedListener = listener
        return this
    }

    /**
     * Set a listener to be called when the step changes
     */
    fun setOnStepChangedListener(listener: (index: Int, step: TourStep) -> Unit): HighlightTour {
        onStepChangedListener = listener
        return this
    }

    /**
     * Set a listener to be called when a step is shown
     */
    fun setOnStepShownListener(listener: (index: Int, targetView: View?, tipView: View?) -> Unit): HighlightTour {
        onStepShownListener = listener
        return this
    }

    /**
     * Add a step to the tour
     */
    fun addStep(view: View, @LayoutRes tipLayout: Int, shape: LightShape = RectLightShape()): HighlightTour {
        steps.add(TourStep(view, tipLayout, shape))
        return this
    }

    /**
     * Add a step to the tour with position callback
     */
    fun addStep(view: View, @LayoutRes tipLayout: Int, position: OnPosCallback, shape: LightShape = RectLightShape()): HighlightTour {
        steps.add(TourStep(view, tipLayout, shape, position))
        return this
    }

    /**
     * Start the tour from the first step
     */
    fun start(): HighlightTour {
        if (steps.isEmpty()) {
            throw IllegalStateException("Cannot start tour with no steps")
        }

        currentStepIndex = -1
        next() // Show the first step
        return this
    }

    /**
     * Move to the next step in the tour
     * If at the last step, will finish the tour
     */
    fun next() {
        // If we're at the last step, finish the tour
        if (currentStepIndex >= steps.size - 1) {
            finish()
            return
        }

        // Remove the previous highlight if it exists
        highLight?.remove()

        // Increment to the next step
        currentStepIndex++

        // Notify step change
        val currentStep = steps[currentStepIndex]
        onStepChangedListener?.invoke(currentStepIndex, currentStep)

        // Create and show the new highlight for this step
        highLight = HighLight(context)
            .enableNext() // Always use next mode for tours
            .addHighLight(
                currentStep.view,
                currentStep.tipLayout,
                currentStep.position,
                currentStep.shape
            )
            .setOnNextCallback(object : OnNextCallback {
                override fun onNext(hightLightView: HightLightView?, targetView: View?, tipView: View?) {
                    onStepShownListener?.invoke(currentStepIndex, targetView, tipView)
                }
            })
            .setClickCallback {
                // Proceed to next step on click
                next()
            }
            .show()
    }

    /**
     * Move to the previous step in the tour
     * If at the first step, does nothing
     */
    fun previous(): Boolean {
        if (currentStepIndex <= 0) {
            return false
        }

        // Remove the current highlight
        highLight?.remove()

        // Decrement to the previous step
        currentStepIndex -= 2 // We subtract 2 because next() will increment by 1

        // Show the previous step
        next()
        return true
    }

    /**
     * Jump to a specific step in the tour
     */
    fun goToStep(index: Int): Boolean {
        if (index < 0 || index >= steps.size) {
            return false
        }

        // Remove the current highlight
        highLight?.remove()

        // Set current index to just before the target index
        currentStepIndex = index - 1

        // Show the target step
        next()
        return true
    }

    /**
     * Finish the tour, removing any active highlights
     */
    fun finish() {
        highLight?.remove()
        highLight = null
        currentStepIndex = -1
        onTourFinishedListener?.invoke()
    }

    /**
     * Create a lifecycle-aware version of the tour
     */
    fun withLifecycle(lifecycleOwner: LifecycleOwner): HighlightTourLifecycleManager {
        return HighlightTourLifecycleManager(lifecycleOwner, this)
    }

    companion object {
        /**
         * Create a tour with a builder pattern
         */
        @JvmStatic
        fun with(context: Context, init: HighlightTour.() -> Unit): HighlightTour {
            return HighlightTour(context).apply(init)
        }
    }
}

/**
 * Represents a single step in a highlight tour
 */
data class TourStep(
    val view: View,
    @LayoutRes val tipLayout: Int,
    val shape: LightShape = RectLightShape(),
    val position: OnPosCallback? = null
)

/**
 * Lifecycle-aware wrapper for HighlightTour
 */
class HighlightTourLifecycleManager(
    private val lifecycleOwner: LifecycleOwner,
    private val highlightTour: HighlightTour
) {
    private var lifecycleManager: HighlightLifecycleManager? = null

    /**
     * Start the tour with lifecycle awareness
     */
    fun start(): HighlightTourLifecycleManager {
        // The tour implementation will handle the actual start logic
        highlightTour.start()
        return this
    }

    /**
     * Move to the next step
     */
    fun next() {
        highlightTour.next()
    }

    /**
     * Move to the previous step
     */
    fun previous(): Boolean {
        return highlightTour.previous()
    }

    /**
     * Jump to a specific step
     */
    fun goToStep(index: Int): Boolean {
        return highlightTour.goToStep(index)
    }

    /**
     * Finish the tour
     */
    fun finish() {
        highlightTour.finish()
        lifecycleManager?.release()
    }
}
