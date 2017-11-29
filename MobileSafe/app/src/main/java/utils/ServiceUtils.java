package utils;

import android.app.ActivityManager;
import android.content.ComponentName;
import android.content.Context;

import java.util.List;

/**
 * Created by Rayn on 2017/5/22 0022.
 */

//这个方法是将LocationService中的服务与系统的所有服务比对，有就true
public class ServiceUtils {
    public static boolean isRunning(Context context,Class clazz){//后面参数就是需要判断的服务的字节码
        //1.activityManager获取手机上的所有正在运行的服务
        //2.拿到所有的正在运行服务循环遍历，和传递进来服务的类名进行比较
        //2.1   如果有一致的，则说明传递进来的服务是开启的
        //2.2   否则是关闭的
        ActivityManager activityManager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        //尝试从手机上获取1000个服务，如果手机上没有这么多正在运行的服务，则会把所有运行的服务返回
        List<ActivityManager.RunningServiceInfo> runningServices = activityManager.getRunningServices(1000);
        for (ActivityManager.RunningServiceInfo runningService:runningServices) {
            //获取每一个正在运行服务的名称
            ComponentName service = runningService.service;//正在运行的服务，点一个服务，才尼玛是服务
            String serviceName = service.getClassName();//然后再getClassName才是服务的名字
            //通过类的字节码文件，获取服务的类名
            String className = clazz.getName();
            if (serviceName.equals(className)){
                return true;
            }
        }
        return false;
    }
}
