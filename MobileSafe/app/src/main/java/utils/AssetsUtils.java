package utils;

import android.content.Context;
import android.content.res.AssetManager;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

//里面就一个方法，用来把assets文件里面的数据库拷贝到sd卡里
public class AssetsUtils {
    public static void open(Context context,String fileName){
        //getFilesDir()方法用于获取/data/data//files目录
        File file = new File(context.getFilesDir(), fileName);
        if (file.exists()){
            return;
        }
        //开启第三方的资产目录
        AssetManager assets = context.getAssets();
        InputStream inputStream = null;
        FileOutputStream fileOutputStream = null;
        try {
            inputStream = assets.open("antivirus.db");
            //3.写入指定路径的文件
            fileOutputStream = new FileOutputStream(file);
            int len = 0;
            byte[] buffer = new byte[1024];
            while ((len = inputStream.read(buffer))!=-1){
                fileOutputStream.write(buffer,0,len);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            StreamUtils.closeStream(inputStream,fileOutputStream);
        }
    }
}
