package utils;

import java.io.Closeable;
import java.io.IOException;

/**
 * Created by Rayn on 2017/5/21 0021.
 */

public class StreamUtils {
    public static void closeStream(Closeable... closeables){
        if (closeables!=null){
            for (Closeable closable:closeables) {
                if (closable!=null){
                    try {
                        closable.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
    }
}
