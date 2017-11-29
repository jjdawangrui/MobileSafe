package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.util.Log;

import engine.ProcessInfoProvider;

public class LockClearService extends Service{

    private ScreenOffReceiver screenOffReceiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        Log.i("","LockClearService 服务开启");
        //注册广播接受者，监听锁屏广播
        IntentFilter intentFilter = new IntentFilter(Intent.ACTION_SCREEN_OFF);
        screenOffReceiver = new ScreenOffReceiver();
        registerReceiver(screenOffReceiver,intentFilter);//注册这个接收者，将其与锁屏意图相关联
        super.onCreate();
    }

    class ScreenOffReceiver extends BroadcastReceiver{
        @Override
        public void onReceive(Context context, Intent intent) {
            //杀死所有的进程
            ProcessInfoProvider.killAllProcess(context);
        }
    }

    @Override
    public void onDestroy() {
        //注销广播接受者
        super.onDestroy();
        unregisterReceiver(screenOffReceiver);//un注册，注销，也就是取消关联
        Log.i("","LockClearService 服务关闭");
    }
}
