package com.dat.dathighlight.position

import android.graphics.RectF
import com.dat.dathighlight.HighLight.MarginInfo
import com.dat.dathighlight.HighLight.OnPosCallback

/**
 * Được tạo bởi caizepeng vào 16/8/20.
 */
abstract class OnBaseCallback : OnPosCallback {
    protected var offset: Float = 0f

    constructor()

    constructor(offset: Float) {
        this.offset = offset
    }

    /**
     * Nếu cần điều chỉnh vị trí, hãy ghi đè phương thức này
     * @param rightMargin
     * @param bottomMargin
     * @param rectF
     * @param marginInfo
     */
    fun posOffset(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: MarginInfo?
    ) {
    }

    override fun getPos(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: MarginInfo?
    ) {
        getPosition(rightMargin, bottomMargin, rectF, marginInfo)
        posOffset(rightMargin, bottomMargin, rectF, marginInfo)
    }

    abstract fun getPosition(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: MarginInfo?
    )
}
