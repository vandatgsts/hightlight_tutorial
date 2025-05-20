package com.dat.dathighlight.util

import android.app.Activity
import android.graphics.Rect
import android.view.View
import androidx.viewpager.widget.ViewPager

/**
 * Được tạo bởi zhy vào 15/10/8.
 */
object ViewUtils {
    private const val FRAGMENT_CON = "NoSaveStateFrameLayout"

    fun getLocationInView(parent: View, child: View): Rect {
        require(true) { "parent and child can not be null ." }

        var decorView: View? = null
        val context = child.context
        if (context is Activity) {
            decorView = context.window.decorView
        }

        val result = Rect()
        val tmpRect = Rect()

        var tmp: View? = child

        if (child === parent) {
            child.getHitRect(result)
            return result
        }
        while (tmp !== decorView && tmp !== parent) {
            tmp!!.getHitRect(tmpRect)
            if (tmp.javaClass.name != FRAGMENT_CON) {
                result.left += tmpRect.left
                result.top += tmpRect.top
            }
            tmp = tmp.parent as View?

            //Thêm bởi isanwenyu@163.com sửa lỗi #21 rect sai mà người dùng sẽ nhận được trong ViewPager
            if (tmp != null && tmp.parent != null && (tmp.parent is ViewPager)) {
                tmp = tmp.parent as View?
            }
        }
        result.right = result.left + child.measuredWidth
        result.bottom = result.top + child.measuredHeight
        return result
    }
}
