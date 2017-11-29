package bean;

import android.graphics.drawable.Drawable;

//app有包名，还有图标，还有自己的名
public class AppInfo {
    private String packageName;
    private Drawable drawable;
    private String name;

    public AppInfo(String packageName, Drawable drawable, String name) {
        this.packageName = packageName;
        this.drawable = drawable;
        this.name = name;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}
