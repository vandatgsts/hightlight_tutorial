package com.dat.dathighlight.shape

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import com.dat.dathighlight.HighLight.ViewPosInfo

/**
 * Được tạo bởi caizepeng vào 16/8/20.
 * Chỉnh sửa bởi isanwenyu@163.com 16/10/26.
 */
class RectLightShape : BaseLightShape {
    private var rx = 6f //Bán kính x của hình bầu dục được sử dụng để làm tròn các góc
    private var ry = 6f //Bán kính y của hình bầu dục được sử dụng để làm tròn các góc

    constructor() : super()

    /**
     * @param dx Độ lệch theo phương ngang
     * @param dy Độ lệch theo phương dọc
     * @param blurRadius Bán kính làm mờ, mặc định 15px, 0 không làm mờ
     * @param rx Bán kính x của hình bầu dục được sử dụng để làm tròn các góc, mặc định 6px.
     * @param ry Bán kính y của hình bầu dục được sử dụng để làm tròn các góc, mặc định 6px.
     */
    constructor(dx: Float, dy: Float, blurRadius: Float, rx: Float, ry: Float) : super(
        dx,
        dy,
        blurRadius
    ) {
        this.rx = rx
        this.ry = ry
    }

    constructor(dx: Float, dy: Float, blurRadius: Float) : super(dx, dy, blurRadius)

    constructor(dx: Float, dy: Float) : super(dx, dy)

    override fun drawShape(bitmap: Bitmap, viewPosInfo: ViewPosInfo) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.setDither(true)
        paint.setAntiAlias(true)
        if (blurRadius > 0) {
            paint.setMaskFilter(BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID))
        }
        canvas.drawRoundRect(viewPosInfo.rectF!!, rx, ry, paint)
    }

    override fun resetRectF4Shape(viewPosInfoRectF: RectF, dx: Float, dy: Float) {
        viewPosInfoRectF.inset(dx, dy)
    }
}
