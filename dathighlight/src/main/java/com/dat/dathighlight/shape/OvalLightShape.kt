package com.dat.dathighlight.shape

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.dat.dathighlight.HighLight.ViewPosInfo

/**
 * 椭圆形高亮形状
 * Created by isanwenyu on 16/11/15.
 * Edited by isanwenyu@163.com 16/10/26.
 */
class OvalLightShape : BaseLightShape {
    constructor() : super()

    constructor(dx: Float, dy: Float) : super(dx, dy)

    constructor(dx: Float, dy: Float, blurRadius: Float) : super(dx, dy, blurRadius)

    override fun drawShape(bitmap: Bitmap, viewPosInfo: ViewPosInfo) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.isDither = true
        paint.isAntiAlias = true
        if (blurRadius > 0) {
            paint.setMaskFilter(BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID))
        }
        val rectF = viewPosInfo.rectF
        canvas.drawOval(rectF!!, paint)
    }

    override fun resetRectF4Shape(viewPosInfoRectF: RectF, dx: Float, dy: Float) {
        //默认根据dx dy横向和竖向缩小RectF范围
        viewPosInfoRectF.inset(dx, dy)
    }
}
