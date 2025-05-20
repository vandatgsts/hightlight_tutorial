package com.dat.dathighlight.util

import android.util.Log

/**
 * Created by zhy on 15/9/23.
 */
object L {
    private const val TAG = "HighLight"
    private const val debug = true

    fun e(msg: String) {
        if (debug) Log.e(TAG, msg)
    }
}
