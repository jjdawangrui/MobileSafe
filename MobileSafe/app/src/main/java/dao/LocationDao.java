package dao;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by Rayn on 2017/5/22 0022.
 */

public class LocationDao {
    private static String location = "未知号码";

    public static String getLocation(String number, Context context) {
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(
                context.getFilesDir() + File.separator+ "address.db", null, SQLiteDatabase.OPEN_READONLY);
        //手机号码的第1位一定是1  ^1      反斜杠可以用File.separator来表示
        //手机号码的第2位可以是3,4,5,6,7,8    [3,4,5,6,7,8]
        //手机号码后面的9位必须是0-9数字 \d[9]$
        String regex = "^1[3,4,5,6,7,8]\\d{9}$";//两个反斜杠要转义
        if (number.matches(regex)){

            number = number.substring(0,7);//取前7位
            Cursor cursor = sqLiteDatabase.rawQuery(
                    "select location from data2 where id = (select outkey from data1 where id = ?)",new String[]{number});
            if (cursor.moveToNext()){
                location = cursor.getString(0);
            }
            cursor.close();
        }else {
            //不是手机号码
        }
        return location;
    }
}
