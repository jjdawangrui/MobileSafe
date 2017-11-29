package service;

import android.app.ActivityManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.net.Uri;
import android.os.Handler;
import android.os.IBinder;
import android.os.SystemClock;
import android.support.annotation.Nullable;


import com.itheima.mobilesafe.Activity_lock;

import java.util.ArrayList;
import java.util.List;

import db.AppLockDao;

public class DogService extends Service{

    private AppLockDao appLockDao;
    private List<String> lockPackageNameList;
    private boolean isRun;
    private ActivityManager am;
    private List<String> unLockList;
    private MyReceiver myReceiver;
    private MyContentObserver myContentObserver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        unLockList = new ArrayList<>();//用来放已经解锁过的应用，锁屏的时候，清空
        appLockDao = new AppLockDao(this);//Dao类，里面增删改查的方法
        //返回是所有加锁应用的包名集合
        lockPackageNameList = appLockDao.queryAll();
        startRun();

        am = (ActivityManager) getSystemService(ACTIVITY_SERVICE);

        IntentFilter intentFilter = new IntentFilter();
        //自定义的解锁广播
        intentFilter.addAction("android.intent.action.UNLOCK");
        //锁屏广播
        intentFilter.addAction(Intent.ACTION_SCREEN_OFF);
        //开锁广播
        intentFilter.addAction(Intent.ACTION_SCREEN_ON);

        myReceiver = new MyReceiver();//调用下面广播接收的方法
        registerReceiver(myReceiver,intentFilter);

        //内容提供者 通过uri对外提供方法路径
        //内容解析者 通过内容提供者uri，去操作数据库
        //注册内容观察者，观察数据库的变化，一旦数据库发生变化，告知应用程序数据发生变化
        Uri uri = Uri.parse("content://com.mobilesafe.itheima.change.uri");
        myContentObserver = new MyContentObserver(null);
        getContentResolver().registerContentObserver(uri,true,myContentObserver);
                        //内容接收者点一个注册内容观察者，里面传地址？my内容观察者
        super.onCreate();
    }

    class MyContentObserver extends ContentObserver{
        public MyContentObserver(Handler handler) {
            super(handler);
        }
        @Override
        public void onChange(boolean selfChange) {
            //一旦监听到数据的变化，则让已加锁应用的集合进行更新
            lockPackageNameList = appLockDao.queryAll();
        }
    }

    //这个类写逻辑，当接收到哪个广播就执行相应的动作
    class MyReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //通过意图，获取传过来的动作是什么
            String action = intent.getAction();
            if (action.equals("android.intent.action.UNLOCK")){ //这里是输对了密码，就不要再锁了
                //获取发送广播时带上的数据
                String unLockPackageName = intent.getStringExtra("unlock");
                //将解锁过的应用的包名存储在一个集合中
                unLockList.add(unLockPackageName);
            }else if(action.equals(Intent.ACTION_SCREEN_OFF)){  //如果动作是锁屏
                //一旦锁屏，则不需要再让看们狗服务监听开启应用
                unLockList.clear();
                isRun = false;
            }else{                                              //如果动作是开屏，那么开启电子狗
                startRun();
            }
        }
    }


    /*当开启日历的时候，手机所处的任务栈发生改变，任务指向的是日历的任务栈，
    通过日历任务栈中的第一个activity，可以获取日历的包名，如果此包名在已加锁
    的数据库中，则认为此应用是加锁应用，需要弹出拦截界面。
    1、找到当前页面所处的任务栈
    2、获取任务栈栈顶activity
    3、获取任务栈栈顶activity所属的应用包名
    4、判断包名是否在数据库中，再弹出拦截界面，否则不弹出*/
    //开启电子狗
    private void startRun() {
        //提供一个可以由代码控制的死循环，代表看门狗一直在工作
        isRun = true;
        new Thread(){
            @Override
            public void run() {
                while(isRun){
                    SystemClock.sleep(300);
                    //1,获取手机当前所处的任务栈ActivityManager
                    //当不点击计算器，没有任务栈的时候，获取不到正在运行时的任务栈，所以没法进行下去
                    List<ActivityManager.RunningTaskInfo> runningTasks = am.getRunningTasks(1);
                    ActivityManager.RunningTaskInfo runningTaskInfo = runningTasks.get(0);
                    //2,获取栈顶部activity页面
                    ComponentName topActivity = runningTaskInfo.topActivity;
                    //3,获取activity所在的应用包名
                    String packageName = topActivity.getPackageName();
                    //如果现在检测的应用是已经解锁过的应用，那此应用的包名一定在unLockList集合中
                    if (unLockList.contains(packageName)){
                        continue;
                    }
                    //4，判断packageName是否在数据库中存储
                    boolean contains = lockPackageNameList.contains(packageName);
                    //
                    if(contains){
                        //应用加锁应用，弹出拦截界面
                        Intent intent = new Intent(getApplicationContext(), Activity_lock.class);
                        intent.putExtra("packageName",packageName);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);//把拦截界面放在新的任务栈上面
                        startActivity(intent);
                    }
                }
            }
        }.start();
    }

    //当服务关闭的时候
    @Override
    public void onDestroy() {
        if (myReceiver!=null){
            unregisterReceiver(myReceiver);//注销my接收者
        }
        isRun = false;//关掉电子狗的开关
        //注销my内容观察者
        getContentResolver().unregisterContentObserver(myContentObserver);
        super.onDestroy();
    }
}
