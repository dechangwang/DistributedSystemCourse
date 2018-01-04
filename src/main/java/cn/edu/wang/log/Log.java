package cn.edu.wang.log;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.OutputStreamWriter;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by wangdechang on 2017/6/9.
 */
public class Log {
    public static void log(String content) {
        SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM-dd");
        String filename = sf.format(new Date());
        try {
            File f = new File("log/" + filename + ".log");
            OutputStreamWriter write = new OutputStreamWriter(new FileOutputStream(f, true), "UTF-8");
            BufferedWriter writer = new BufferedWriter(write);
            writer.write(content + "\n");
            writer.flush();

            write.close();
            writer.close();
        } catch (Exception e) {
            System.out.println("写文件内容操作出错");
            e.printStackTrace();
        }
    }

    public static void main(String[] args) {

    }
}
