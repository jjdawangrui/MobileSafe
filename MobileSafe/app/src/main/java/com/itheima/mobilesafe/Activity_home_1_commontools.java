package com.itheima.mobilesafe;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.View;
import android.widget.Toast;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;
import service.DogService;
import utils.ServiceUtils;
import utils.SmsUtil;
import view.ItemSettingView;

/**
 * Created by Rayn on 2017/5/21 0021.
 */

public class Activity_home_1_commontools extends Activity {
    @InjectView(R.id.itemsettingview_query)
    ItemSettingView itemsettingviewQuery;
    @InjectView(R.id.itemsettingview_save)
    ItemSettingView itemsettingviewSave;
    @InjectView(R.id.itemsettingview_backup)
    ItemSettingView itemsettingviewBackup;
    @InjectView(R.id.itemsettingview_lock)
    ItemSettingView itemsettingviewLock;
    @InjectView(R.id.itemsettingview_dogservice)
    ItemSettingView itemsettingviewDogservice;


    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home_1_commontools);
        ButterKnife.inject(this);

        //显示服务器初始的状态
        boolean running = ServiceUtils.isRunning(this, DogService.class);
        itemsettingviewDogservice.setFlag(running);
    }

    @OnClick({R.id.itemsettingview_query, R.id.itemsettingview_save, R.id.itemsettingview_backup, R.id.itemsettingview_lock, R.id.itemsettingview_dogservice})
    public void onViewClicked(View view) {
        switch (view.getId()) {
            case R.id.itemsettingview_query:
                Intent intent = new Intent(Activity_home_1_commontools.this,Activity_commontools_1_querylocation.class);
                startActivity(intent);
                break;
            case R.id.itemsettingview_save://短信备份
                final ProgressDialog progressDialog = new ProgressDialog(this);
                progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog.show();
                SmsUtil.backup(this, new SmsUtil.OnBackupListener() {//这个上下文和后面这个吊
                    @Override
                    public void setMax(int maxProgress) {
                        progressDialog.setMax(maxProgress);
                    }
                    @Override
                    public void setCurrentProgress(int currentProgress) {
                        progressDialog.setProgress(currentProgress);
                    }
                    @Override
                    public void onSuccessed() {
                        progressDialog.dismiss();
                        Toast.makeText(Activity_home_1_commontools.this,"备份完成",Toast.LENGTH_SHORT).show();
                    }

                    @Override
                    public void onFail() {
                        progressDialog.dismiss();
                        Toast.makeText(Activity_home_1_commontools.this,"备份失败",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.itemsettingview_backup://短信还原
                final ProgressDialog progressDialog1 = new ProgressDialog(this);
                progressDialog1.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);
                progressDialog1.show();

                //从sd卡中读取json数据，解析，插入到短信的数据库表中去
                SmsUtil.restore(this, new SmsUtil.OnBackupListener() {
                    @Override
                    public void setMax(int maxProgress) {
                        //提示还原总进度方法
                        progressDialog1.setMax(maxProgress);
                    }
                    @Override
                    public void setCurrentProgress(int currentProgress) {
                        progressDialog1.setProgress(currentProgress);
                    }
                    @Override
                    public void onSuccessed() {
                        progressDialog1.dismiss();
                        Toast.makeText(Activity_home_1_commontools.this,"还原完成",Toast.LENGTH_SHORT).show();
                    }
                    @Override
                    public void onFail() {
                        progressDialog1.dismiss();
                        Toast.makeText(Activity_home_1_commontools.this,"还原失败",Toast.LENGTH_SHORT).show();
                    }
                });
                break;
            case R.id.itemsettingview_lock://程序锁
                startActivity(new Intent(getApplicationContext(),Activity_commontools_4_applock.class));
                break;
            case R.id.itemsettingview_dogservice://电子狗
                //点击过程中可以切换服务开启和关闭的状态
                itemsettingviewDogservice.reverseFlag();
                boolean running = ServiceUtils.isRunning(this, DogService.class);
                Intent intent2 = new Intent(this, DogService.class);
                if (running){
                    //点击前服务开启的，点击后服务关闭
                    stopService(intent2);
                }else{
                    startService(intent2);
                }
                break;
        }
    }
}
