package service;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;
import android.view.View;
import android.view.WindowManager;
import android.widget.TextView;
import android.widget.Toast;

import com.itheima.mobilesafe.R;

import dao.LocationDao;
import utils.ServiceUtils;
import view.ItemSettingView;
import view.LocationToast;

/**
 * Created by Rayn on 2017/5/22 0022.
 */

//服务里有个监听拨入电话的方法，拨通的时候调用LocationToast里面的showDialog方法
public class LocationService extends Service {
    private TelephonyManager telephonyManager;
    private MyPhoneStateListener myPhoneStateListener;
    private LocationToast locationtoast;
    CallBroadcastReceiver receiver;

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        //电话管理者对象
        telephonyManager = (TelephonyManager) getSystemService(TELEPHONY_SERVICE);
        //监听电话状态
        MyPhoneStateListener myPhoneStateListener = new MyPhoneStateListener();
        telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_CALL_STATE);
        locationtoast = new LocationToast(getApplicationContext());

        //创建监听拨出电话的广播接受者
        receiver = new CallBroadcastReceiver();
        //添加拨出电话的过滤条件
        IntentFilter intentFilter = new IntentFilter();
        intentFilter.addAction(Intent.ACTION_NEW_OUTGOING_CALL);
        //开启广播，监听拨出电话动作
        registerReceiver(receiver, intentFilter);
    }

    //广播，监听拨打电话
    class CallBroadcastReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            //获取拨出的电话号码
            String phoneNumber = intent.getStringExtra(Intent.EXTRA_PHONE_NUMBER);
            //根据拨出的电话号码，查询归属地
            String address = LocationDao.getLocation(phoneNumber, getApplicationContext());
            locationtoast.showToast(address);
        }
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onDestroy() {
        //在服务器关闭后，取消对电话状态的监听
        if (telephonyManager != null && myPhoneStateListener != null) {
            telephonyManager.listen(myPhoneStateListener, PhoneStateListener.LISTEN_NONE);
        }

        //注销拨出电话的广播
        if (receiver != null) {
            unregisterReceiver(receiver);
        }
        super.onDestroy();
    }

    class MyPhoneStateListener extends PhoneStateListener {
        //电话状态一改变就会触发的方法
        @Override                       //获取状态和打进来的电话
        public void onCallStateChanged(int state, String incomingNumber) {
            //在电话状态发生改变的时候，触发的方法
            //state 电话状态（空闲     响铃      摘机（通话））
            //incomingNumber 拨入的电话号码
            switch (state) {
                case TelephonyManager.CALL_STATE_IDLE://空闲
                    System.out.println("电话挂断，归属地隐藏");
                    locationtoast.hide();
                    break;
                case TelephonyManager.CALL_STATE_RINGING://响铃
                    System.out.println("电话响铃，显示吐司");
                    String location = LocationDao.getLocation(incomingNumber, getApplicationContext());
                    //Toast.makeText(getApplicationContext(), location, Toast.LENGTH_LONG).show();
                    locationtoast.showToast(location);
                    break;
                case TelephonyManager.CALL_STATE_OFFHOOK://通话
                    System.out.println("通话状态");
                    break;
            }
            super.onCallStateChanged(state, incomingNumber);
        }
    }
}

