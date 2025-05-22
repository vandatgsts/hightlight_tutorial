# Dathighlight Library

Dathighlight là một thư viện Android mạnh mẽ giúp tạo các hướng dẫn trực quan, làm nổi bật các thành phần UI và hiển thị các tooltip. Thư viện được thiết kế để dễ sử dụng, linh hoạt và hỗ trợ đầy đủ lifecycle của ứng dụng Android.

## Tính năng

- 🔦 **Làm nổi bật các thành phần UI** với nhiều hình dạng (hình chữ nhật, hình tròn, hình bầu dục)
- 🎯 **Tooltips tùy chỉnh** được định vị chính xác xung quanh các phần tử được làm nổi bật
- ✨ **Hiệu ứng animation** khi hiển thị và ẩn tooltips
- 🔄 **Hướng dẫn nhiều bước** với quản lý tour đầy đủ
- 🛡️ **Quản lý lifecycle an toàn** giúp tránh memory leak
- 🔧 **API Kotlin hiện đại** với DSL và lambda callbacks

## Cài đặt

Thêm vào file `settings.gradle` của dự án:

```groovy
repositories {
    mavenCentral()
    // Nếu sử dụng jitpack
    maven { url 'https://jitpack.io' }
}
```

Sau đó thêm vào file `build.gradle` của module:

```groovy
dependencies {
    implementation 'com.github.yourusername:dathighlight:1.0.0'
}
```

## Sử dụng cơ bản

### 1. Highlight đơn giản với DSL

```kotlin
// Cách 1: DSL có kiểm soát lifecycle
highLight(this) {
    maskColor = Color.parseColor("#99000000") // Màu nền (màu tối)
    
    // Thêm highlight cho một view
    addHighlight(binding.btnLight) {
        tipLayout = R.layout.tip_layout // Layout của tooltip
        shape = RectLightShape() // Hình dạng làm nổi bật (RectLightShape, CircleLightShape, OvalLightShape)
    }
    
    // Sử dụng lambda callback hiện đại
    onClick { 
        Toast.makeText(this@MainActivity, "Highlight clicked!", Toast.LENGTH_SHORT).show()
    }
}

// Cách 2: Builder pattern truyền thống
val highLight = HighLight(this)
    .anchor(binding.root) // View gốc (mặc định là content của Activity)
    .autoRemove(true) // Tự động xóa khi click
    .maskColor(Color.parseColor("#99000000"))
    .addHighLight(
        binding.btnLight,
        R.layout.tip_layout,
        OnBottomPosCallback(10f), // Vị trí tooltip (OnTopPosCallback, OnLeftPosCallback, OnRightPosCallback)
        RectLightShape()
    )
    .setOnClickListener {
        Toast.makeText(this, "Highlight clicked!", Toast.LENGTH_SHORT).show()
    }
    .show()
```

### 2. Tùy chỉnh vị trí tooltip

```kotlin
// Sử dụng position callbacks có sẵn
highLight(this) {
    addHighlight(binding.btnLight) {
        tipLayout = R.layout.tip_layout
        shape = CircleLightShape()
        position = OnTopPosCallback(15f) // Tooltip ở trên, cách 15px
    }
    
    addHighlight(binding.btnRightLight) {
        tipLayout = R.layout.another_tip_layout
        shape = OvalLightShape()
        position = OnLeftPosCallback(10f) // Tooltip ở bên trái, cách 10px
    }
}
```

### 3. Tùy chỉnh tooltip với animation

```kotlin
highLight(this) {
    // Animation khi hiển thị tooltip
    setEnterAnimation(AnimationType.SCALE, duration = 300)
    
    // Animation khi ẩn tooltip
    setExitAnimation(AnimationType.FADE, duration = 200)
    
    // Tùy chỉnh tooltip sau khi được tạo
    setOnTipViewInflatedListener { tipView ->
        val btnNext = tipView.findViewById<Button>(R.id.btn_next)
        btnNext.setOnClickListener {
            // Xử lý khi người dùng bấm nút trong tooltip
        }
        
        // Tùy chỉnh giao diện tooltip
        val tvTitle = tipView.findViewById<TextView>(R.id.tv_title)
        tvTitle.text = "Tiêu đề tùy chỉnh!"
    }
    
    addHighlight(binding.btnLight) {
        tipLayout = R.layout.tip_layout
        shape = RectLightShape()
    }
}
```

