package db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by HASEE.
 */

public class VirusDao {
    /**
     * @param ctx   上下文环境
     * @param md5   应用签名文件生成的md5码
     * @return      true 是病毒    false 不是病毒
     */
    public static boolean isVirus(Context ctx,String md5){
        //select count(*) from datable where md5=a2bd62c99207956348986bf1357dea01;
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(
                ctx.getFilesDir() + File.separator + "antivirus.db"
                , null, SQLiteDatabase.OPEN_READONLY);
        Cursor cursor = sqLiteDatabase.rawQuery(
                "select count(*) from datable where md5 = ?", new String[]{md5});
        int count = 0;
        if (cursor!=null){
            if (cursor.moveToNext()){
                count = cursor.getInt(0);
            }
            cursor.close();
        }
        sqLiteDatabase.close();
        return count>0;
    }
}
