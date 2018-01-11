package cn.edu.wang.DataAnalysis;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.MappedByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.stream.Stream;

public class CSVUtil {
    /**
     * 读取CSV文件
     */
    public static ArrayList<CallDataModel> ReadCsvFromFile(String filePath) {
        try {

            ArrayList<CallDataModel> dataList = new ArrayList<CallDataModel>(); //用来保存数据
            //String csvFilePath = "d:/1-tb_call_201202_random.txt";
            CsvReader reader = new CsvReader(filePath, '\t', Charset.forName("UTF-8"));    //一般用这编码读就可以了

            //reader.readHeaders(); // 跳过表头   如果需要表头的话，不要写这句。

            while (reader.readRecord()) { //逐行读入除表头的数据、
                CallDataModel d = new CallDataModel();
                String[] values = reader.getValues();
                if(values.length != 14) continue;
                d.day_id = values[0];
                d.calling_nbr = values[1];
                d.called_nbr = values[2];
                int i = Integer.valueOf(values[3]);
                d.calling_optr = MobileOperator.FromInteger(i);
                d.called_optr = MobileOperator.FromInteger(Integer.valueOf(values[4]));
                d.calling_city = values[5];
                d.called_city = values[6];
                d.calling_roam_city = values[7];
                d.called_roam_city = values[8];
                d.start_time = values[9];
                d.end_time = values[10];
                d.raw_dur = values[11];
                d.call_type = CallType.FromInteger(Integer.valueOf(values[12]));
                d.calling_cell = values[13];
                dataList.add(d);
            }
            reader.close();
            return dataList;
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    public static ArrayList<CallDataModel> ReadCsvFromBytes(byte[] bytes) {
        try {

            ArrayList<CallDataModel> dataList = new ArrayList<>(); //用来保存数据
            //String csvFilePath = "d:/1-tb_call_201202_random.txt";
            CsvReader reader = new CsvReader(new ByteArrayInputStream(bytes), '\t', Charset.forName("UTF-8"));    //一般用这编码读就可以了

            //reader.readHeaders(); // 跳过表头   如果需要表头的话，不要写这句。

            while (reader.readRecord()) { //逐行读入除表头的数据、
                CallDataModel d = new CallDataModel();
                String[] values = reader.getValues();
                d.day_id = values[0];
                d.calling_nbr = values[1];
                d.called_nbr = values[2];
                int i = Integer.valueOf(values[3]);
                d.calling_optr = MobileOperator.FromInteger(i);
                d.called_optr = MobileOperator.FromInteger(Integer.valueOf(values[4]));
                d.calling_city = values[5];
                d.called_city = values[6];
                d.calling_roam_city = values[7];
                d.called_roam_city = values[8];
                d.start_time = values[9];
                d.end_time = values[10];
                d.raw_dur = values[11];
                d.call_type = CallType.FromInteger(Integer.valueOf(values[12]));
                d.calling_cell = values[13];
                dataList.add(d);
            }
            reader.close();
            return dataList;
        } catch (Exception ex) {
            System.out.println(ex);
            return null;
        }
    }

    //分割一个文本文件 返回分割的文件路径数组
    public static LinkedList<String> Split(String path, String fileName, String extension, int count) {
        System.out.println("Start Split");
        try {
           // FileReader read = new FileReader(path + fileName + "." + extension);
            InputStreamReader isr = new InputStreamReader(new FileInputStream(path + fileName + "." + extension), "UTF-8");
            BufferedReader read = new BufferedReader(isr);
            BufferedReader br = new BufferedReader(read);
            String row;
            List<FileWriter> flist = new ArrayList<FileWriter>();
            LinkedList<String> fPath = new LinkedList<>();
            for (int i = 0; i < count; i++) {
                flist.add(new FileWriter(path + fileName + "_" + i + "." + extension));
                fPath.add(path + fileName + "_" + i + "." + extension);
            }
           // Stream<String> lines = br.lines();
           // long totalLines = lines.count();
           // long avgLine = totalLines / count;
            int rownum = 0;// 计数器

            while ((row = br.readLine()) != null) {
                flist.get(rownum % count).append(row + "\r\n");
                rownum++;
            }
            System.out.println("End Split");

            return fPath;
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * the traditional io way
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray(String filename) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        ByteArrayOutputStream bos = new ByteArrayOutputStream((int) f.length());
        BufferedInputStream in = null;
        try {
            in = new BufferedInputStream(new FileInputStream(f));
            int buf_size = 1024;
            byte[] buffer = new byte[buf_size];
            int len = 0;
            while (-1 != (len = in.read(buffer, 0, buf_size))) {
                bos.write(buffer, 0, len);
            }
            return bos.toByteArray();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                in.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            bos.close();
        }
    }


    /**
     * NIO way
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] toByteArray2(String filename) throws IOException {

        File f = new File(filename);
        if (!f.exists()) {
            throw new FileNotFoundException(filename);
        }

        FileChannel channel = null;
        FileInputStream fs = null;
        try {
            fs = new FileInputStream(f);
            channel = fs.getChannel();
            ByteBuffer byteBuffer = ByteBuffer.allocate((int) channel.size());
            while ((channel.read(byteBuffer)) > 0) {
                // do nothing
//              System.out.println("reading");
            }
            return byteBuffer.array();
        } catch (IOException e) {
            e.printStackTrace();
            throw e;
        } finally {
            try {
                channel.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
            try {
                fs.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }


    /**
     * Mapped File  way
     * MappedByteBuffer 可以在处理大文件时，提升性能
     *
     * @param filename
     * @return
     * @throws IOException
     */
    public static byte[] ToByteArray3(String filename) {

        FileChannel fc = null;
        try {
            fc = new RandomAccessFile(filename, "r").getChannel();
            MappedByteBuffer byteBuffer = fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size()).load();
            System.out.println(byteBuffer.isLoaded());
            byte[] result = new byte[(int) fc.size()];
            if (byteBuffer.remaining() > 0) {
//              System.out.println("remain");
                byteBuffer.get(result, 0, byteBuffer.remaining());
            }
            return result;
        } catch (IOException e) {
            e.printStackTrace();

        } finally {
            try {
                fc.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return null;
    }
}
