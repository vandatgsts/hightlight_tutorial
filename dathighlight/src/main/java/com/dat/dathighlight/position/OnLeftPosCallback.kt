package com.dat.dathighlight.position

import android.graphics.RectF
import com.dat.dathighlight.HighLight.MarginInfo

/**
 * Được tạo bởi caizepeng vào 16/8/20.
 */
class OnLeftPosCallback : OnBaseCallback {
    constructor()

    constructor(offset: Float) : super(offset)

    override fun getPosition(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: MarginInfo?
    ) {
        if (rectF == null || marginInfo == null) return

        marginInfo.rightMargin = rightMargin + rectF.width() + offset
        marginInfo.topMargin = rectF.top
    }
}
