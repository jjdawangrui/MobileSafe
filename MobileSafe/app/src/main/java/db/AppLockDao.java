package db;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.util.ArrayList;
import java.util.List;


//增删改查
public class AppLockDao {

    private Context mCtx;
    private AppLockOpenHelper appLockOpenHelper;

    public AppLockDao(Context ctx) {
        this.mCtx = ctx;
        appLockOpenHelper = new AppLockOpenHelper(ctx);
    }

    /**
     * @param packageName
     * @return  true 成功     false 失败
     */
    public boolean insert(String packageName){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        ContentValues contentValues = new ContentValues();
        contentValues.put(DBConstant.COLUMN_PACKAGENAME,packageName);
        long result = db.insert(DBConstant.TABLE_NAME, null, contentValues);
        db.close();
        return result != -1;
    }

    /**
     * @param packageName   删除数据库中此包名应用
     * @return  返回true代表删除成功    false删除失败
     */
    public boolean delete(String packageName){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        String whereClause = DBConstant.COLUMN_PACKAGENAME + " = ?";
        int num = db.delete(DBConstant.TABLE_NAME, whereClause, new String[]{packageName});
        db.close();


        return num>0;
    }

    /**
     * @return 查询数据库中所有数据
     */
    public List<String> queryAll(){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        List<String> packageNameList = new ArrayList<>();
        //select packagename from locklist;
        Cursor cursor = db.rawQuery("select " + DBConstant.COLUMN_PACKAGENAME + " from "
                + DBConstant.TABLE_NAME, null);
        if (cursor!=null){
            while (cursor.moveToNext()){
                String packageName = cursor.getString(0);//每个游标0位置是包名
                packageNameList.add(packageName);
            }
            cursor.close();
        }
        db.close();
        return  packageNameList;
    }

    /**
     * @param packageName   查询packageName包名是否在数据库中
     * @return  是否是加锁应用     true是   false 不是
     */
    public boolean queryPackageName(String packageName){
        SQLiteDatabase db = appLockOpenHelper.getWritableDatabase();
        //select packagename from locklist where packagename = ?;
        Cursor cursor = db.rawQuery("select " + DBConstant.COLUMN_PACKAGENAME + " from "
                + DBConstant.TABLE_NAME + " where " + DBConstant.COLUMN_PACKAGENAME
                + " = ?", new String[]{packageName});
        boolean b = false;
        if (cursor!=null){
            if (cursor.moveToNext()){
                b = true;
            }
            cursor.close();
        }
        db.close();
        return b;
    }

}
