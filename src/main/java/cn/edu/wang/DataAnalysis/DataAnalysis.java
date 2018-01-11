package cn.edu.wang.DataAnalysis;


import com.csvreader.CsvWriter;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;

import java.io.*;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.*;

import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.util.stream.Collectors;


public abstract class DataAnalysis<T> {
    //计算任务的ID
    public final int ID;

    //待汇总的文件类型
    public T CollectedResult;

    //客户端执行的计算任务，计算完毕返回类型T
    public abstract T Compute(String filePath);

    //服务器端将客户端传来的数据T汇总
    public abstract void Collect(T small);

    //服务器端执行最后的计算输出
    public abstract void Final();
    public String OutputFilePath;
    public DataAnalysis(int id)
    {
        ID = id;
    }
}