package com.itheima.mobilesafe;

import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import butterknife.ButterKnife;
import butterknife.InjectView;
import butterknife.OnClick;




public class Activity_lock extends AppCompatActivity {
    @InjectView(R.id.iv_icon)
    ImageView ivIcon;
    @InjectView(R.id.tv_app_name)
    TextView tvAppName;
    @InjectView(R.id.et_psd)
    EditText etPsd;
    @InjectView(R.id.btn_confirm)
    Button btnConfirm;
    private String packageName;

    @Override
    protected void onCreate(Bundle savedInstanceState) {                        //拦截界面
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_lock);
        ButterKnife.inject(this);
        //获取应用程序的包名
        if (getIntent()!=null){
            packageName = getIntent().getStringExtra("packageName");
        }
        //包管理
        PackageManager packageManager = getPackageManager();
        try {
            PackageInfo packageInfo = packageManager.getPackageInfo(packageName, 0);
                                            //包管理点一个获取包信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
                                                //包信息可以获取应用信息
            String name = applicationInfo.loadLabel(packageManager).toString();
                                        //应用信息，加载标签，包管理，然后转成String
            Drawable drawable = applicationInfo.loadIcon(packageManager);
                                        //应用信息，加载图片，包管理，直接就变成drawable可拉拽
            tvAppName.setText(name);
            ivIcon.setImageDrawable(drawable);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
    }

    @OnClick(R.id.btn_confirm)
    public void onClick() {
        //判断输入的密码是否正确
        String psd = etPsd.getText().toString();
        if ("123".equals(psd)){
            //发送广播，在此处将解锁过的应用，通过广播的形式告知看门狗服务，让其不要再对此应用进行监听
            Intent intent = new Intent();
            //给广播接受者提供过滤条件
            intent.setAction("android.intent.action.UNLOCK");
            //给广播接受者提供不需要监听应用的包名
            intent.putExtra("unlock",packageName);
            //发送广播
            sendBroadcast(intent);
            //密码输入正确，结束拦截界面，显示计算器
            finish();
        }
    }

    @Override
    public void onBackPressed() {
        //回退按钮一旦被点击，则会触发此方法，让程序跳转到桌面
        /*<intent-filter>
        <action android:name="android.intent.action.MAIN" />
        <category android:name="android.intent.category.HOME" />
        <category android:name="android.intent.category.DEFAULT" />
        <category android:name="android.intent.category.MONKEY"/>
        </intent-filter>*/
        Intent intent = new Intent("android.intent.action.MAIN");
        intent.addCategory("android.intent.category.HOME");
        startActivity(intent);
        finish();
    }
}
