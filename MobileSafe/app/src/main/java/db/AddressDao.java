package db;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;

import java.io.File;

/**
 * Created by HASEE.
 */

public class AddressDao {

    private static String address = "未知号码";

    /**
     * @param number    电话号码（手机号，座机号）
     * @param context
     * @return
     */
    public static String getAddress(String number,Context context){
        SQLiteDatabase sqLiteDatabase = SQLiteDatabase.openDatabase(
                context.getFilesDir() + File.separator + "address.db"
                , null, SQLiteDatabase.OPEN_READONLY);
        //通过正则表达式判断number是否是手机号码
        //手机号码的第1位一定是1:^1
        //手机号码的第2位可以是3,4,5,6,7,8  [3,4,5,6,7,8]
        //手机号码后面的9位必须是0-9数字 \d{9}$
        String regex = "^1[3,4,5,6,7,8]\\d{9}";
        if (number.matches(regex)){
            //返回true是手机号码
            number = number.substring(0,7);

            Cursor cursor = sqLiteDatabase.rawQuery(
                    "select location from data2 where id = (select outkey from data1 where id = ?)",
                    new String[]{number});
            if (cursor.moveToNext()){
                address = cursor.getString(0);
            }
            cursor.close();
        }else{
            //不是手机号码，座机号码
            switch (number.length()){
                case 3:     //110 119
                    address = "报警电话";
                    break;
                case 4:
                    address = "模拟器";
                    break;
                case 5:
                    address = "运营商";
                    break;
                case 7:
                    address = "本地座机";
                    break;
                case 8:
                    address = "本地座机";
                    break;
                case 11:// 3(区号)+8(电话号码长度)  010 66778899
                    String area = number.substring(1, 3);
                    Cursor cursor = sqLiteDatabase.rawQuery("select location from data2 where area = ?"
                            , new String[]{area});
                    if (cursor.moveToNext()){
                        address = cursor.getString(0);
                    }
                    cursor.close();
                    break;
                case 12:// 4(区号)+8(电话号码长度) 0791 66778899
                    String area1 = number.substring(1, 4);
                    Cursor cursor1 = sqLiteDatabase.rawQuery("select location from data2 where area = ?"
                            , new String[]{area1});
                    if (cursor1.moveToNext()){
                        address = cursor1.getString(0);
                    }
                    cursor1.close();
                    break;
                default:
                    address = "未知号码";
                    break;
            }
        }
        return address;
    }
}
