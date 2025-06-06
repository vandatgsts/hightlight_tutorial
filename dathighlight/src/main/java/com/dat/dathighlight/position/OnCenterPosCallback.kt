package com.dat.dathighlight.position

import android.graphics.RectF
import com.dat.dathighlight.HighLight

class OnCenterPosCallback : OnBaseCallback {
    constructor()

    constructor(offset: Float) : super(offset)

    override fun getPosition(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: HighLight.MarginInfo?,
    ) {
        if (rectF == null || marginInfo == null) return

//        marginInfo.leftMargin = rectF.right - rectF.width() / 2
        marginInfo.leftMargin = 0f
        marginInfo.topMargin = rectF.top + offset
    }
}
