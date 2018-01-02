package cn.edu.wang.fileUtils;

import java.io.*;

/**
 * Created by wangdechang on 2018/1/2
 */
public class Utils {
    public static void close(BufferedOutputStream outputStream){
        if (outputStream != null){
            try {
                outputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(InputStream inputStream){
        if(inputStream != null){
            try {
                inputStream.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public static void close(RandomAccessFile randomAccessFile,BufferedOutputStream outputStream) throws IOException {
        if (randomAccessFile != null){
            randomAccessFile.close();
        }
        close(outputStream);

    }
}
