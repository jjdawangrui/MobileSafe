package view;

import android.content.Context;
import android.graphics.PixelFormat;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

import utils.Constant;
import utils.SPUtils;

/**
 * Created by HASEE.
 */

public class LocationToast {
    private Context context;
    private WindowManager windowManager;
    private WindowManager.LayoutParams params;
    private View mView;

    public LocationToast(Context context) {
        this.context = context;
        windowManager = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
    }

    /**
     * 将窗体的吐司隐藏掉
     */
    public void hide() {
        if (mView != null) {//如果有吐司，那么view上肯定有东西，肯定不是null
            if (mView.getParent() != null) {//就算view对象有东西，但是他没显示出来，也不用关
                windowManager.removeView(mView);//窗口管理者，调用方法从窗口上移除
            }
        }
    }

    //服务里有个监听拨入电话的方法，拨通的时候调用这个方法
    public void showToast(String address) {
        //显示下一个吐司时，隐藏上一个吐司
        hide();

        //1.指定吐司的布局
        mView = View.inflate(context, R.layout.toast_location, null);//toast_location就是吐司的布局
        RelativeLayout relativeLayoutRoot = (RelativeLayout) mView.findViewById(R.id.relativelayout_root);

        //获取归属地样式对话框中选中的背景，作为自定义吐司的背景
        int drawableResId = SPUtils.getInt(context, Constant.DRAWABLE_BG, R.drawable.shape_style_alpha);
                                                //如果通过key取不到drawables的图片索引值，默认半透明是选中的
        relativeLayoutRoot.setBackgroundResource(drawableResId);//设置吐司布局里面的background

        TextView textView = (TextView) mView.findViewById(R.id.tv_address);//从吐司view上找TextView，666
        textView.setText(address);//以后如果一个布局上只有一个，直接命名textView

        //2.获取windowManager对象，布局参数对象吧
        params = new WindowManager.LayoutParams();
        params.height = WindowManager.LayoutParams.WRAP_CONTENT;
        params.width = WindowManager.LayoutParams.WRAP_CONTENT;
        params.format = PixelFormat.TRANSLUCENT;
//            params.windowAnimations = com.android.internal.R.style.Animation_Toast;//源码样式，工程中拿不到
        params.type = WindowManager.LayoutParams.TYPE_PHONE;//修改吐司的类型
        params.setTitle("Toast");
        params.flags = WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON
                | WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE;
//                    | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE;

        //3.添加view到窗体上
        windowManager.addView(mView, params);

        //4.为了能够拖拽吐司，需要对其进行一个拖拽的事件监听
        mView.setOnTouchListener(new View.OnTouchListener() {
            //开始点的坐标位置，按下时候的坐标位置
            private int startX;
            private int startY;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                //触摸方法中（按下一次    移动（连续，移动多次触发）     抬起一次）
                //1.记录按下的坐标
                //2.记录移动过程中的坐标  移动过程中的坐标-按下的坐标 = 吐司需要移动的距离
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        startX = (int) event.getRawX();
                        startY = (int) event.getRawY();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        int moveX = (int) event.getRawX();
                        int moveY = (int) event.getRawY();
                        //获取x轴需要移动的距离和y轴需要移动的距离
                        int disX = moveX - startX;
                        int disY = moveY - startY;
                        //让吐司params参数中的x，y随偏移量进行叠加
                        params.x += disX;
                        params.y += disY;
                        //将params添加偏移量后的对象，重新作用在吐司上
                        windowManager.updateViewLayout(mView, params);

                        //将上一次移动的最终坐标，作为下一次移动的开始坐标
                        startX = moveX;
                        startY = moveY;
                        break;
                    case MotionEvent.ACTION_UP:
                        break;
                }
                return false;
            }
        });
    }

}