### 4. Tạo hướng dẫn nhiều bước (Tour)

```kotlin
// Cách 1: API đơn giản với Tour Manager
val tour = HighlightTour(this)
    .addStep(binding.btnFirst, R.layout.tip_step1, OnTopPosCallback())
    .addStep(binding.btnSecond, R.layout.tip_step2, OnRightPosCallback())
    .addStep(binding.btnThird, R.layout.tip_step3, OnBottomPosCallback())
    .setOnStepChangedListener { index, step ->
        Log.d("Tour", "Đang hiển thị bước $index")
    }
    .setOnTourFinishedListener {
        Toast.makeText(this, "Tour đã hoàn thành!", Toast.LENGTH_SHORT).show()
    }
    .withLifecycle(this) // Tự động quản lý lifecycle
    .start()

// Điều hướng tour
btnNext.setOnClickListener { tour.next() }
btnPrevious.setOnClickListener { tour.previous() }
btnFinish.setOnClickListener { tour.finish() }

// Cách 2: Sử dụng API có sẵn cũ với next()
val highLight = HighLight(this)
    .enableNext() // Bật chế độ next
    .autoRemove(false) // Không tự động xóa khi click
    .maskColor(Color.parseColor("#99000000"))
    .addHighLight(binding.btnLight, R.layout.tip_first, OnBottomPosCallback(10f), RectLightShape())
    .addHighLight(binding.btnRightLight, R.layout.tip_second, OnLeftPosCallback(10f), CircleLightShape())
    .addHighLight(binding.btnBottomLight, R.layout.tip_third, OnTopPosCallback(10f), RectLightShape())
    .setOnNextListener { _, _, _ ->
        // Xử lý khi chuyển sang bước tiếp theo
    }
    .show()

// Chuyển sang bước tiếp theo khi cần
btnNext.setOnClickListener { highLight.next() }
```

## Tùy chỉnh nâng cao

### Tạo hình dạng highlight tùy chỉnh

```kotlin
class CustomShape : BaseLightShape() {
    override fun resetRectF4Shape(viewPosInfoRectF: RectF, dx: Float, dy: Float) {
        viewPosInfoRectF.inset(dx, dy)
    }
    
    override fun drawShape(bitmap: Bitmap, viewPosInfo: ViewPosInfo) {
        val canvas = Canvas(bitmap)
        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
        paint.isDither = true
        paint.isAntiAlias = true
        if (blurRadius > 0) {
            paint.setMaskFilter(BlurMaskFilter(blurRadius, BlurMaskFilter.Blur.SOLID))
        }
        
        // Vẽ hình dạng tùy chỉnh của bạn
        // Ví dụ: vẽ hình tam giác
        val path = Path()
        val rectF = viewPosInfo.rectF!!
        path.moveTo(rectF.left, rectF.bottom)
        path.lineTo(rectF.right, rectF.bottom)
        path.lineTo(rectF.centerX(), rectF.top)
        path.close()
        
        canvas.drawPath(path, paint)
    }
}
```

### Tạo animation tùy chỉnh

```kotlin
class CustomAnimation : HighlightAnimation {
    override fun animateShow(view: View, onFinished: () -> Unit) {
        // Hiệu ứng hiển thị tùy chỉnh
        view.alpha = 0f
        view.scaleX = 1.5f
        view.scaleY = 0.5f
        view.visibility = View.VISIBLE
        
        val animSet = AnimatorSet()
        animSet.playTogether(
            ObjectAnimator.ofFloat(view, View.ALPHA, 0f, 1f),
            ObjectAnimator.ofFloat(view, View.SCALE_X, 1.5f, 1f),
            ObjectAnimator.ofFloat(view, View.SCALE_Y, 0.5f, 1f)
        )
        animSet.duration = 500
        animSet.interpolator = OvershootInterpolator(1.5f)
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                onFinished()
            }
        })
        animSet.start()
    }
    
    override fun animateHide(view: View, onFinished: () -> Unit) {
        // Hiệu ứng ẩn tùy chỉnh
        val animSet = AnimatorSet()
        animSet.playTogether(
            ObjectAnimator.ofFloat(view, View.ALPHA, 1f, 0f),
            ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 0.5f),
            ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 0.5f),
            ObjectAnimator.ofFloat(view, View.TRANSLATION_Y, 0f, -200f)
        )
        animSet.duration = 500
        animSet.interpolator = AnticipateInterpolator(1.5f)
        animSet.addListener(object : AnimatorListenerAdapter() {
            override fun onAnimationEnd(animation: Animator) {
                view.visibility = View.GONE
                onFinished()
            }
        })
        animSet.start()
    }
}

// Sử dụng
highLight(this) {
    setEnterAnimation(CustomAnimation())
    // ...
}
```

