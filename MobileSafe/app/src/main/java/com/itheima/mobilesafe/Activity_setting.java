package com.itheima.mobilesafe;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import service.LocationService;
import utils.Constant;
import utils.SPUtils;
import utils.ServiceUtils;
import view.DialogStyle;
import view.ItemSettingView;

/**
 * Created by Rayn on 2017/5/20 0020.
 */

public class Activity_setting extends Activity {

    @InjectView(R.id.itemsettingview_1_autoupdate)
    ItemSettingView itemsettingview_1_autoupdate;
    @InjectView(R.id.itemsettingview_2_showlocation)
    ItemSettingView itemsettingview_2_showlocation;
    @InjectView(R.id.itemsettingview_3_locationstyle)
    ItemSettingView itemsettingview_3_locationstyle;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_setting);
        ButterKnife.inject(this);
        //先初始化，那个中间的常量，是个j8，可以换成任意String
        itemsettingview_1_autoupdate.setFlag(SPUtils.getBoolean(this, Constant.UPDATE_AUTO, true));
    }

    @OnClick({R.id.itemsettingview_1_autoupdate, R.id.itemsettingview_2_showlocation, R.id.itemsettingview_3_locationstyle})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            //自动更新开关
            case R.id.itemsettingview_1_autoupdate:
//                if (itemsettingview_1_autoupdate.getFlag()){
//                    //点击之前是开启的，点完后就关闭
//                    itemsettingview_1_autoupdate.setFlag(false);
//                    SPUtils.saveBoolean(this, Constant.UPDATE_AUTO,itemsettingview_1_autoupdate.getFlag());
//                }else{
//                    //点击之前是关闭的，点完后就开启
//                    itemsettingview_1_autoupdate.setFlag(true);
//                    SPUtils.saveBoolean(this, Constant.UPDATE_AUTO,itemsettingview_1_autoupdate.getFlag());
//                }
                itemsettingview_1_autoupdate.reverseFlag();
                SPUtils.saveBoolean(this, Constant.UPDATE_AUTO, itemsettingview_1_autoupdate.getFlag());
                break;

            //是否显示归属地对话框，的按钮
            case R.id.itemsettingview_2_showlocation:
                //按了之后，就置反，红变绿，绿变红
                itemsettingview_2_showlocation.reverseFlag();//只要有按钮的就要来一个这个方法我觉得
                //这个方法是将LocationService中的服务与系统的所有服务比对，有就true，也就是传入的服务是否在开启？
                boolean running = ServiceUtils.isRunning(this, LocationService.class);//传入上下文和服务类的字节码
                //这不再是开启页面的意图了，这是开启服务的意图，但是还没开
                Intent intent = new Intent(this, LocationService.class);
                if (running) {
                    //如果点击前，服务是开启的，点击后就关闭
                    stopService(intent);
                } else {
                    //如果点击前服务是关闭的，点击后就开启
                    startService(intent);
                }
                break;

            //选择对话框的风格
            case R.id.itemsettingview_3_locationstyle:
                //弹出样式选择对话框,自定义对话框
                DialogStyle dialogStyle = new DialogStyle(this);
                dialogStyle.show();
                break;
        }
    }

    @Override
    protected void onStart() {
        //地址服务开启还是关闭的状态，显示给用户查看
        boolean running = ServiceUtils.isRunning(this, LocationService.class);
        itemsettingview_2_showlocation.setFlag(running);
        super.onStart();
    }
}
