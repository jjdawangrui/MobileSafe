package com.itheima.mobilesafe;


import android.app.Activity;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.TranslateAnimation;
import android.widget.BaseAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.List;

import bean.AppInfo;
import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import db.AppLockDao;
import engine.AppInfoProvider;

public class Activity_commontools_4_applock extends Activity {

    @InjectView(R.id.btn_unlock)
    Button btnUnlock;
    @InjectView(R.id.btn_lock)
    Button btnLock;
    @InjectView(R.id.tv_title_des)
    TextView tvTitleDes;
    @InjectView(R.id.lv_unlock)
    ListView lvUnlock;
    @InjectView(R.id.lv_lock)
    ListView lvLock;

    private AppLockDao appLockDao;
    private ArrayList<AppInfo> unLockList;
    private ArrayList<AppInfo> lockList;
    private MyAdapter adapterUnlock;
    private MyAdapter adapterLock;
    private LinearLayout progress;

    private TranslateAnimation leftToRightAnimation;
    private TranslateAnimation rightToLeftAnimation;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_commontools_4_applock);
        ButterKnife.inject(this);

        progress = (LinearLayout) findViewById(R.id.progress);
        appLockDao = new AppLockDao(this);

        //已经加锁应用包名，存储在lock.db的locklist表中，区分每一个应用是加锁的还是未加锁

        unLockList = new ArrayList<>();
        lockList = new ArrayList<>();
        //获取所有应用的集合
        List<AppInfo> appInfoList = AppInfoProvider.getAppInfoList(Activity_commontools_4_applock.this);
        //遍历该集合
        for (AppInfo appInfo: appInfoList) {
            //通过包名查找数据库，获取是否加锁
            boolean isLock = appLockDao.queryPackageName(appInfo.getPackageName());
            if (isLock){//加锁的就添加到加锁的集合
                lockList.add(appInfo);
            }else{//不加锁的就添加到不加锁的集合
                unLockList.add(appInfo);
            }
        }
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                //将数据放置在已加锁和未加锁的ListView列表中显示
                //2个ListView列表公用同一个数据适配器，通过构造方法中的参数区分使用加锁集合
                    //还是未加锁集合   false 未加锁集合     true 已加锁集合
                adapterUnlock = new MyAdapter(false);
                lvUnlock.setAdapter(adapterUnlock);

                adapterLock = new MyAdapter(true);
                lvLock.setAdapter(adapterLock);
                //打开界面的是，默认显示的是未加锁的列表，所以title设置为未加锁
                tvTitleDes.setText("未加锁("+unLockList.size()+")");

                progress.setVisibility(View.GONE);
            }
        });

        initAnimation();
    }

    private void initAnimation() {
        //由左向右移动动画（x平移，y不动）,未加锁---->已加锁
        leftToRightAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 1.0f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        leftToRightAnimation.setDuration(500);
        //由右向左移动动画，已加锁---->未加锁
        rightToLeftAnimation = new TranslateAnimation(
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, -1.0f,
                Animation.RELATIVE_TO_SELF, 0,
                Animation.RELATIVE_TO_SELF, 0);
        rightToLeftAnimation.setDuration(500);
    }

    class MyAdapter extends BaseAdapter {
        private boolean isLock;
        boolean isStart = false;

        public MyAdapter(boolean isLock){//握草这个吊了
            this.isLock = isLock;
        }
        @Override
        public int getCount() {
            if (isLock){
                return lockList.size();
            }else{
                return unLockList.size();
            }
        }

        @Override
        public AppInfo getItem(int position) {
            if (isLock){
                return lockList.get(position);
            }else{
                return unLockList.get(position);
            }
        }

        @Override
        public long getItemId(int position) {
            return position;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            //复用convertView
            //生成viewHolder减少findViewById
            ViewHolder viewHolder = null;
            if (convertView == null){
                viewHolder = new ViewHolder();
                convertView = View.inflate(Activity_commontools_4_applock.this,R.layout.item_app_lock,null);
                viewHolder.ivIcon = (ImageView) convertView.findViewById(R.id.iv_icon);
                viewHolder.tvAppName = (TextView) convertView.findViewById(R.id.tv_app_name);
                viewHolder.ivIsLock = (ImageView) convertView.findViewById(R.id.iv_isLock);
                convertView.setTag(viewHolder);
            }else{
                viewHolder = (ViewHolder) convertView.getTag();
            }

            final AppInfo appInfo = getItem(position);
            viewHolder.ivIcon.setImageDrawable(appInfo.getDrawable());
            viewHolder.tvAppName.setText(appInfo.getName());
            if (isLock){
                //需要显示未锁上图片
                viewHolder.ivIsLock.setImageResource(R.drawable.selector_unlock_bg);
            }else{
                //需要显示锁上图片
                viewHolder.ivIsLock.setImageResource(R.drawable.selector_lock_bg);
            }

            final View viewAnimation = convertView;//动画的view

            viewHolder.ivIsLock.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    //设置一个flag，要前一个动画结束，才能开始下一个
                    if(isStart){
                        return;
                    }
                    //因为现在监听的是2个ListView列表公用的数据适配器，所以点击的控件，可能是
                    //已加锁列表中的控件，也可能是未加锁列表中的控件，判断目前以下的那种情况
                    if (isLock){
                        //已加锁------->未加锁
                        //1.在数据库中删除一条记录
                        boolean success = appLockDao.delete(appInfo.getPackageName());
                        if (success){
                            //动画的监听，大意是，动画结束之后，才把条目删掉
                            rightToLeftAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    isStart = true;//开始了了以后设置成true，下一个动作就不能开始
                                }

                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    //数据库中删除成功，将删除的应用bean对象在已加锁集合中移除
                                    lockList.remove(appInfo);
                                    //未加锁的集合中对象添加一个
                                    unLockList.add(appInfo);

                                    //通知已加锁和未加锁的数据适配器刷新
                                    adapterUnlock.notifyDataSetChanged();
                                    adapterLock.notifyDataSetChanged();

                                    if (tvTitleDes!=null && unLockList!=null){
                                        tvTitleDes.setText("已加锁("+lockList.size()+")");
                                    }

                                    isStart = false;//结束就可以回来了
                                }

                                @Override
                                public void onAnimationRepeat(Animation animation) {

                                }
                            });
                            viewAnimation.startAnimation(rightToLeftAnimation);//开始动画


                        }else{
                            Toast.makeText(getApplicationContext(),"解锁失败",Toast.LENGTH_SHORT).show();
                        }
                    }else{
                        //未加锁------->已加锁
                        boolean success = appLockDao.insert(appInfo.getPackageName());
                        if (success){
                            leftToRightAnimation.setAnimationListener(new Animation.AnimationListener() {
                                @Override
                                public void onAnimationStart(Animation animation) {
                                    isStart = true;
                                }
                                @Override
                                public void onAnimationEnd(Animation animation) {
                                    isStart = false;
                                    //未加锁集合中删除一条数据
                                    unLockList.remove(appInfo);
                                    //已加锁集合中添加一条数据
                                    lockList.add(appInfo);
                                    //刷新数据适配器
                                    //通知已加锁和未加锁的数据适配器刷新
                                    adapterUnlock.notifyDataSetChanged();
                                    adapterLock.notifyDataSetChanged();
                                    if (tvTitleDes!=null && unLockList!=null){
                                        tvTitleDes.setText("未加锁("+unLockList.size()+")");
                                    }
                                }
                                @Override
                                public void onAnimationRepeat(Animation animation) {
                                }
                            });

                            viewAnimation.startAnimation(leftToRightAnimation);//开始动画

                        }else{
                            Toast.makeText(getApplicationContext(),"加锁失败",Toast.LENGTH_SHORT).show();
                        }
                    }
                }
            });
            return convertView;
        }
    }
    class ViewHolder{
        ImageView ivIcon;
        TextView tvAppName;
        ImageView ivIsLock;
    }

    @OnClick({R.id.btn_unlock, R.id.btn_lock})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.btn_unlock:
                //点中未加锁，未加锁：图片变蓝色，文字变白色
                btnUnlock.setBackgroundResource(R.drawable.shape_unlock_bg_blue);
                btnUnlock.setTextColor(Color.WHITE);
                //已加锁：图片变白，文字变蓝色
                btnLock.setBackgroundResource(R.drawable.shape_lock_bg_white);
                btnLock.setTextColor(Color.BLUE);

                if (tvTitleDes!=null && unLockList!=null){
                    tvTitleDes.setText("未加锁("+unLockList.size()+")");
                }

                lvUnlock.setVisibility(View.VISIBLE);
                lvLock.setVisibility(View.GONE);
                break;
            case R.id.btn_lock:
                //点中已加锁，已加锁：图片变蓝色，文字变白色
                btnLock.setBackgroundResource(R.drawable.shape_lock_bg_blue);
                btnLock.setTextColor(Color.WHITE);
                //未加锁：图片变白，文字变蓝色
                btnUnlock.setBackgroundResource(R.drawable.shape_unlock_bg_white);
                btnUnlock.setTextColor(Color.BLUE);

                if (tvTitleDes!=null && unLockList!=null){
                    tvTitleDes.setText("已加锁("+lockList.size()+")");
                }

                lvUnlock.setVisibility(View.GONE);
                lvLock.setVisibility(View.VISIBLE);
                break;
        }
    }
}