### Tùy chỉnh vị trí tooltip

```kotlin
// Tạo vị trí tùy chỉnh
class CustomPosCallback : OnBaseCallback(10f) {
    override fun getPosition(
        rightMargin: Float, 
        bottomMargin: Float, 
        rectF: RectF?, 
        marginInfo: MarginInfo?
    ) {
        if (rectF == null || marginInfo == null) return
        
        // Đặt tooltip ở giữa trên của view
        marginInfo.leftMargin = rectF.left + (rectF.width() / 2) - 100 // Giả sử tooltip rộng 200px
        marginInfo.bottomMargin = bottomMargin + rectF.height() + offset
    }
    
    override fun posOffset(
        rightMargin: Float,
        bottomMargin: Float,
        rectF: RectF?,
        marginInfo: MarginInfo?
    ) {
        // Điều chỉnh thêm vị trí nếu cần
        marginInfo?.leftMargin = (marginInfo?.leftMargin ?: 0f) + 20f // Thêm 20px về phía bên phải
    }
}

// Sử dụng
highLight(this) {
    addHighlight(binding.btnLight) {
        tipLayout = R.layout.tip_layout
        position = CustomPosCallback()
    }
}
```

## Làm việc với Fragment, RecyclerView và ViewPager

### Fragment

```kotlin
// Trong Fragment
override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    
    // Sử dụng DSL với lifecycle của Fragment
    highLight(viewLifecycleOwner, requireContext()) {
        addHighlight(binding.btnInFragment) {
            tipLayout = R.layout.tip_layout
        }
    }
    
    // Hoặc sử dụng Fragment extension
    highLight {
        addHighlight(binding.btnInFragment) {
            tipLayout = R.layout.tip_layout
        }
    }
}
```

### RecyclerView

```kotlin
// Highlight một item trong RecyclerView
val layoutManager = recyclerView.layoutManager as LinearLayoutManager
val targetPosition = 3 // Vị trí item cần highlight
val targetView = layoutManager.findViewByPosition(targetPosition)

if (targetView != null) {
    highLight(this) {
        addHighlight(targetView) {
            tipLayout = R.layout.tip_recycler_item
            shape = RectLightShape()
        }
    }
} else {
    // Đảm bảo item hiển thị trước khi highlight
    recyclerView.scrollToPosition(targetPosition)
    recyclerView.post {
        val view = layoutManager.findViewByPosition(targetPosition)
        if (view != null) {
            highLight(this) {
                addHighlight(view) {
                    tipLayout = R.layout.tip_recycler_item
                    shape = RectLightShape()
                }
            }
        }
    }
}
```

### ViewPager2

```kotlin
// Highlight một phần tử trong trang ViewPager2 hiện tại
viewPager2.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
    override fun onPageSelected(position: Int) {
        super.onPageSelected(position)
        
        // Đợi trang được render hoàn toàn
        viewPager2.post {
            val currentView = (viewPager2.getChildAt(0) as RecyclerView)
                .findViewHolderForAdapterPosition(position)?.itemView
                
            if (currentView != null) {
                // Tìm view cụ thể trong trang hiện tại
                val targetView = currentView.findViewById<View>(R.id.btn_in_page)
                
                if (targetView != null) {
                    // Hiển thị highlight
                    highLight(this@MainActivity) {
                        addHighlight(targetView) {
                            tipLayout = R.layout.tip_viewpager_item
                            shape = CircleLightShape()
                        }
                    }
                }
            }
        }
    }
})
```

## License

```
MIT License

Copyright (c) 2023 Your Name

Permission is hereby granted, free of charge, to any person obtaining a copy
of this software and associated documentation files (the "Software"), to deal
in the Software without restriction, including without limitation the rights
to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
copies of the Software, and to permit persons to whom the Software is
furnished to do so, subject to the following conditions:

The above copyright notice and this permission notice shall be included in all
copies or substantial portions of the Software.

THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
SOFTWARE.
```
