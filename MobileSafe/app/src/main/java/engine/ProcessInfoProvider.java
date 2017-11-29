package engine;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.pm.ProviderInfo;
import android.content.pm.ServiceInfo;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Debug;

import com.itheima.mobilesafe.R;

import java.io.BufferedReader;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import bean.ProcessInfo;

public class ProcessInfoProvider {

    public static List<ProcessInfo> getRunningProcessInfo(Context context){
        ArrayList<ProcessInfo> processInfoList = new ArrayList<>();
        //系统固定，获取  激活管理
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //系统固定，获取  包管理
        PackageManager pm = context.getPackageManager();
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
                  //里面是 激活管理.正在运行程序信息 应用进程信息              //系统固定，获取运行的进程，返回集合
        for (ActivityManager.RunningAppProcessInfo appProcessInfo:runningAppProcesses) {
            //process名称，就是包名
            String packageName = appProcessInfo.processName;//应用进程信息 可以点一个 进程名，得到的是包名
            //获取进程的pid
            int pid = appProcessInfo.pid;//还可以点一个pid
            String name = null;
            Drawable drawable = null;
            boolean isSys = false;
            try {
                ApplicationInfo applicationInfo = pm.getApplicationInfo(packageName, 0);//用包管理 获取应用信息，右边的看一下
                //获取图片，应用名称，系统或用户
                drawable = applicationInfo.loadIcon(pm);
                name = applicationInfo.loadLabel(pm).toString();

                //判断此进程是系统进程还是用户进程
                //1 << 0 :1左移0单位：  1扩大0倍 = 1
                //根据当前的进程的flags 和  1 进行与的运算  = 1  为系统进程。
                //比如：当前的进程flags 为 比如： FLAG_SYSTEM  = 1 << 0 其值为：1
                //1 & 1 与运算：
                // 0001
                // 0001
                //--------
                // 0001  = 1
                //不明白记着，这就是判断是否是系统进程的写法
                if ((applicationInfo.flags & ApplicationInfo.FLAG_SYSTEM)
                        == ApplicationInfo.FLAG_SYSTEM) {
                    //系统进程
                    isSys = true;
                } else {
                    //用户进程
                    isSys = false;
                }
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
                //如果此进程没有名称，则拿其包名作为应用名称
                name = packageName;
                //将手机卫士应用ic_launcher图标作为进程图标
                drawable = context.getResources().getDrawable(R.mipmap.ic_launcher);
                //将没有图标没有名称的进程作为系统进程
                isSys = true;
            }

            //通过pid获取进程使用内存数组
            int[] pids = new int[]{pid};
            Debug.MemoryInfo[] processMemoryInfo = am.getProcessMemoryInfo(pids);
            //获取pid指向进程使用内存大小，单位kB
            int totalPss = processMemoryInfo[0].getTotalPss()*1024;//单位kB == 1024Byte

            //将（图片，名称，包名，系统用户，内存）存储在javabean（ProcessInfo）
            ProcessInfo processInfo = new ProcessInfo(name, packageName, totalPss, drawable, isSys);
            processInfoList.add(processInfo);

        }
        return processInfoList;
    }


    public static void killProcess(Context context, String packageName) {
        //1.activityManager上杀死进程的方法
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.杀死指定包名的进程   权限
        am.killBackgroundProcesses(packageName);
    }


    /**
     * @param context   杀死手机所有的进程
     */
    public static void killAllProcess(Context context) {
        //1.获取ActivityManager对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取正在运行进程数
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses
                = am.getRunningAppProcesses();
        for (ActivityManager.RunningAppProcessInfo processInfo: runningAppProcesses) {
            if (!processInfo.processName.equals(context.getPackageName())){
                am.killBackgroundProcesses(processInfo.processName);
            }
        }
    }

    //获取正在运行的进程数量
    public static int getRunningProcess(Context context){
        //1.获取ActivityManager对象
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //2.获取正在运行进程数
        List<ActivityManager.RunningAppProcessInfo> runningAppProcesses = am.getRunningAppProcesses();
        //3.获取正在运行进程数量
        return runningAppProcesses.size();
    }

