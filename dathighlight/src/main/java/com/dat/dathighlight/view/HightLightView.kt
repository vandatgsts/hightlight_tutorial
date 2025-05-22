package com.dat.dathighlight.view

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.widget.FrameLayout
import com.dat.dathighlight.HighLight
import com.dat.dathighlight.HighLight.ViewPosInfo
import com.dat.dathighlight.animation.HighlightAnimation
import com.dat.dathighlight.animation.HighlightAnimationHelper

/**
 * Được tạo bởi zhy vào 15/10/8.
 */
@SuppressLint("ViewConstructor")
class HightLightView(
    context: Context,
    private val mHighLight: HighLight?,
    maskColor: Int,
    private val mViewRects: MutableList<ViewPosInfo>,
    isNext: Boolean
) : FrameLayout(context) {
    private var mMaskBitmap: Bitmap? = null
    private var mLightBitmap: Bitmap? = null
    private var mPaint: Paint = Paint().apply {
        isDither = true
        isAntiAlias = true
        style = Paint.Style.FILL
    }
    private val mInflater: LayoutInflater = LayoutInflater.from(context)

    //một số cấu hình
    private var maskColor = -0x34000000

    // thêm bởi isanwenyu@163.com
    val isNext: Boolean //cờ chế độ next
    private var mPosition = -1 //vị trí hiện tại của layout gợi ý đang hiển thị
    private var mViewPosInfo: ViewPosInfo? = null //thông tin vị trí layout được làm nổi bật hiện tại

    // Các thuộc tính animation mới
    private var enterAnimation: HighlightAnimation? = null
    private var exitAnimation: HighlightAnimation? = null
    private var tipViewInflatedListener: ((View) -> Unit)? = null

    init {
        this.maskColor = maskColor
        this.isNext = isNext
        setWillNotDraw(false)

        // Mặc định sử dụng FADE animation
        enterAnimation = HighlightAnimationHelper.createAnimation(
            HighlightAnimationHelper.AnimationType.FADE
        )
        exitAnimation = HighlightAnimationHelper.createAnimation(
            HighlightAnimationHelper.AnimationType.FADE
        )
    }

    /**
     * Set animation to be used when showing tooltips
     */
    fun setEnterAnimation(animation: HighlightAnimation) {
        this.enterAnimation = animation
    }

    /**
     * Set animation to be used when hiding tooltips
     */
    fun setExitAnimation(animation: HighlightAnimation) {
        this.exitAnimation = animation
    }

    /**
     * Set listener to be called when a tip view is inflated
     */
    fun setOnTipViewInflatedListener(listener: (View) -> Unit) {
        this.tipViewInflatedListener = listener
    }

    fun addViewForTip() {
        if (isNext) {
            //kiểm tra mPosition
            if (mPosition < -1 || mPosition > mViewRects.size - 1) {
                //đặt lại vị trí
                mPosition = 0
            } else if (mPosition == mViewRects.size - 1) {
                //xóa layout hiện tại
                mHighLight?.remove()
                return
            } else {
                //mPosition++
                mPosition++
            }
            mViewPosInfo = mViewRects.getOrNull(mPosition)

            // Trước khi xóa views, thực hiện exit animation nếu có views
            val currentView = if (childCount > 0) getChildAt(0) else null
            if (currentView != null && exitAnimation != null) {
                exitAnimation?.animateHide(currentView) {
                    removeAllViews() // Xóa tất cả các layout gợi ý
                    mViewPosInfo?.let { addViewForEveryTip(it) }
                    mHighLight?.sendNextMessage()
                }
            } else {
                removeAllViews() // Xóa tất cả các layout gợi ý
                mViewPosInfo?.let { addViewForEveryTip(it) }
                mHighLight?.sendNextMessage()
            }
        } else {
            for (viewPosInfo in mViewRects) {
                addViewForEveryTip(viewPosInfo)
            }
        }
    }

    /**
     * Thêm từng layout được làm nổi bật
     * @param viewPosInfo thông tin layout được làm nổi bật
     * @author isanwenyu@163.com
     */
    private fun addViewForEveryTip(viewPosInfo: ViewPosInfo) {
        viewPosInfo.marginInfo?.let { marginInfo ->
            val view = mInflater.inflate(viewPosInfo.layoutId, this, false)
            //đặt id thành layout id để HighLight tìm kiếm
            view.id = viewPosInfo.layoutId

            // Thông báo cho listener về view vừa được inflate
            tipViewInflatedListener?.invoke(view)

            val lp = buildTipLayoutParams(view, viewPosInfo) ?: return

            lp.leftMargin = marginInfo.leftMargin.toInt()
            lp.topMargin = marginInfo.topMargin.toInt()
            lp.rightMargin = marginInfo.rightMargin.toInt()
            lp.bottomMargin = marginInfo.bottomMargin.toInt()

            //xác định gravity dựa trên margin
            lp.gravity = if (lp.rightMargin != 0) Gravity.RIGHT else Gravity.LEFT
            lp.gravity = lp.gravity or if (lp.bottomMargin != 0) Gravity.BOTTOM else Gravity.TOP

            // Ban đầu view sẽ được ẩn nếu có animation
            if (enterAnimation != null) {
                view.visibility = View.INVISIBLE
            }

            addView(view, lp)

            // Áp dụng animation nếu có
            if (enterAnimation != null) {
                enterAnimation?.animateShow(view)
            }
        }
    }

    // Phương thức mới để xóa tip hiện tại với animation
    fun removeTipWithAnimation(onRemoved: () -> Unit = {}) {
        if (childCount == 0) {
            onRemoved()
            return
        }

        val views = mutableListOf<View>()
        for (i in 0 until childCount) {
            views.add(getChildAt(i))
        }

        if (exitAnimation != null) {
            var viewsRemaining = views.size

            for (view in views) {
                exitAnimation?.animateHide(view) {
                    viewsRemaining--
                    if (viewsRemaining == 0) {
                        removeAllViews()
                        onRemoved()
                    }
                }
            }
        } else {
            removeAllViews()
            onRemoved()
        }
    }

    private fun buildMask() {
        // Tạo lại bitmap cho mask
        recycleBitmap(mMaskBitmap)
        mMaskBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(mMaskBitmap!!)
        canvas.drawColor(maskColor)
        mPaint.xfermode = MODE_DST_OUT
        mHighLight?.updateInfo()

        // Tạo lại bitmap cho highlight
        recycleBitmap(mLightBitmap)
        mLightBitmap = Bitmap.createBitmap(measuredWidth, measuredHeight, Bitmap.Config.ARGB_8888)

        if (isNext) {
            // Thêm hình dạng nền làm nổi bật cho layout gợi ý hiện tại
            mViewPosInfo?.let { addViewEveryTipShape(it) }
        } else {
            for (viewPosInfo in mViewRects) {
                addViewEveryTipShape(viewPosInfo)
            }
        }
        canvas.drawBitmap(mLightBitmap!!, 0f, 0f, mPaint)
    }

    /**
     * Chủ động thu hồi bitmap đã tạo trước đó
     * @param bitmap bitmap cần thu hồi
     */
    private fun recycleBitmap(bitmap: Bitmap?) {
        if (bitmap != null && !bitmap.isRecycled) {
            bitmap.recycle()
            System.gc()
        }
    }

    /**
     * Thêm hình dạng nền cho layout gợi ý
     * @param viewPosInfo thông tin vị trí của layout gợi ý
     * @author isanwenyu@16.com
     */
    private fun addViewEveryTipShape(viewPosInfo: ViewPosInfo) {
        viewPosInfo.lightShape?.shape(mLightBitmap, viewPosInfo)
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        val width = MeasureSpec.getSize(widthMeasureSpec)
        val height = MeasureSpec.getSize(heightMeasureSpec)

        measureChildren(
            MeasureSpec.makeMeasureSpec(width, MeasureSpec.EXACTLY),
            MeasureSpec.makeMeasureSpec(height, MeasureSpec.EXACTLY)
        )
        setMeasuredDimension(width, height)
    }

    override fun onLayout(changed: Boolean, left: Int, top: Int, right: Int, bottom: Int) {
        super.onLayout(changed, left, top, right, bottom)
        if (changed || isNext) { // Chỉnh sửa bởi isanwenyu@163.com cho chế độ next
            buildMask()
            updateTipPos()
        }
    }

    private fun updateTipPos() {
        if (isNext) { // Nếu là chế độ next chỉ có một control con, làm mới tip tại vị trí hiện tại
            val view = getChildAt(0) ?: return
            mViewPosInfo?.let { viewPosInfo ->
                buildTipLayoutParams(view, viewPosInfo)?.let { lp ->
                    view.layoutParams = lp
                }
            }
        } else {
            for (i in 0 until childCount) {
                val view = getChildAt(i)
                val viewPosInfo = mViewRects.getOrNull(i) ?: continue

                buildTipLayoutParams(view, viewPosInfo)?.let { lp ->
                    view.layoutParams = lp
                }
            }
        }
    }

    private fun buildTipLayoutParams(view: View, viewPosInfo: ViewPosInfo): LayoutParams? {
        val marginInfo = viewPosInfo.marginInfo ?: return null
        val lp = view.layoutParams as? LayoutParams ?: LayoutParams(
            LayoutParams.WRAP_CONTENT,
            LayoutParams.WRAP_CONTENT
        )

        // Kiểm tra xem có cần cập nhật layout params không
        if (lp.leftMargin == marginInfo.leftMargin.toInt() &&
            lp.topMargin == marginInfo.topMargin.toInt() &&
            lp.rightMargin == marginInfo.rightMargin.toInt() &&
            lp.bottomMargin == marginInfo.bottomMargin.toInt()) {
            return null
        }

        lp.leftMargin = marginInfo.leftMargin.toInt()
        lp.topMargin = marginInfo.topMargin.toInt()
        lp.rightMargin = marginInfo.rightMargin.toInt()
        lp.bottomMargin = marginInfo.bottomMargin.toInt()

        // Xác định gravity dựa trên margin
        lp.gravity = if (lp.rightMargin != 0) Gravity.RIGHT else Gravity.LEFT
        lp.gravity = lp.gravity or if (lp.bottomMargin != 0) Gravity.BOTTOM else Gravity.TOP

        return lp
    }

    override fun onDraw(canvas: Canvas) {
        try {
            mMaskBitmap?.let { canvas.drawBitmap(it, 0f, 0f, null) }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        super.onDraw(canvas)
    }

    val curentViewPosInfo: ViewPosInfo?
        /**
         * @return thông tin layout gợi ý được làm nổi bật hiện tại
         */
        get() = mViewPosInfo

    override fun onDetachedFromWindow() {
        super.onDetachedFromWindow()
        // Tối ưu hóa việc thu hồi bitmap
        recycleBitmap(mLightBitmap)
        recycleBitmap(mMaskBitmap)
        mLightBitmap = null
        mMaskBitmap = null
    }

    companion object {
        private const val DEFAULT_WIDTH_BLUR = 15
        private const val DEFAULT_RADIUS = 6
        private val MODE_DST_OUT = PorterDuffXfermode(PorterDuff.Mode.DST_OUT)
    }
}
