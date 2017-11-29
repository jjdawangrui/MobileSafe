package view;

import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Adapter;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;

import com.itheima.mobilesafe.R;

import utils.Constant;
import utils.SPUtils;

/**
 * Created by Rayn on 2017/5/23 0023.
 */

//继承对话框类，特么还有这个类？
public class DialogStyle extends Dialog {

    public static String[] colorDes = new String[]{
            "半透明","活力橙","卫士蓝","金属灰","苹果绿"
    };
    public static int[] drawables = new int[]{
            R.drawable.shape_style_alpha,
            R.drawable.shape_style_orange,
            R.drawable.shape_style_blue,
            R.drawable.shape_style_gray,
            R.drawable.shape_style_green};

    ListView listview_style;
    MyAdapter myAdapter;

    //把dialog放到屏幕下方
    public DialogStyle(Context context) {
        super(context);
        //对话框放置在窗体上，通过窗体指定对话框所在位置
        //1.获取窗体对象
        Window window = getWindow();
        //2.获取窗体中放置对话框参数信息params
        WindowManager.LayoutParams layoutParams = window.getAttributes();//窗口得到属性，得到布局属性
        //3.layoutParams对象指定对话框在屏幕的下方并且居中显示
        layoutParams.gravity = Gravity.BOTTOM|Gravity.CENTER_HORIZONTAL;//设置两个gravity
        //4.把layoutParams值告知窗体
        window.setAttributes(layoutParams);//窗口设置属性方法，把上面那个布局属性传进去
    }

    //指定对话框的布局效果
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        //给对话框设置一套布局文件
        setContentView(R.layout.dialog_list);//一个listview
        listview_style = (ListView) findViewById(R.id.listview_style);
        myAdapter = new MyAdapter();
        listview_style.setAdapter(myAdapter);//适配器上面放item

        //在条目的点击事件中，通过sp记录点中条目图片的drawable索引值，
        //当需要列表展示多张图片的时候，根据记录的drawable索引值和展示条目的图片drawable索引值比对
        //如果一致，则需要显示图片后方的选中的图片
        listview_style.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                SPUtils.saveInt(getContext(), Constant.DRAWABLE_BG,drawables[position]);
                //立即刷新界面，告知用户哪个条目被选中了
                myAdapter.notifyDataSetChanged();
                //隐藏对话框
                dismiss();
            }
        });
    }

    private class MyAdapter extends BaseAdapter{
        @Override
        public int getCount() {
            return colorDes.length;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView==null){
                convertView = View.inflate(getContext(),R.layout.item_dialog,null);
            }
            //item上的3个控件，分别是颜色，文字，对勾
            ImageView imageViewStyle = (ImageView) convertView.findViewById(R.id.imageview_sytle);
            TextView textviewColorDes = (TextView) convertView.findViewById(R.id.textview_color_des);
            ImageView imageviewCheck = (ImageView) convertView.findViewById(R.id.imageview_check);
            imageViewStyle.setImageResource(drawables[position]);
            textviewColorDes.setText(colorDes[position]);

            //如果通过key取不到drawables的图片索引值，默认半透明是选中的
            int drawableResId = SPUtils.getInt
                    (getContext(), Constant.DRAWABLE_BG, R.drawable.shape_style_alpha);
            //如果drawableResId一致，则说明后面的选中勾需要显示
            if (drawableResId == drawables[position]){//这就说明每次item机会走一次适配器，所以一个一个地比对，
                imageviewCheck.setVisibility(View.VISIBLE); //对上了，才设置对勾可见
            }else{
                imageviewCheck.setVisibility(View.GONE);
            }
            return convertView;
        }
    }
}
