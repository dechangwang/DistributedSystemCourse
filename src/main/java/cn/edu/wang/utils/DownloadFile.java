package cn.edu.wang.utils;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

/**
 * Created by wangdechang on 2018/1/10
 */
public class DownloadFile {

    public static boolean downloadFile(String urlString,String dest){
        URL url = null;
        try {
            url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream(dest);
            int len = -1;
            byte[] bytes = new byte[1024];
            while ( (len = is.read(bytes)) != -1) {
                os.write(bytes,0,len);
            }
            os.close();
            is.close();
            return true;
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;

    }

    public static void main(String[] args) {

        String urlString = "http://127.0.0.1:7878/config.properties";
        try {
            URL url = new URL(urlString);
            URLConnection conn = url.openConnection();
            InputStream is = conn.getInputStream();
            OutputStream os = new FileOutputStream("D:\\connfig.properties");
            int len = -1;
            byte[] bytes = new byte[1024];
            while ( (len = is.read(bytes)) != -1) {
                os.write(bytes,0,len);
            }
            os.close();
            is.close();
        } catch (MalformedURLException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

}
