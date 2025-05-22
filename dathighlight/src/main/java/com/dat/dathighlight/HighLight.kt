package com.dat.dathighlight

import android.app.Activity
import android.content.Context
import android.graphics.Bitmap
import android.graphics.RectF
import android.os.Build
import android.os.Handler
import android.os.Message
import android.view.View
import android.view.ViewGroup
import android.view.ViewTreeObserver.OnGlobalLayoutListener
import android.widget.FrameLayout
import android.widget.RelativeLayout
import com.dat.dathighlight.animation.HighlightAnimation
import com.dat.dathighlight.animation.HighlightAnimationHelper
import com.dat.dathighlight.interfaces.HighLightInterface
import com.dat.dathighlight.interfaces.HighLightInterface.OnClickCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnLayoutCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnNextCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnRemoveCallback
import com.dat.dathighlight.interfaces.HighLightInterface.OnShowCallback
import com.dat.dathighlight.shape.RectLightShape
import com.dat.dathighlight.util.ViewUtils
import com.dat.dathighlight.view.HightLightView
import java.lang.ref.WeakReference

/**
 * Được tạo bởi zhy vào 15/10/8.
 * Cải tiến với Kotlin APIs hiện đại và tính năng mới.
 */
class HighLight(private val mContext: Context) : HighLightInterface, OnGlobalLayoutListener {
    class ViewPosInfo {
        @JvmField
        var layoutId: Int = -1
        @JvmField
        var rectF: RectF? = null
        @JvmField
        var marginInfo: MarginInfo? = null
        var view: View? = null
        var onPosCallback: OnPosCallback? = null
        @JvmField
        var lightShape: LightShape? = null
    }

    interface LightShape {
        fun shape(bitmap: Bitmap?, viewPosInfo: ViewPosInfo?)
    }

    class MarginInfo {
        @JvmField
        var topMargin: Float = 0f
        @JvmField
        var leftMargin: Float = 0f
        @JvmField
        var rightMargin: Float = 0f
        @JvmField
        var bottomMargin: Float = 0f
    }

    interface OnPosCallback {
        fun getPos(rightMargin: Float, bottomMargin: Float, rectF: RectF?, marginInfo: MarginInfo?)
    }

    private var mAnchor: View
    private val mViewRects: MutableList<ViewPosInfo> = ArrayList()
    private var mHightLightView: HightLightView? = null

    private var intercept = true
    private var maskColor = -0x34000000

    // Animation properties
    private var enterAnimation: HighlightAnimation? = null
    private var exitAnimation: HighlightAnimation? = null

    // Lambda callbacks for modern Kotlin style
    private var onClickListener: (() -> Unit)? = null
    private var onShowListener: ((HightLightView) -> Unit)? = null
    private var onRemoveListener: (() -> Unit)? = null
    private var onNextListener: ((HightLightView?, View?, View?) -> Unit)? = null
    private var onLayoutListener: (() -> Unit)? = null
    private var onTipViewInflatedListener: ((View) -> Unit)? = null

    // Thêm bởi isanwenyu@163.com
    private var autoRemove = true // điểm gọi là tự động loại bỏ, mặc định là true

    /**
     * Trả về có phải là chế độ next
     *
     * @return true nếu đang ở chế độ next
     * @author isanwenyu@163.com
     */
    var isNext: Boolean = false // cờ chế độ next, mặc định là false
        private set

    /**
     * @return Cho biết dialog có đang hiển thị hay không
     * @author isanwenyu@163.com
     */
    var isShowing: Boolean = false // Đang hiển thị hay không
        private set

    private var mShowMessage: Message? = null
    private var mRemoveMessage: Message? = null
    private var mClickMessage: Message? = null
    private var mNextMessage: Message? = null
    private var mLayoutMessage: Message? = null
    private val mListenersHandler: ListenersHandler

    init {
        mAnchor = (mContext as? Activity)?.findViewById(android.R.id.content)
            ?: throw IllegalArgumentException("Context phải là một Activity")
        mListenersHandler = ListenersHandler(this)
        registerGlobalLayoutListener()
    }

    fun anchor(anchor: View): HighLight {
        mAnchor = anchor
        registerGlobalLayoutListener()
        return this
    }

    override fun getAnchor(): View {
        return mAnchor
    }

    fun intercept(intercept: Boolean): HighLight {
        this.intercept = intercept
        return this
    }

    fun maskColor(maskColor: Int): HighLight {
        this.maskColor = maskColor
        return this
    }

