package com.itheima.mobilesafe;

import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.SystemClock;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.concurrent.TimeUnit;

import butterknife.ButterKnife;
import butterknife.InjectView;
import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;
import utils.AssetsUtils;
import utils.Constant;
import utils.SPUtils;
import utils.StreamUtils;


public class Activity_splash extends AppCompatActivity {

    String desc;
    String downloadUrl;
    OkHttpClient okHttpClient;
    private static final int REQUEST_INSTALL_APK_CODE = 100;

    @InjectView(R.id.tv_version)//黄油刀
    TextView tv_version;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        System.out.println("有syso的日志！！！");
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);
        ButterKnife.inject(this);
        tv_version.setText("版本：" + getPackageVersionName());
        copyAddressDB();
        copyVirusDB();
        if (SPUtils.getBoolean(this, Constant.UPDATE_AUTO,true)){
            checkVersion();
        }else {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    enterHome();
                }
            },2000);
        }
    }

    private void copyVirusDB() {
        AssetsUtils.open(this,"antivirus.db");
    }

    private void copyAddressDB() {
        AssetsUtils.open(this,"address.db");
    }

    //检查版本的方法
    private void checkVersion(){
        //发送一个请求给服务器，获取update.json中的versionCode用于和本地的比较
        //httpUriConnection okhttp 导入jar包，记得要在build.gradle里面添加okhttp依赖库2个
        //1.创建okhttp对象过程，设置超时时间
        okHttpClient = new OkHttpClient.Builder()
                .connectTimeout(2, TimeUnit.SECONDS)//连接超时时间是2秒
                .readTimeout(2,TimeUnit.SECONDS)
                .build();
        //2.提供访问链接地址，前面的这个是通过手机访问的，不大明白
        String url = "http://10.0.2.2:8080/mobilesafe/update.json";
        //3.构建Request（请求方式get，请求地址url）
        Request request = new Request.Builder().get().url(url).build();
        //4.发送请求
        Call call = okHttpClient.newCall(request);
        call.enqueue(new Callback() {//请求的回调
            @Override//没成功就进入首页
            public void onFailure(Call call, IOException e) {
                enterHome();
            }

            @Override//如果请求成功
            public void onResponse(Call call, Response response) throws IOException {
                ResponseBody body = response.body();//原来已经有这个body了，就是请求文件的全部数据
                String data = body.string();
                try {
                    JSONObject jsonObject = new JSONObject(data);
                    desc = jsonObject.getString("desc");//版本更新的描述
                    downloadUrl = jsonObject.getString("downloadUrl");//下载地址
                    int remoteVersionCode = jsonObject.getInt("versionCode");//下载的版本
                    int localVersionCode = getPackageVersionCode();

                    if (remoteVersionCode > localVersionCode){//满足更新的条件
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                showDialog();
                            }
                        });
                    }else{
                        new Handler().postDelayed(new Runnable() {
                            @Override
                            public void run() {
                                enterHome();
                            }
                        },2000);
                    }
                } catch (JSONException e) {
                    enterHome();
                    e.printStackTrace();
                }
            }
        });
    }

    //显示是否下载，询问对话框
    private void showDialog(){
        AlertDialog.Builder builder = new AlertDialog.Builder(Activity_splash.this);
        builder.setTitle("有新的版本");
        builder.setMessage(desc);
        builder.setPositiveButton("立即更新", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();//对话框关掉
                downloadApk();
            }
        });
        builder.setNegativeButton("稍后再说", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
                enterHome();
            }
        });
        //对话框被取消的方法，比如按了返回键
        builder.setOnCancelListener(new DialogInterface.OnCancelListener() {
            @Override
            public void onCancel(DialogInterface dialog) {
                enterHome();
            }
        });
        builder.show();//最后别忘了
    }

    //下载Apk2.0的方法
    private void downloadApk() {
        if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
            final ProgressDialog progressDialog = new ProgressDialog(this);//进度条
            progressDialog.setProgressStyle(ProgressDialog.STYLE_HORIZONTAL);//变成横向
            progressDialog.show();//显示出来
            //构建下载请求
            Request request = new Request.Builder().get().url(downloadUrl).build();
            Call call = okHttpClient.newCall(request);
            call.enqueue(new Callback() {//之前是访问json，现在是下载apk，所以还要判断一次访问成功与否
                @Override
                public void onFailure(Call call, IOException e) {
                    enterHome();
                }

                @Override
                public void onResponse(Call call, Response response) throws IOException {
                    InputStream inputStream;
                    FileOutputStream fileOutputStream;
                    ResponseBody body = response.body();//因为已经关联了下载地址，所以这里body就是apk的全部数据
                    long length = body.contentLength();//那么长度就是文件的大小
                    progressDialog.setMax((int) length);

                    //1.sd卡所在的路径
                    String path = Environment.getExternalStorageDirectory()+"/mobilesafe_2.0.apk";
                    //2.读取服务端返回的数据
                    inputStream = body.byteStream();//这个方法很稳，输入流和body也就是apk的地址关联
                    File file = new File(path);
                    fileOutputStream = new FileOutputStream(file);
                    int len = 0;
                    byte[] arr = new byte[1024];
                    int progress = 0;
                    while ((len=inputStream.read(arr))!=-1){
                        fileOutputStream.write(arr,0,len);
                        progress += len;
//                        SystemClock.sleep(3);//我这里不能睡，电脑本来就很慢了
                        progressDialog.setProgress(progress);
                    }

                    installApk(file);//下载完成，安装apk
                }
            });
        }
    }

    private void installApk(File file){
//     <activity android:name=".PackageInstallerActivity"
//            android:configChanges="orientation|keyboardHidden"
//            android:theme="@style/TallTitleBarTheme">
//            <intent-filter>
//                <action android:name="android.intent.action.VIEW" />
//                <category android:name="android.intent.category.DEFAULT" />
//                <data android:scheme="content" />
//                <data android:scheme="file" />
//                <data android:mimeType="application/vnd.android.package-archive" />
//            </intent-filter>
//        </activity>
        Intent intent = new Intent();
        intent.setAction("android.intent.action.VIEW");
        intent.addCategory("android.intent.category.DEFAULT");
        intent.setDataAndType(Uri.fromFile(file),"application/vnd.android.package-archive");
        //在更新了新版本的应用后，就可以自动开启
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        //开启一个新的系统安装界面，等待其他安装或者取消的结果
        startActivityForResult(intent,REQUEST_INSTALL_APK_CODE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == REQUEST_INSTALL_APK_CODE){
            enterHome();
        }
        super.onActivityResult(requestCode, resultCode, data);
    }

    //进入到首页的方法
    private void enterHome(){//显示
        Intent intent = new Intent(Activity_splash.this,Activity_home.class);
        startActivity(intent);
        finish();
    }

    //返回应用程序的版本名称
    private String getPackageVersionName() {
        //包管理者对象，版本号，版本名称，所有的Activity，Service
        PackageManager packageManager = getPackageManager();
        try {
            //通过包管理，获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionName;//包信息，来获取版本名
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return "";
    }

    //把上面的改一下就行了
    private int getPackageVersionCode() {
        PackageManager packageManager = getPackageManager();
        try {
            //通过包管理，获取包信息
            PackageInfo packageInfo = packageManager.getPackageInfo(getPackageName(), 0);
            return packageInfo.versionCode;
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
        }
        return 0;
    }
}
