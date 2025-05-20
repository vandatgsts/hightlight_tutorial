package com.dat.tutorial

import android.graphics.Bitmap
import android.graphics.BlurMaskFilter
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.RectF
import android.os.Bundle
import android.util.TypedValue
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.dat.dathighlight.HighLight
import com.dat.dathighlight.HighLight.ViewPosInfo
import com.dat.dathighlight.position.OnBottomPosCallback
import com.dat.dathighlight.position.OnLeftPosCallback
import com.dat.dathighlight.position.OnRightPosCallback
import com.dat.dathighlight.position.OnTopPosCallback
import com.dat.dathighlight.shape.BaseLightShape
import com.dat.dathighlight.shape.CircleLightShape
import com.dat.dathighlight.shape.OvalLightShape
import com.dat.dathighlight.shape.RectLightShape

class MainActivity : AppCompatActivity() {
    private var mHightLight: HighLight? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)


        //        findViewById(R.id.id_btn_amazing).post(
//                new Runnable()
//                {
//                    @Override
//                    public void run()
//                    {
//                        showTipMask();
//                    }
//                }
//
//        );
        showNextTipViewOnCreated()
    }

    override fun onWindowFocusChanged(hasFocus: Boolean) {
        super.onWindowFocusChanged(hasFocus)
        // Hiển thị trực tiếp bố cục làm nổi bật sau khi giao diện được khởi tạo
//        if(hasFocus) mHightLight.show();
    }

    fun showTipView(view: View?) {
        mHightLight = HighLight(this@MainActivity) //
            .anchor(findViewById(R.id.id_container)) // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
            .addHighLight(
                R.id.btn_rightLight,
                R.layout.info_gravity_left_down,
                OnLeftPosCallback(45f),
                RectLightShape()
            )
            .addHighLight(
                R.id.btn_light,
                R.layout.info_gravity_left_down,
                OnRightPosCallback(5f),
                CircleLightShape()
            )
            .addHighLight(
                R.id.btn_bottomLight,
                R.layout.info_gravity_left_down,
                OnTopPosCallback(),
                CircleLightShape()
            )
            .addHighLight(
                view,
                R.layout.info_gravity_left_down,
                OnBottomPosCallback(60f),
                CircleLightShape()
            )
        mHightLight!!.show()
    }

    /**
     * Hiển thị bố cục gợi ý ở chế độ next khi giao diện hoàn thành
     * Phương thức hiển thị phải được gọi trong onLayouted
     * Áp dụng cho Activity và Fragment
     * Có thể gọi trực tiếp trong phương thức onCreated
     * @author isanwenyu@163.com
     */
    fun showNextTipViewOnCreated() {
        mHightLight = HighLight(this@MainActivity) //
            .anchor(findViewById(R.id.id_container)) // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
            .autoRemove(false)
            .enableNext()
            .setOnLayoutCallback { // Thêm tipview sau khi giao diện được hoàn thành
                mHightLight!!.addHighLight(
                    R.id.btn_rightLight,
                    R.layout.info_gravity_left_down,
                    OnLeftPosCallback(45f),
                    RectLightShape()
                )
                    .addHighLight(
                        R.id.btn_light,
                        R.layout.info_gravity_left_down,
                        OnRightPosCallback(5f),
                        CircleLightShape()
                    )
                    .addHighLight(
                        R.id.btn_bottomLight,
                        R.layout.info_gravity_left_down,
                        OnTopPosCallback(),
                        CircleLightShape()
                    )
                // Sau đó hiển thị bố cục làm nổi bật
                mHightLight!!.show()
            }
            .setClickCallback {
                Toast.makeText(
                    this@MainActivity,
                    "Đã nhấp và hiển thị gợi ý tiếp theo bởi chính bạn",
                    Toast.LENGTH_SHORT
                ).show()
                mHightLight!!.next()
            }
    }

    /**
     * Hiển thị bố cục gợi ý ở chế độ next
     * @param view
     * @author isanwenyu@163.com
     */
    fun showNextTipView(view: View?) {
        mHightLight = HighLight(this@MainActivity) //
            .anchor(findViewById(R.id.id_container)) // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
            .addHighLight(
                R.id.btn_rightLight,
                R.layout.info_gravity_left_down,
                OnLeftPosCallback(45f),
                RectLightShape()
            )
            .addHighLight(
                R.id.btn_light,
                R.layout.info_gravity_left_down,
                OnRightPosCallback(5f),
                CircleLightShape()
            )
            .addHighLight(
                R.id.btn_bottomLight,
                R.layout.info_gravity_left_down,
                OnTopPosCallback(),
                CircleLightShape()
            )
            .addHighLight(
                view,
                R.layout.info_gravity_left_down,
                OnBottomPosCallback(60f),
                CircleLightShape()
            )
            .autoRemove(false)
            .enableNext()
            .setClickCallback {
                Toast.makeText(
                    this@MainActivity,
                    "Đã nhấp và hiển thị gợi ý tiếp theo bởi chính bạn",
                    Toast.LENGTH_SHORT
                ).show()
                mHightLight!!.next()
            }
        mHightLight!!.show()
    }

    /**
     * Hiển thị bố cục làm nổi bật với nút "Tôi đã biết"
     * @param view id là R.id.iv_known
     * @author isanwenyu@163.com
     */
    fun showKnownTipView(view: View?) {
        mHightLight = HighLight(this@MainActivity) //
            .autoRemove(false) // Đặt tự động xóa khi nhấp vào nền thành false, mặc định là true
            .intercept(false) // Đặt thuộc tính chặn thành false để bố cục làm nổi bật không ảnh hưởng đến hiệu ứng trượt của bố cục phía sau, đồng thời làm cho callback click phía dưới không hiệu lực
            .setClickCallback {
                Toast.makeText(
                    this@MainActivity,
                    "Đã nhấp và tự xóa view HightLight",
                    Toast.LENGTH_SHORT
                ).show()
                remove()
            }
            .anchor(findViewById(R.id.id_container)) // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
            .addHighLight(
                R.id.btn_rightLight,
                R.layout.info_known,
                OnLeftPosCallback(45f),
                RectLightShape()
            )
            .addHighLight(
                R.id.btn_light,
                R.layout.info_known,
                OnRightPosCallback(5f),
                CircleLightShape(0f, 0f, 0f)
            )
            .addHighLight(
                R.id.btn_bottomLight,
                R.layout.info_known,
                OnTopPosCallback(),
                CircleLightShape()
            )
            .addHighLight(
                view,
                R.layout.info_known,
                OnBottomPosCallback(10f),
                OvalLightShape(5f, 5f, 20f)
            )
        mHightLight!!.show()

        //        // Thêm bởi isanwenyu@163.com thiết lập listener chỉ có knownView cuối cùng được thêm vào HightLightView phản hồi sự kiện
//        // Tối ưu hóa bằng cách khai báo phương thức onClick trong bố cục {@link #clickKnown(view)} để phản hồi tất cả sự kiện click của các điều khiển có R.id.iv_known
//        View decorLayout = mHightLight.getHightLightView();
//        ImageView knownView = (ImageView) decorLayout.findViewById(R.id.iv_known);
//        knownView.setOnClickListener(new View.OnClickListener()
//          {
//            @Override
//            public void onClick(View view) {
//                remove(null);
//            }
//        });
    }

    /**
     * Hiển thị bố cục làm nổi bật "Tôi đã biết" ở chế độ next
     * @param view id là R.id.iv_known
     * @author isanwenyu@163.com
     */
    fun showNextKnownTipView(view: View?) {
        mHightLight = HighLight(this@MainActivity) //
            .autoRemove(false) // Đặt tự động xóa khi nhấp vào nền thành false, mặc định là true
            //                .intercept(false) // Đặt thuộc tính chặn thành false để bố cục làm nổi bật không ảnh hưởng đến hiệu ứng trượt của bố cục phía sau
            .intercept(true) // Thuộc tính chặn mặc định là true, làm cho ClickCallback bên dưới có hiệu lực
            .enableNext() // Bật chế độ next và hiển thị thông qua phương thức show, sau đó chuyển đến bố cục gợi ý tiếp theo bằng cách gọi phương thức next(), cho đến khi tự loại bỏ
            //                .setClickCallback(new HighLight.OnClickCallback() {
            //                    @Override
            //                    public void onClick() {
            //                        Toast.makeText(MainActivity.this, "clicked and remove HightLight view by yourself", Toast.LENGTH_SHORT).show();
            //                        remove(null);
            //                    }
            //                })
            .anchor(findViewById(R.id.id_container)) // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
            .addHighLight(
                R.id.btn_rightLight,
                R.layout.info_known,
                OnLeftPosCallback(45f),
                RectLightShape(0f, 0f, 15f, 0f, 0f)
            ) // Hình chữ nhật không có góc tròn
            .addHighLight(
                R.id.btn_light,
                R.layout.info_known,
                OnRightPosCallback(5f),
                object : BaseLightShape(
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        5f,
                        getResources().displayMetrics
                    ),
                    TypedValue.applyDimension(
                        TypedValue.COMPLEX_UNIT_DIP,
                        5f,
                        getResources().displayMetrics
                    ),
                    0f
                ) {
                    override fun resetRectF4Shape(viewPosInfoRectF: RectF, dx: Float, dy: Float) {
                        // Thu nhỏ phạm vi điều khiển làm nổi bật
                        viewPosInfoRectF.inset(dx, dy)
                    }

                    override fun drawShape(bitmap: Bitmap, viewPosInfo: ViewPosInfo) {
                        // Tùy chỉnh hình dạng làm nổi bật của bạn
                        val canvas = Canvas(bitmap)
                        val paint = Paint(Paint.ANTI_ALIAS_FLAG)
                        paint.isDither = true
                        paint.isAntiAlias = true
                        // blurRadius phải lớn hơn 0
                        if (blurRadius > 0) {
                            paint.maskFilter = BlurMaskFilter(
                                blurRadius,
                                BlurMaskFilter.Blur.SOLID
                            )
                        }
                        val rectF = viewPosInfo.rectF
                        canvas.drawOval(rectF!!, paint)
                    }
                })
            .addHighLight(
                R.id.btn_bottomLight,
                R.layout.info_known,
                OnTopPosCallback(),
                CircleLightShape()
            )
            .addHighLight(
                view,
                R.layout.info_known,
                OnBottomPosCallback(10f),
                OvalLightShape(5f, 5f, 20f)
            )
            .setOnRemoveCallback {
                Toast.makeText(
                    this@MainActivity,
                    "View HightLight đã được loại bỏ",
                    Toast.LENGTH_SHORT
                ).show()
            }
            .setOnShowCallback {
                Toast.makeText(
                    this@MainActivity,
                    "View HightLight đã được hiển thị",
                    Toast.LENGTH_SHORT
                ).show()
            }.setOnNextCallback { hightLightView, targetView, tipView -> // targetView là nút mục tiêu, tipView là bố cục gợi ý được thêm vào, có thể tìm trực tiếp nút 'Tôi đã biết' để thêm sự kiện lắng nghe, v.v.
                Toast.makeText(
                    this@MainActivity,
                    "HightLight hiển thị TipView tiếp theo, targetViewID:" + (if (targetView == null) null else targetView.id) + ",tipViewID:" + (if (tipView == null) null else tipView.id),
                    Toast.LENGTH_SHORT
                ).show()
            }
        mHightLight!!.show()
    }

    /**
     * Phản hồi tất cả sự kiện click của các điều khiển có R.id.iv_known
     *
     *
     * Loại bỏ bố cục làm nổi bật
     *
     *
     * @param view
     */
    fun clickKnown() {
        if (mHightLight!!.isShowing && mHightLight!!.isNext)  // Nếu đã bật chế độ next
        {
            mHightLight!!.next()
        } else {
            remove()
        }
    }

    private fun showTipMask() {
//        mHightLight = new HighLight(MainActivity.this)//
//                .anchor(findViewById(R.id.id_container))
        // Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
//                .addHighLight(R.id.id_btn_important, R.layout.info_up,
//                        new HighLight.OnPosCallback()
//                        {
//                            @Override
//                            public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo)
//                            {
//                                marginInfo.leftMargin = rectF.right - rectF.width() / 2;
//                                marginInfo.topMargin = rectF.bottom;
//                            }
//                        })//
//                .addHighLight(R.id.id_btn_amazing, R.layout.info_down, new HighLight.OnPosCallback()
//                {
//                    /**
//                     * @param rightMargin Khoảng cách bên phải của view làm nổi bật trong anchor
//                     * @param bottomMargin Khoảng cách bên dưới của view làm nổi bật trong anchor
//                     * @param rectF l,t,r,b,w,h của view làm nổi bật
//                     * @param marginInfo Thiết lập vị trí của bố cục của bạn, thường thiết lập l,t hoặc r,b
//                     */
//                    @Override
//                    public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo)
//                    {
//                        marginInfo.rightMargin = rightMargin + rectF.width() / 2;
//                        marginInfo.bottomMargin = bottomMargin + rectF.height();
//                    }
//
//                });
//        .addHighLight(R.id.id_btn_important_right,R.layout.info_gravity_right_up, new HighLight.OnPosCallback(){
//
//
//            @Override
//            public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo) {
//                marginInfo.rightMargin = rightMargin;
//                marginInfo.topMargin = rectF.top + rectF.height();
//            }
//        })
//        .addHighLight(R.id.id_btn_whoami, R.layout.info_gravity_left_down, new HighLight.OnPosCallback() {
//
//
//            @Override
//            public void getPos(float rightMargin, float bottomMargin, RectF rectF, HighLight.MarginInfo marginInfo) {
//                marginInfo.leftMargin = rectF.right - rectF.width()/2;
//                marginInfo.bottomMargin = bottomMargin + rectF.height();
//            }
//        })
//        .setClickCallback(new HighLight.OnClickCallback() {
//            @Override
//            public void onClick() {
//                Toast.makeText(MainActivity.this,"clicked",Toast.LENGTH_SHORT).show();
//            }
//        });

//        mHightLight.show();
//        mHightLight = new HighLight(MainActivity.this)//
//                .anchor(findViewById(R.id.id_container))// Nếu thêm lớp hướng dẫn lên Activity, không cần thiết lập anchor
//                .addHighLight(R.id.btn_rightLight,R.layout.info_left, new OnLeftPosCallback(10),new RectLightShape())
//                .addHighLight(R.id.btn_light,R.layout.info_right,new OnRightPosCallback(),new CircleLightShape())
//                .addHighLight(R.id.btn_bottomLight,R.layout.info_up,new OnTopPosCallback(46),new CircleLightShape())
//                .addHighLight(R.id.id_btn_amazing,R.layout.info_up,new OnBottomPosCallback(46),new CircleLightShape());
//        mHightLight.show();
    }


    fun remove() {
        mHightLight!!.remove()
    }

    fun add() {
        mHightLight!!.show()
    }


    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
//        getMenuInflater().inflate(R.menu.menu_main, menu);


        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Xử lý các lần nhấp vào item trên thanh hành động tại đây. Thanh hành động sẽ
        // tự động xử lý các lần nhấp vào nút Home/Up, miễn là
        // bạn chỉ định một activity cha trong AndroidManifest.xml.
        item.itemId

        return super.onOptionsItemSelected(item)
    }
}