    /**
     * Set animation used when showing tooltips
     */
    fun setEnterAnimation(animation: HighlightAnimation): HighLight {
        enterAnimation = animation
        return this
    }

    /**
     * Set animation used when hiding tooltips
     */
    fun setExitAnimation(animation: HighlightAnimation): HighLight {
        exitAnimation = animation
        return this
    }

    /**
     * Set animation from predefined animation type
     */
    fun setEnterAnimation(
        animationType: HighlightAnimationHelper.AnimationType,
        duration: Long = 300
    ): HighLight {
        enterAnimation = HighlightAnimationHelper.createAnimation(animationType, duration)
        return this
    }

    /**
     * Set exit animation from predefined animation type
     */
    fun setExitAnimation(
        animationType: HighlightAnimationHelper.AnimationType,
        duration: Long = 300
    ): HighLight {
        exitAnimation = HighlightAnimationHelper.createAnimation(animationType, duration)
        return this
    }

    /**
     * Set a listener to be called when a tip view is inflated
     */
    fun setOnTipViewInflatedListener(listener: (View) -> Unit): HighLight {
        onTipViewInflatedListener = listener
        return this
    }

    fun addHighLight(
        viewId: Int,
        decorLayoutId: Int,
        onPosCallback: OnPosCallback,
        lightShape: LightShape?
    ): HighLight {
        val parent = mAnchor as? ViewGroup
            ?: throw IllegalStateException("Anchor không phải là ViewGroup")
        val view = parent.findViewById<View>(viewId)
            ?: throw IllegalArgumentException("Không tìm thấy view với ID $viewId")
        addHighLight(view, decorLayoutId, onPosCallback, lightShape)
        return this
    }

    fun updateInfo() {
        val parent = mAnchor as? ViewGroup ?: return
        for (viewPosInfo in mViewRects) {
            val view = viewPosInfo.view ?: continue
            val rect = RectF(ViewUtils.getLocationInView(parent, view))

            // Cập nhật thông tin vị trí
            viewPosInfo.rectF = rect
            viewPosInfo.onPosCallback?.getPos(
                parent.width - rect.right,
                parent.height - rect.bottom,
                rect,
                viewPosInfo.marginInfo
            )
        }
    }

    fun addHighLight(
        view: View?,
        decorLayoutId: Int,
        onPosCallback: OnPosCallback?,
        lightShape: LightShape?
    ): HighLight {
        if (view == null) {
            return this
        }

        require(!(onPosCallback == null && decorLayoutId != -1)) { "onPosCallback không thể là null." }

        val parent = mAnchor as? ViewGroup ?: return this
        val rect = RectF(ViewUtils.getLocationInView(parent, view))

        // Nếu RectF rỗng thì return - được thêm bởi isanwenyu 2016/10/26
        if (rect.isEmpty) return this

        val viewPosInfo = ViewPosInfo().apply {
            this.layoutId = decorLayoutId
            this.rectF = rect
            this.view = view
            this.onPosCallback = onPosCallback
            this.lightShape = lightShape ?: RectLightShape()

            val marginInfo = MarginInfo()
            onPosCallback?.getPos(
                parent.width - rect.right,
                parent.height - rect.bottom,
                rect,
                marginInfo
            )
            this.marginInfo = marginInfo
        }

        mViewRects.add(viewPosInfo)
        return this
    }

    // Một cảnh có thể có nhiều bước làm nổi bật. Một bước hoàn thành sau đó mới bước sang bước tiếp theo
    // Legacy callback methods for backwards compatibility
    fun setClickCallback(clickCallback: OnClickCallback?): HighLight {
        mClickMessage = clickCallback?.let { mListenersHandler.obtainMessage(CLICK, it) }
        return this
    }

    fun setOnShowCallback(onShowCallback: OnShowCallback?): HighLight {
        mShowMessage = onShowCallback?.let { mListenersHandler.obtainMessage(SHOW, it) }
        return this
    }

    fun setOnRemoveCallback(onRemoveCallback: OnRemoveCallback?): HighLight {
        mRemoveMessage = onRemoveCallback?.let { mListenersHandler.obtainMessage(REMOVE, it) }
        return this
    }

    fun setOnNextCallback(onNextCallback: OnNextCallback?): HighLight {
        mNextMessage = onNextCallback?.let { mListenersHandler.obtainMessage(NEXT, it) }
        return this
    }

