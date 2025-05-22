package com.dat.dathighlight.dsl

import android.app.Activity
import android.content.Context
import android.view.View
import androidx.fragment.app.Fragment
import androidx.lifecycle.LifecycleOwner
import com.dat.dathighlight.HighLight
import com.dat.dathighlight.HighLight.LightShape
import com.dat.dathighlight.HighLight.OnPosCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnClickCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnNextCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnRemoveCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnShowCallback
import com.dat.dathighlight.lifecycle.HighlightLifecycleManager
import com.dat.dathighlight.shape.RectLightShape
import com.dat.dathighlight.view.HightLightView

/**
 * Kotlin DSL for creating and configuring a HighLight instance.
 *
 * Example usage:
 * ```
 * highLight(this) {
 *     maskColor = Color.parseColor("#99000000")
 *     autoRemove = true
 *
 *     addHighlight(binding.btnLight) {
 *         tipLayout = R.layout.tooltip_layout
 *         shape = CircleLightShape()
 *         position = OnBottomPosCallback(10f)
 *     }
 *
 *     onClick {
 *         // Handle click
 *     }
 * }
 * ```
 */
fun highLight(context: Context, init: HighLightBuilder.() -> Unit): HighLight {
    val builder = HighLightBuilder(context)
    builder.init()
    return builder.build()
}

/**
 * Lifecycle-aware version of highLight DSL function.
 * Automatically handles cleanup when the lifecycle owner is destroyed.
 */
fun highLight(lifecycleOwner: LifecycleOwner, context: Context, init: HighLightBuilder.() -> Unit): HighlightLifecycleManager {
    return HighlightLifecycleManager(lifecycleOwner) {
        val builder = HighLightBuilder(context)
        builder.init()
        builder.build()
    }
}

/**
 * Extension function for Activity to create a HighLight with lifecycle awareness
 */
fun Activity.highLight(init: HighLightBuilder.() -> Unit): HighlightLifecycleManager {
    return highLight(this as LifecycleOwner, this, init)
}

/**
 * Extension function for Fragment to create a HighLight with lifecycle awareness
 */
fun Fragment.highLight(init: HighLightBuilder.() -> Unit): HighlightLifecycleManager {
    val fragmentContext = context ?: throw IllegalStateException("Fragment not attached to a context")
    return highLight(this, fragmentContext, init)
}

class HighLightBuilder(private val context: Context) {
    private val highLight: HighLight = HighLight(context)

    /**
     * Whether to intercept click events on the highlight mask
     */
    var intercept: Boolean = true
        set(value) {
            field = value
            highLight.intercept(value)
        }

    /**
     * The color of the mask (background behind the highlight)
     */
    var maskColor: Int = -0x34000000
        set(value) {
            field = value
            highLight.maskColor(value)
        }

    /**
     * Whether to automatically remove the highlight when clicked
     */
    var autoRemove: Boolean = true
        set(value) {
            field = value
            highLight.autoRemove(value)
        }

    /**
     * Whether to enable next mode for multi-step tutorials
     */
    var enableNext: Boolean = false
        set(value) {
            field = value
            if (value) {
                highLight.enableNext()
            }
        }

    /**
     * Add a highlight to a view
     */
    fun addHighlight(view: View, init: HighlightConfig.() -> Unit) {
        val config = HighlightConfig()
        config.init()

        highLight.addHighLight(
            view,
            config.tipLayout,
            config.position,
            config.shape
        )
    }

    /**
     * Add a highlight to a view by ID
     */
    fun addHighlight(viewId: Int, init: HighlightConfig.() -> Unit) {
        val config = HighlightConfig()
        config.init()

        highLight.addHighLight(
            viewId,
            config.tipLayout,
            config.position,
            config.shape
        )
    }

    /**
     * Set a click callback
     */
    fun onClick(callback: () -> Unit) {
        highLight.setClickCallback(object : OnClickCallback {
            override fun onClick() {
                callback()
            }
        })
    }

    /**
     * Set a show callback
     */
    fun onShow(callback: (view: android.view.View?) -> Unit) {
        highLight.setOnShowCallback(object : OnShowCallback {
            override fun onShow(hightLightView: HightLightView?) {
                callback(hightLightView)
            }
        })
    }

    /**
     * Set a remove callback
     */
    fun onRemove(callback: () -> Unit) {
        highLight.setOnRemoveCallback(object : OnRemoveCallback {
            override fun onRemove() {
                callback()
            }
        })
    }

    /**
     * Set a next callback for multi-step tutorials
     */
    fun onNext(callback: (highlightView: android.view.View?, targetView: android.view.View?, tipView: android.view.View?) -> Unit) {
        highLight.setOnNextCallback(object : OnNextCallback {
            override fun onNext(hightLightView: android.view.View?, targetView: android.view.View?, tipView: android.view.View?) {
                callback(hightLightView, targetView, tipView)
            }
        })
    }

    /**
     * Build and return the configured HighLight instance
     */
    fun build(): HighLight {
        return highLight
    }

    /**
     * Show the highlight immediately after building
     */
    fun show(): HighLight {
        return build().show()
    }
}

class HighlightConfig {
    /**
     * Layout resource ID for the tooltip/tip
     */
    var tipLayout: Int = -1

    /**
     * Shape of the highlight (default is RectLightShape)
     */
    var shape: LightShape = RectLightShape()

    /**
     * Position callback for positioning the tooltip relative to the highlighted view
     */
    var position: OnPosCallback? = null
}

/**
 * Simple position enum for easier positioning
 */
enum class Position {
    TOP,
    BOTTOM,
    LEFT,
    RIGHT
}
