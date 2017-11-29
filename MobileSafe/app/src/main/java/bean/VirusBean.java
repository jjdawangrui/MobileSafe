package bean;

import android.graphics.drawable.Drawable;

//病毒的业务类
public class VirusBean {
    private String name;
    private Drawable drawable;
    private String packageName;
    private boolean isVirus;

    public VirusBean(String name, Drawable drawable, String packageName, boolean isVirus) {
        this.name = name;
        this.drawable = drawable;
        this.packageName = packageName;
        this.isVirus = isVirus;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Drawable getDrawable() {
        return drawable;
    }

    public void setDrawable(Drawable drawable) {
        this.drawable = drawable;
    }

    public String getPackageName() {
        return packageName;
    }

    public void setPackageName(String packageName) {
        this.packageName = packageName;
    }

    public boolean isVirus() {
        return isVirus;
    }

    public void setVirus(boolean virus) {
        isVirus = virus;
    }
}