    fun setOnLayoutCallback(onLayoutCallback: OnLayoutCallback?): HighLight {
        mLayoutMessage = onLayoutCallback?.let { mListenersHandler.obtainMessage(LAYOUT, it) }
        return this
    }

    // Modern lambda-based callback methods
    fun setOnClickListener(listener: () -> Unit): HighLight {
        onClickListener = listener
        return this
    }

    fun setOnShowListener(listener: (HightLightView) -> Unit): HighLight {
        onShowListener = listener
        return this
    }

    fun setOnRemoveListener(listener: () -> Unit): HighLight {
        onRemoveListener = listener
        return this
    }

    fun setOnNextListener(listener: (HightLightView?, View?, View?) -> Unit): HighLight {
        onNextListener = listener
        return this
    }

    fun setOnLayoutListener(listener: () -> Unit): HighLight {
        onLayoutListener = listener
        return this
    }

    /**
     * Có tự động loại bỏ khi click hay không
     *
     * @return chuỗi giao diện trả về chính nó
     * @author isanwenyu@163.com
     * @see .show
     * @see .remove
     */
    fun autoRemove(autoRemove: Boolean): HighLight {
        this.autoRemove = autoRemove
        return this
    }

    /**
     * Lấy bố cục làm nổi bật, nếu muốn lấy decorLayout nên gọi sau [.show]
     *
     * ID bố cục làm nổi bật trong [.show] là hightLightView.setId(R.id.high_light_view)
     *
     * @return trả về đối tượng bố cục làm nổi bật với id R.id.high_light_view
     * @author isanwenyu@163.com
     * @see .show
     */
    override fun getHightLightView(): HightLightView? {
        return mHightLightView ?: (mContext as? Activity)?.findViewById<HightLightView>(R.id.high_light_view)?.also {
            mHightLightView = it
        }
    }

    /**
     * Bật chế độ next
     *
     * @return chuỗi giao diện trả về chính nó
     * @author isanwenyu@163.com
     * @see .show
     */
    fun enableNext(): HighLight {
        this.isNext = true
        return this
    }

    /**
     * Chuyển đến bố cục gợi ý tiếp theo
     *
     * @return Đối tượng HighLight
     * @author isanwenyu@163.com
     */
    override fun next(): HighLight {
        getHightLightView()?.addViewForTip()
            ?: throw NullPointerException("HightLightView là null, bạn phải gọi show() trước!")
        return this
    }

    override fun show(): HighLight {
        // Nếu view đã tồn tại, sử dụng lại
        getHightLightView()?.let {
            mHightLightView = it
            // Đặt lại thuộc tính của đối tượng HighLight hiện tại
            this.isShowing = true
            isNext = it.isNext
            return this
        }

        // Nếu danh sách ViewRects trống, trả về
        if (mViewRects.isEmpty()) return this

        val hightLightView = HightLightView(mContext, this, maskColor, mViewRects, isNext).apply {
            // Thêm ID duy nhất cho view làm nổi bật bởi isanwenyu@163.com vào 2016/9/28
            id = R.id.high_light_view

            // Set animations if available
            enterAnimation?.let { setEnterAnimation(it) }
            exitAnimation?.let { setExitAnimation(it) }

            // Set tip view inflated listener
            onTipViewInflatedListener?.let { setOnTipViewInflatedListener(it) }
        }

        // Tương thích với AutoFrameLayout, v.v.
        when (mAnchor) {
            is FrameLayout -> {
                val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                (mAnchor as ViewGroup).addView(
                    hightLightView,
                    (mAnchor as ViewGroup).childCount,
                    lp
                )
            }
            else -> {
                val frameLayout = FrameLayout(mContext)
                val parent = mAnchor.parent as? ViewGroup
                    ?: throw IllegalStateException("Anchor không có parent")

                parent.removeView(mAnchor)
                parent.addView(frameLayout, mAnchor.layoutParams)

                val lp = ViewGroup.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.MATCH_PARENT
                )
                frameLayout.addView(mAnchor, lp)
                frameLayout.addView(hightLightView)
            }
        }

        if (intercept) {
            hightLightView.setOnClickListener {
                // Thêm autoRemove bởi isanwenyu@163.com
                if (autoRemove) remove()
                sendClickMessage()
                onClickListener?.invoke() // Call lambda if set
            }
        }

        // Thêm bố cục gợi ý
        hightLightView.addViewForTip()
        mHightLightView = hightLightView
        this.isShowing = true

