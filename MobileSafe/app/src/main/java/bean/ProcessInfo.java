package bean;

import android.graphics.drawable.Drawable;

/**
 * Created by HASEE.
 */

public class ProcessInfo {
    private String name;    //进程所在应用的名称
    private String packageName;//进程对应的包名
    private int memorySize;//进程占用的内存大小
    private Drawable drawable;//进程使用的图标
    private boolean isSys;//是否是系统进程（系统，用户）
    public boolean isCheck;//是否被选中，默认情况下都没有被选中  false （没选中）  true（选中了）

    public ProcessInfo(String name, String packageName, int memorySize,
                       Drawable drawable, boolean isSys) {
        this.name = name;
        this.packageName = packageName;
        this.memorySize = memorySize;
        this.drawable = drawable;
        this.isSys = isSys;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public int getMemorySize() {
        return memorySize;
    }

    public void setMemorySize(int memorySize) {
        this.memorySize = memorySize;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public boolean isSys() {
        return isSys;
    }

    public void setSys(boolean sys) {
        isSys = sys;
    }

    public boolean isCheck() {
        return isCheck;
    }

    public void setCheck(boolean check) {
        isCheck = check;
    }
}
