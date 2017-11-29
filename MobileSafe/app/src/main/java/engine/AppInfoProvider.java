package engine;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;


import java.util.ArrayList;
import java.util.List;

import bean.AppInfo;

//就一个方法，获取所有app信息的的集合
public class AppInfoProvider {
    public static List<AppInfo> getAppInfoList(Context context){
        ArrayList<AppInfo> appInfoList = new ArrayList<>();
        //包管理，通过上下文来获取
        PackageManager pm = context.getPackageManager();
        //包信息是系统的，通过包管理获取安装的包，0是获取所有包
        List<PackageInfo> installedPackages = pm.getInstalledPackages(0);

        for (PackageInfo packageInfo: installedPackages) {
            //用包信息来获取包名String
            String packageName = packageInfo.packageName;
            //包信息还能获取应用信息
            ApplicationInfo applicationInfo = packageInfo.applicationInfo;
            //应用信息加载图片，里面传包管理
            Drawable drawable = applicationInfo.loadIcon(pm);
            //应用信息加载标签，也是传包管理，转成String，获取应用名
            String name = applicationInfo.loadLabel(pm).toString();
            AppInfo appInfo = new AppInfo(packageName, drawable, name);
            appInfoList.add(appInfo);
        }
        return appInfoList;
    }
}
