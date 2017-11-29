package utils;

import android.annotation.TargetApi;
import android.app.Activity;
import android.app.AppOpsManager;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Binder;
import android.os.Build;
import android.os.Environment;
import android.os.SystemClock;
import android.util.Log;
import android.widget.Toast;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.lang.reflect.Type;
import java.util.ArrayList;
import java.util.List;
import bean.SmsInfo;

public class SmsUtil {
    public static void backup (final Activity ctx, final OnBackupListener backupListener) {
        new Thread() {
            @Override
            public void run() {
                //进度显示
                //100条短信
                //备份了35条 ，进度条显示成35%
                //短信备份过程中出现异常，告知用户有异常
                //短信备份完成，告知用户备份完成，请继续使用
                Cursor cursor = null;
                FileWriter fileWriter = null;
                try {
                    //1.获取内存解析器
                    ContentResolver contentResolver = ctx.getContentResolver();
                    //2.提供需要从数据库中查询到的字段信息
                    String[] strings = {"address", "date", "read", "type", "body"};
                    cursor = contentResolver.query(Uri.parse("content://sms"), strings, null, null, null);
                    //指定短信总条数进度条100%时数值
//                    progressBar.setMax(cursor.getCount());
                    backupListener.setMax(cursor.getCount());//游标的总数，也就是短信的总条数

                    List<SmsInfo> smsInfoList = new ArrayList<>();
                    int tempCount = 0;

                    while (cursor.moveToNext()) {
                        SystemClock.sleep(300);
                        String address = cursor.getString(0);
                        int date = cursor.getInt(1);
                        int read = cursor.getInt(2);
                        int type = cursor.getInt(3);
                        String body = cursor.getString(4);

                        SmsInfo smsInfo = new SmsInfo(address, date, read, type, body);

                        smsInfoList.add(smsInfo);

                        tempCount++;
                        //progressDialog中不需要自己计算百分比，只需要指定最大值和当前所处的值
                        //系统代码会帮组我们计算百分比
//                        progressBar.setProgress(tempCount);//tempCount*100/totalCount
                        backupListener.setCurrentProgress(tempCount);
                    }
                    //短信的内容已经存储在smsInfoList集合中，将此集合中的数据存储在sd卡文件中去
                    //xml  json 形式存储短信数据   Gson
                    Gson gson = new Gson();
                    //将集合转换成json串
                    String json = gson.toJson(smsInfoList);

                    if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)) {
                        //获取sd卡路径，写入json串
                        String path = Environment.getExternalStorageDirectory().getPath() + File.separator + "sms.json";
                        File file = new File(path);
                        fileWriter = new FileWriter(file);
                        fileWriter.write(json);
                        fileWriter.flush();

                        ctx.runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                backupListener.onSuccessed();
                            }
                        });
                    }
                } catch (Exception e) {
                    e.printStackTrace();

                    ctx.runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            backupListener.onFail();
                        }
                    });
                } finally {
                    if (cursor != null) {
                        cursor.close();
                    }
                    StreamUtils.closeStream(fileWriter);
                }
            }
        }.start();
    }

    //1.定义一个接口（接口中未实现的方法）
    //2.分析工具类中的相关业务，找到不确定性的因素,几个因素对应几个方法
    //2.1 显示总进度的控件未知（ProgressDialog  ProgressBar）
    //2.2 显示当前进度地图控件未知
    //3.在工具类中传递一个实现了onBackupListener接口实现类对象进来
    //4.在合适的地方，调用第3个步骤中实现了onBackupListener接口的类的对象身上的相关方法
    public interface OnBackupListener {
        public void setMax(int maxProgress);

        public void setCurrentProgress(int currentProgress);

        public void onSuccessed();

        public void onFail();
    }


    public static void restore(final Activity ctx,final OnBackupListener backupListener){

//        checkMode(ctx);
//        setMode(ctx);

        new Thread(){
            @Override
            public void run() {

                Uri uri = Uri.parse("content://sms/");

                //1.创建内容解析器对象
                ContentResolver contentResolver = ctx.getContentResolver();
                //2.解析sd卡中的json，将其转换成集合，把集合中的每个对象的数据，插入数据中
                if (Environment.getExternalStorageState().equals(Environment.MEDIA_MOUNTED)){
                    FileReader fileReader = null;
                    try {
                        //获取sd卡路径，写入json串
                        String path = Environment.getExternalStorageDirectory().getPath()+ File.separator+ "sms.json";
                        File file = new File(path);
                        fileReader = new FileReader(file);
                        //Gson
                        Gson gson = new Gson();
                        Type type = new TypeToken<ArrayList<SmsInfo>>(){}.getType();
                        //参数一：读取文件的流
                        //参数二：type即为json最终需要转换成的类型
                        ArrayList<SmsInfo> smsInfoList = gson.fromJson(fileReader, type);
                        if (backupListener!=null){
                            backupListener.setMax(smsInfoList.size());
                        }
                        int tempCount = 0;
                        for (SmsInfo smsInfo: smsInfoList) {
                            SystemClock.sleep(300);
                            ContentValues contentValues = new ContentValues();
                            contentValues.put("address",smsInfo.getAddress());
                            contentValues.put("date",smsInfo.getDate());
                            contentValues.put("read",smsInfo.getRead());
                            contentValues.put("type",smsInfo.getType());
                            contentValues.put("body",smsInfo.getBody());

                            contentResolver.insert(uri,contentValues);

                            tempCount++;
                            if (backupListener!=null){
                                backupListener.setCurrentProgress(tempCount);
                            }
                        }
                        if (backupListener!=null){
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    backupListener.onSuccessed();
                                }
                            });
                        }
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();

                        if (backupListener!=null){
                            ctx.runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    backupListener.onFail();
                                }
                            });
                        }
                    }finally {
                        StreamUtils.closeStream(fileReader);
                    }
                }
            }
        }.start();
    }

    @TargetApi(Build.VERSION_CODES.KITKAT)
    public static  int checkMode(Context context){
        AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        Class c = appOps.getClass();
        try {
            Class[] cArg = new Class[3];
            cArg[0] = int.class;
            cArg[1] = int.class;
            cArg[2] = String.class;
            Method lMethod = c.getDeclaredMethod("checkOp", cArg);
            return (Integer) lMethod.invoke(appOps, 15, Binder.getCallingUid(), context.getPackageName());
        } catch(NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public static boolean setMode(Context context){
        AppOpsManager appOps = (AppOpsManager)context.getSystemService(Context.APP_OPS_SERVICE);
        Class c = appOps.getClass();
        Class[] cArg = new Class[4];
        cArg[0] = int.class;
        cArg[1] = int.class;
        cArg[2] = String.class;
        cArg[3] = int.class;
        Method lMethod;
        try {
            lMethod = c.getDeclaredMethod("setMode", cArg);
            lMethod.invoke(appOps, 15, Binder.getCallingUid(), context.getPackageName(),AppOpsManager.MODE_ALLOWED);
            return true;
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
        return false;
    }

}
