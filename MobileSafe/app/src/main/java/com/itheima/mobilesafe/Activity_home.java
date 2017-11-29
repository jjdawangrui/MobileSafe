package com.itheima.mobilesafe;

import android.animation.ObjectAnimator;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.GridView;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;

/**
 * Created by Rayn on 2017/5/18 0018.
 */

public class Activity_home extends Activity {

    public static String[] titles = new String[]{"常用工具", "进程管理", "手机杀毒", "功能设置"};
    public static String[] des = new String[]{"工具大全", "管理运行进程", "病毒无处藏身", "占位"};
    //定义1个图片的数组,里面放置的是图片的资源文件索引 R.mipmap.xxx
    public static int[] drawables = {R.mipmap.cygj, R.mipmap.jcgl, R.mipmap.sjsd, R.mipmap.srlj};

    //这是gridview上面的3个控件，不能用黄油刀
    ImageView imageviewIcon;
    TextView textviewTop;
    TextView textViewBottom;

    @InjectView(R.id.imageview_heima)
    ImageView imageviewHeima;
    @InjectView(R.id.textview_big)
    TextView textviewBig;
    @InjectView(R.id.textview_small)
    TextView textviewSmall;
    @InjectView(R.id.imageview_setting)
    ImageView imageviewSetting;
    @InjectView(R.id.gridview)
    GridView gridview;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);
        ButterKnife.inject(this);

        //GridView和ListView使用方式类似
        //1.准备列表数据集合
        //2.准备数据适配器
        MyBaseAdapter adapter = new MyBaseAdapter();
        //3.将数据适配器设置给GridView
        gridview.setAdapter(adapter);
        //旋转对象，传控件，Y轴，从0转到360度
        ObjectAnimator objectAnimator = ObjectAnimator.ofFloat(imageviewHeima,"rotationY",0,360);
        objectAnimator.setDuration(2000);//每次持续2秒
        objectAnimator.setRepeatMode(ObjectAnimator.REVERSE);//重复模式，反转，正转一次反转一次
        objectAnimator.setRepeatCount(ObjectAnimator.INFINITE);//旋转次数，无限
        objectAnimator.start();

        gridview.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                switch (position){
                    case 0:
                        startActivity(new Intent(Activity_home.this,Activity_home_1_commontools.class));
                        break;
                    case 1:
                        startActivity(new Intent(Activity_home.this,Activity_home_2_process.class));
                        break;
                    case 2:
                        startActivity(new Intent(Activity_home.this,Activity_home_3_antiVirus.class));
                        break;
                    case 3:

                        break;
                }
            }
        });

    }


    private class MyBaseAdapter extends BaseAdapter {
        @Override
        public int getCount() {
            return titles.length;
        }
        @Override
        public Object getItem(int position) {
            return null;
        }
        @Override
        public long getItemId(int position) {
            return 0;
        }
        @Override//因为item很少，不需要优化
        public View getView(int position, View convertView, ViewGroup parent) {
            if (convertView == null) {
                convertView = View.inflate(Activity_home.this, R.layout.item_home, null);
            }
            imageviewIcon = (ImageView) convertView.findViewById(R.id.imageview_icon);
            textviewTop = (TextView) convertView.findViewById(R.id.textview_top);
            textViewBottom = (TextView) convertView.findViewById(R.id.textview_bottom);

            imageviewIcon.setImageResource(drawables[position]);//修改的是前景src
            textviewTop.setText(titles[position]);
            textViewBottom.setText(des[position]);
            return convertView;

//            View view = null;
//            if (convertView == null) {
//                view = View.inflate(Activity_home.this, R.layout.item_home, null);
//            } else {
//                view = convertView;
//            }
//            imageviewIcon.setImageResource(drawables[position]);//修改的是前景
//            textviewTop.setText(titles[position]);
//            textViewBottom.setText(des[position]);
//            return view;

        }
    }

    //右上角设置按钮的点击方法
    @OnClick(R.id.imageview_setting)
    public void onViewClicked() {
        Intent intent = new Intent(Activity_home.this,Activity_setting.class);
        startActivity(intent);
    }


}
