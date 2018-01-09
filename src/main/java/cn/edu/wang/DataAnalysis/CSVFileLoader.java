package cn.edu.wang.DataAnalysis;

import com.csvreader.CsvReader;
import com.csvreader.CsvWriter;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.ArrayList;

public class CSVFileLoader {
    /**
     * 读取CSV文件
     */
    public static  ArrayList<CallDataModel> ReadCsvFromFile(String filePath){
        try {

            ArrayList<CallDataModel> dataList = new ArrayList<CallDataModel>(); //用来保存数据
            //String csvFilePath = "d:/1-tb_call_201202_random.txt";
            CsvReader reader = new CsvReader(filePath,'\t', Charset.forName("UTF-8"));    //一般用这编码读就可以了

            //reader.readHeaders(); // 跳过表头   如果需要表头的话，不要写这句。

            while(reader.readRecord()){ //逐行读入除表头的数据、
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
        }catch(Exception ex){
            System.out.println(ex);
            return null;
        }
    }

    public static  ArrayList<CallDataModel> ReadCsvFromString(String content){
        try {

            ArrayList<CallDataModel> dataList = new ArrayList<>(); //用来保存数据
            //String csvFilePath = "d:/1-tb_call_201202_random.txt";
            CsvReader reader = new CsvReader( new ByteArrayInputStream(content.getBytes("UTF-8")),'\t', Charset.forName("UTF-8"));    //一般用这编码读就可以了

            //reader.readHeaders(); // 跳过表头   如果需要表头的话，不要写这句。

            while(reader.readRecord()){ //逐行读入除表头的数据、
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
        }catch(Exception ex){
            System.out.println(ex);
            return null;
        }
    }
}
