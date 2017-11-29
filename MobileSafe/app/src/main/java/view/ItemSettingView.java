package view;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

/**
 * Created by Rayn on 2017/5/20 0020.
 */

//setting和commontools里的一条一条，都是这个view
public class ItemSettingView extends RelativeLayout{//继承相对布局，好理解
    // name space 命名空间 规范 schemas限定
    public static final String NAMESPACE = "http://schemas.android.com/apk/res-auto";
    private TextView textviewTitle;
    private ImageView imageviewToogle;

    //有这个初始化View的方法，里面用View来获取上面的控件
    private void initView() {
        //把itemSettingView打气成view
        View view = inflate(getContext(), R.layout.item_setting_view,null);
        //然后添加view
        addView(view);
        textviewTitle = (TextView) view.findViewById(R.id.textview_title);
        imageviewToogle = (ImageView) view.findViewById(R.id.imageview_toggle);
    }

    //用于记录自动更新是否开启的变量
    private boolean flag;

    //这个方法用来修改按钮的红绿色
    public void setFlag(boolean flag) {
        this.flag = flag;
        if (flag){
            //代表现在是开启的状态，修改按钮图片为绿色
            imageviewToogle.setImageResource(R.mipmap.on);//用ImageView的set图片资源的方法，就是在设置src
        }else{
            //代表现在是关闭的状态，修改按钮图片为红色
            imageviewToogle.setImageResource(R.mipmap.off);
        }
    }

    public boolean getFlag(){
        return flag;
    }

    public void reverseFlag(){
        flag = !flag;
        setFlag(flag);
    }

    public ItemSettingView(Context context) {
        this(context,null);
    }
    public ItemSettingView(Context context, AttributeSet attrs) {
        this(context, attrs,-1);
    }
    public ItemSettingView(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        initView();
        initAttrs(attrs);
    }


    private void initAttrs(AttributeSet attrs) {
        String title = attrs.getAttributeValue(NAMESPACE,"title");
        boolean isToggle = attrs.getAttributeBooleanValue(NAMESPACE,"isToggle",true);
        textviewTitle.setText(title);

        //设置是否有那个开关按钮
        if (isToggle){
            imageviewToogle.setVisibility(View.VISIBLE);
        }else{
            imageviewToogle.setVisibility(View.GONE);
        }
    }

}