    //可以被开启的所有进程数量，有点固定写法的意思
    public static int getAllProcess(Context context){
        //1.安装在手机上的所有的应用PackageManager
        PackageManager packageManager = context.getPackageManager();//1、包管理器
        List<PackageInfo> installedPackageList = packageManager.getInstalledPackages(//2、包管理器可以获取包信息
                PackageManager.GET_ACTIVITIES | PackageManager.GET_SERVICES
                        | PackageManager.GET_RECEIVERS | PackageManager.GET_PROVIDERS);

        //自动过滤相同的字符串
        HashSet<String> hashSet = new HashSet<>();
        //2.循环遍历手机安装过的应用，应用和包相对应，一个应用可能对应了多个包
        for (PackageInfo packageInfo: installedPackageList) {
            //在application中配置的process的属性内容
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;//3、包信息又可以获取应用信息
            String appProcessName = applicationInfo.processName;//4、应用信息又可以获取进程名
            hashSet.add(appProcessName);

            //获取多个activity中配置的process属性
            ActivityInfo[] activities = packageInfo.activities;//3.包信息还可以获取激活信息
            if (activities!=null){
                for (ActivityInfo activityInfo: activities) {
                    String activityProcessName = activityInfo.processName;//4.激活信息也可以获取进程名
                    hashSet.add(activityProcessName);//如果同名就添加不进去了
                }
            }
            //获取多个service中配置的process属性
            ServiceInfo[] services = packageInfo.services;//3。包信息还可以获取服务信息
            if (services!=null){
                for (ServiceInfo serviceInfo: services) {
                    String serviceProcessName = serviceInfo.processName;//4。服务信息也可以获取进程名
                    hashSet.add(serviceProcessName);
                }
            }
            //获取多个receiver中配置的process属性
            ActivityInfo[] receivers = packageInfo.receivers;
            if (receivers!=null){
                for (ActivityInfo activityInfo: receivers) {
                    String receiverProcessName = activityInfo.processName;
                    hashSet.add(receiverProcessName);
                }
            }
            //获取多个provider中配置的process属性
            ProviderInfo[] providers = packageInfo.providers;
            if (providers!=null){
                for (ProviderInfo providerInfo: providers) {
                    String providerProcessName = providerInfo.processName;
                    hashSet.add(providerProcessName);
                }
            }
        }
        return hashSet.size();
    }

    //获取占用内存大小
    public static long getAvailMemory(Context context){
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //执行完了以下代码，memoryInfo对象就被赋值了
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        //获取可用内存大小
        return memoryInfo.availMem;//草泥马各种螺旋嵌套
    }

    //获取系统总内存大小
    public static long getAllMemory(Context context){
        //activityManager
        ActivityManager am = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //执行完了以下代码，memoryInfo对象就被赋值了
        ActivityManager.MemoryInfo memoryInfo = new ActivityManager.MemoryInfo();
        am.getMemoryInfo(memoryInfo);
        //手机内存的总大小

        //如果现在手机的编译版本高于等于16，则可以使用以下的api获取内存的总大小
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            return memoryInfo.totalMem;
        }else{
            //低于16版本sdk，获取内存总大小的逻辑
            return getAllMemoryRow();
        }
    }

    /**
     * @return 在低版本手机上获取手机的总内存数
     * 返回单位  byte
     */
    private static long getAllMemoryRow() {
        //手机中有一个叫做 proc/meminfo---->存储了手机的内存大小
        //在低版本的手机上，读取proc文件夹中 的meminfo中存储手机内存大小的信息，获取手机内存大小值
        BufferedReader bufferedReader = null;
        String lineOneContent = "";
        try {
            bufferedReader = new BufferedReader(new FileReader("proc/meminfo"));
            lineOneContent = bufferedReader.readLine();
            String strMemoryInfo = lineOneContent.replace("MemTotal:","").replace("kB","").trim();
            //kB = 1024 byte
            return Long.parseLong(strMemoryInfo)*1024;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }
}