        // Gửi callback hiển thị
        sendShowMessage()
        onShowListener?.invoke(hightLightView) // Call lambda if set
        return this
    }

    override fun remove(): HighLight {
        val hightLightView = getHightLightView() ?: return this

        // Sử dụng animation khi xóa nếu có
        if (exitAnimation != null) {
            hightLightView.removeTipWithAnimation {
                performRemove(hightLightView)
            }
        } else {
            performRemove(hightLightView)
        }

        return this
    }

    private fun performRemove(hightLightView: HightLightView) {
        val parent = hightLightView.parent as? ViewGroup ?: return

        when (parent) {
            is RelativeLayout, is FrameLayout -> {
                parent.removeView(hightLightView)
            }
            else -> {
                parent.removeView(hightLightView)
                val origin = parent.getChildAt(0)
                val grandParent = parent.parent as? ViewGroup
                    ?: throw IllegalStateException("Parent không có parent")
                grandParent.removeView(parent)
                grandParent.addView(origin, parent.layoutParams)
            }
        }

        mHightLightView = null
        sendRemoveMessage()
        onRemoveListener?.invoke() // Call lambda if set
        this.isShowing = false
    }

    private fun sendClickMessage() {
        mClickMessage?.let {
            Message.obtain(it).sendToTarget()
        }
    }

    private fun sendRemoveMessage() {
        mRemoveMessage?.let {
            Message.obtain(it).sendToTarget()
        }
    }

    private fun sendShowMessage() {
        mShowMessage?.let {
            Message.obtain(it).sendToTarget()
        }
    }

    private fun sendLayoutMessage() {
        mLayoutMessage?.let {
            Message.obtain(it).sendToTarget()
        }
        onLayoutListener?.invoke() // Call lambda if set
    }

    fun sendNextMessage() {
        check(isNext) { "Chỉ cho chế độ isNext, vui lòng gọi enableNext() trước" }

        val hightLightView = getHightLightView() ?: return

        val viewPosInfo = hightLightView.curentViewPosInfo ?: return

        // Call legacy callback
        mNextMessage?.let { message ->
            message.arg1 = viewPosInfo.view?.id ?: -1
            message.arg2 = viewPosInfo.layoutId
            Message.obtain(message).sendToTarget()
        }

        // Call modern lambda callback
        onNextListener?.invoke(
            hightLightView,
            viewPosInfo.view,
            hightLightView.findViewById(viewPosInfo.layoutId)
        )
    }

    /**
     * Đăng ký trình nghe bố cục toàn cục cho mAnchor
     */
    private fun registerGlobalLayoutListener() {
        mAnchor.viewTreeObserver.addOnGlobalLayoutListener(this)
    }

    /**
     * Hủy đăng ký trình nghe bố cục toàn cục từ mAnchor
     */
    private fun unRegisterGlobalLayoutListener() {
        mAnchor.viewTreeObserver.removeOnGlobalLayoutListener(this)
    }

    override fun onGlobalLayout() {
        unRegisterGlobalLayoutListener()
        sendLayoutMessage()
    }

    /**
     * @see android.app.Dialog.ListenersHandler
     */
    private class ListenersHandler(highLight: HighLight?) : Handler() {
        private val mHighLightInterface: WeakReference<HighLightInterface?> = WeakReference(highLight)

        override fun handleMessage(msg: Message) {
            val highLightInterface = mHighLightInterface.get() ?: return
            val hightLightView = highLightInterface.getHightLightView()
            val anchorView = highLightInterface.getAnchor()

            when (msg.what) {
                CLICK -> (msg.obj as OnClickCallback).onClick()
                REMOVE -> (msg.obj as OnRemoveCallback).onRemove()
                SHOW -> (msg.obj as OnShowCallback).onShow(hightLightView)
                NEXT -> {
                    val targetView = if (msg.arg1 != -1) anchorView?.findViewById<View>(msg.arg1) else null
                    val tipView = hightLightView?.findViewById<View>(msg.arg2)
                    (msg.obj as OnNextCallback).onNext(hightLightView, targetView, tipView)
                }
                LAYOUT -> (msg.obj as OnLayoutCallback).onLayouted()
            }
        }
    }

    companion object {
        private const val CLICK = 0x40
        private const val REMOVE = 0x41
        private const val SHOW = 0x42
        private const val NEXT = 0x43
        private const val LAYOUT = 0x44
    }
}

