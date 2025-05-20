package com.dat.dathighlight.shape

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.dat.dathighlight.HighLight.ViewPosInfo
import kotlin.math.max

/**
 * Created by caizepeng on 16/8/20.
 * Edited by isanwenyu@163.com 16/10/26.
 */
class CircleLightShape : BaseLightShape {
    constructor() : super()

    constructor(dx: Float, dy: Float) : super(dx, dy)

    constructor(dx: Float, dy: Float, blurRadius: Float) : super(dx, dy, blurRadius)

    override fun drawShape(bitmap: Bitmap, viewPosInfo: ViewPosInfo) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setDither(true)
        paint.setAntiAlias(true)
        if (blurRadius > 0) {
            paint.setMaskFilter(BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID))
        }
        val rectF = viewPosInfo.rectF
        canvas.drawCircle(
            rectF!!.left + (rectF.width() / 2), rectF.top + (rectF.height() / 2),
            max(rectF.width(), rectF.height()) / 2, paint
        )
    }

    override fun resetRectF4Shape(viewPosInfoRectF: RectF, dx: Float, dy: Float) {
        viewPosInfoRectF.inset(dx, dy)
    }
}
