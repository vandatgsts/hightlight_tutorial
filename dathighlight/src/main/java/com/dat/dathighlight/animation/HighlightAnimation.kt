package com.dat.dathighlight.animation

import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.animation.AnimatorSet
import android.animation.ObjectAnimator
import android.view.View
import android.view.animation.AccelerateDecelerateInterpolator
import android.view.animation.Interpolator

/**
 * Interface for custom highlight animations
 */
interface HighlightAnimation {
    /**
     * Animate a view being shown
     * @param view The view to animate
     * @param onFinished Callback when animation completes
     */
    fun animateShow(view: View, onFinished: () -> Unit = {})

    /**
     * Animate a view being hidden
     * @param view The view to animate
     * @param onFinished Callback when animation completes
     */
    fun animateHide(view: View, onFinished: () -> Unit = {})
}

/**
 * Built-in animation types for tooltips
 */
enum class AnimationType {
    NONE,    // No animation
    FADE,    // Fade in/out
    SCALE,   // Scale up/down
    SLIDE_TOP,    // Slide from/to top
    SLIDE_BOTTOM, // Slide from/to bottom
    SLIDE_LEFT,   // Slide from/to left
    SLIDE_RIGHT   // Slide from/to right
}

/**
 * Helper class to create and manage highlight animations
 */
object HighlightAnimationHelper {
    private const val DEFAULT_DURATION = 300L

    /**
     * Create a standard highlight animation based on type
     * @param type Animation type to create
     * @param duration Duration of the animation in milliseconds
     * @param interpolator Optional custom interpolator
     */
    @JvmStatic
    fun createAnimation(
        type: AnimationType,
        duration: Long = DEFAULT_DURATION,
        interpolator: Interpolator = AccelerateDecelerateInterpolator()
    ): HighlightAnimation {
        return when (type) {
            AnimationType.NONE -> NoAnimation()
            AnimationType.FADE -> FadeAnimation(duration, interpolator)
            AnimationType.SCALE -> ScaleAnimation(duration, interpolator)
            AnimationType.SLIDE_TOP -> SlideAnimation(SlideDirection.TOP, duration, interpolator)
            AnimationType.SLIDE_BOTTOM -> SlideAnimation(SlideDirection.BOTTOM, duration, interpolator)
            AnimationType.SLIDE_LEFT -> SlideAnimation(SlideDirection.LEFT, duration, interpolator)
            AnimationType.SLIDE_RIGHT -> SlideAnimation(SlideDirection.RIGHT, duration, interpolator)
        }
    }

    /**
     * Animation that does nothing
     */
    class NoAnimation : HighlightAnimation {
        override fun animateShow(view: View, onFinished: () -> Unit) {
            view.visibility = View.VISIBLE
            onFinished()
        }

        override fun animateHide(view: View, onFinished: () -> Unit) {
            view.visibility = View.GONE
            onFinished()
        }
    }

    /**
     * Animation that fades the view in/out
     */
    class FadeAnimation(
        private val duration: Long = DEFAULT_DURATION,
        private val interpolator: Interpolator = AccelerateDecelerateInterpolator()
    ) : HighlightAnimation {
        override fun animateShow(view: View, onFinished: () -> Unit) {
            view.alpha = 0f
            view.visibility = View.VISIBLE

            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f).apply {
                this.duration = this@FadeAnimation.duration
                this.interpolator = this@FadeAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished()
                    }
                })
                start()
            }
        }

        override fun animateHide(view: View, onFinished: () -> Unit) {
            ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f).apply {
                this.duration = this@FadeAnimation.duration
                this.interpolator = this@FadeAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                        onFinished()
                    }
                })
                start()
            }
        }
    }

    /**
     * Animation that scales the view in/out
     */
    class ScaleAnimation(
        private val duration: Long = DEFAULT_DURATION,
        private val interpolator: Interpolator = AccelerateDecelerateInterpolator(),
        private val fromScale: Float = 0.5f
    ) : HighlightAnimation {
        override fun animateShow(view: View, onFinished: () -> Unit) {
            view.alpha = 0f
            view.scaleX = fromScale
            view.scaleY = fromScale
            view.visibility = View.VISIBLE

            val animSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
                    ObjectAnimator.ofFloat(view, View.SCALE_X, fromScale, 1f),
                    ObjectAnimator.ofFloat(view, View.SCALE_Y, fromScale, 1f)
                )
                this.duration = this@ScaleAnimation.duration
                this.interpolator = this@ScaleAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished()
                    }
                })
            }
            animSet.start()
        }

        override fun animateHide(view: View, onFinished: () -> Unit) {
            val animSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f),
                    ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, fromScale),
                    ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, fromScale)
                )
                this.duration = this@ScaleAnimation.duration
                this.interpolator = this@ScaleAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                        onFinished()
                    }
                })
            }
            animSet.start()
        }
    }

    /**
     * Direction for slide animations
     */
    enum class SlideDirection {
        TOP, BOTTOM, LEFT, RIGHT
    }

    /**
     * Animation that slides the view in/out from a direction
     */
    class SlideAnimation(
        private val direction: SlideDirection,
        private val duration: Long = DEFAULT_DURATION,
        private val interpolator: Interpolator = AccelerateDecelerateInterpolator(),
        private val distance: Float = 100f
    ) : HighlightAnimation {
        override fun animateShow(view: View, onFinished: () -> Unit) {
            view.alpha = 0f
            view.visibility = View.VISIBLE

            // Set initial position based on slide direction
            when (direction) {
                SlideDirection.TOP -> view.translationY = -distance
                SlideDirection.BOTTOM -> view.translationY = distance
                SlideDirection.LEFT -> view.translationX = -distance
                SlideDirection.RIGHT -> view.translationX = distance
            }

            // Create animation based on slide direction
            val positionAnimator = when (direction) {
                SlideDirection.TOP, SlideDirection.BOTTOM -> ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f)
                SlideDirection.LEFT, SlideDirection.RIGHT -> ObjectAnimator.ofFloat(view, View.TRANSLATION_X, 0f)
            }

            val animSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
                    positionAnimator
                )
                this.duration = this@SlideAnimation.duration
                this.interpolator = this@SlideAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        onFinished()
                    }
                })
            }
            animSet.start()
        }

        override fun animateHide(view: View, onFinished: () -> Unit) {
            // Create animation based on slide direction
            val positionAnimator = when (direction) {
                SlideDirection.TOP -> ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, -distance)
                SlideDirection.BOTTOM -> ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, distance)
                SlideDirection.LEFT -> ObjectAnimator.ofFloat(view, View.TRANSLATION_X, -distance)
                SlideDirection.RIGHT -> ObjectAnimator.ofFloat(view, View.TRANSLATION_X, distance)
            }

            val animSet = AnimatorSet().apply {
                playTogether(
                    ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f),
                    positionAnimator
                )
                this.duration = this@SlideAnimation.duration
                this.interpolator = this@SlideAnimation.interpolator
                addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) {
                        view.visibility = View.GONE
                        onFinished()
                    }
                })
            }
            animSet.start()
        }
    }
}
