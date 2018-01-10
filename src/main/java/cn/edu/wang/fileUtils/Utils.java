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

    public static void writeFile2TargetPath(String originPath,String targetPath){
        InputStream inStream = null;
        OutputStream outStream = null;

        try {

            File originFile = new File(originPath);
            File targetFile = new File(targetPath);

            inStream = new FileInputStream(originFile);
            outStream = new FileOutputStream(targetFile);

            byte[] buffer = new byte[1024];

            int length;
            //copy the file content in bytes
            while ((length = inStream.read(buffer)) > 0) {

                outStream.write(buffer, 0, length);

            }

            inStream.close();
            outStream.close();

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
