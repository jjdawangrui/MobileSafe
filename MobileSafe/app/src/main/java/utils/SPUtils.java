package utils;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Rayn on 2017/5/20 0020.
 */

public class SPUtils {
    private static SharedPreferences sp;
    public static void saveBoolean(Context context,String key,boolean value){
        if (sp==null){
            sp = context.getSharedPreferences("config",Context.MODE_PRIVATE);//这个就是之前的0
        }
        sp.edit().putBoolean(key,value).commit();
    }
    public static boolean getBoolean(Context context,String key,boolean defValue){
        if (sp == null){
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getBoolean(key,defValue);
    }

    /**
     * @param context   上下文环境
     * @param key   键
     * @param value 存储的值
     *              保存int类型的值
     */
    public static void saveInt(Context context,String key,int value) {
        if (sp == null){
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        sp.edit().putInt(key,value).commit();
    }

    /**
     * @param context
     * @param key
     * @param defValue
     * @return
     */
    //如果通过key取不到drawables的图片索引值，那就用后面那个默认的值
    public static int getInt(Context context,String key,int defValue) {
        if (sp == null){
            sp = context.getSharedPreferences("config", Context.MODE_PRIVATE);
        }
        return sp.getInt(key,defValue);
    }
}
