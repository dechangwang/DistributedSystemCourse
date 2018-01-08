package cn.edu.wang.DataAnalysis;


import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;

import java.util.stream.Collectors;

public class DataAnalysis {

    //计算出用户的每日平均通话次数，并将结果以<主叫号码, 每日平均通话
    //次数>的格式保存成 txt 或 excel 文件
    public static void ProcessOne(String processFile, String outputFile)
    {
        ArrayList<CallDataModel> datas = CSVFileLoader.ReadCsv(processFile);

        ArrayList<Pair<String, Float>> UserCallTimePair = new ArrayList<>();
        Map<String, ArrayList<CallDataModel>> distinctUsers =
                datas.parallelStream().collect(Collectors.groupingBy((CallDataModel x)->x.calling_nbr,Collectors.toCollection(ArrayList::new)));

        for(Map.Entry<String, ArrayList<CallDataModel>> distinctUser : distinctUsers.entrySet())
        {
            Map<String, Long> CallTimePerDay =
                    distinctUser.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x)->x.day_id, Collectors.counting()));

            Float avg = new Float(CallTimePerDay.values().stream().reduce(new Long(0), Long::sum)) / CallTimePerDay.values().size();
            UserCallTimePair.add(new Pair<>(distinctUser.getKey(), avg));

        }

        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(outputFile, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = { "主叫号码", "每日平均通话次数" };
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for(Pair<String, Float> pair : UserCallTimePair)
            {
                String[] csvLine = {pair.GetFirst(), pair.GetSecond().toString()};
                csvWriter.writeRecord(csvLine);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    //计算出不同通话类型（市话、长途、国际）下各个运营商（移动，联通，
    //电信）的占比，并画出饼状图
    public static void ProcessTwo(String processFile) {
        ArrayList<CallDataModel> datas = CSVFileLoader.ReadCsv(processFile);

        Map<CallType, ArrayList<CallDataModel>> distinctUsers =
                datas.parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.call_type, Collectors.toCollection(ArrayList::new)));

        EnumMap<CallType, Map<MobileOperator, Float>> result = new EnumMap<>(CallType.class);

        for (Map.Entry<CallType, ArrayList<CallDataModel>> eachCallType : distinctUsers.entrySet()) {
            Map<MobileOperator, Long> CallingCountPerOptr =
                    eachCallType.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.calling_optr, Collectors.counting()));
            Map<MobileOperator, Long> CalledCountPerOptr =
                    eachCallType.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.called_optr, Collectors.counting()));

            Long total = CallingCountPerOptr.values().stream().reduce(new Long(0), Long::sum);
            total += CalledCountPerOptr.values().stream().reduce(new Long(0), Long::sum);


            Map<MobileOperator, Long> TotalCountPerOptr = new EnumMap<>(MobileOperator.class);
            Map<MobileOperator, Float> resultPerOptr = new EnumMap<>(MobileOperator.class);

            //上面那个map合并到一个新的map中
            TotalCountPerOptr.putAll(CallingCountPerOptr);
            CalledCountPerOptr.forEach((optr, count) -> TotalCountPerOptr.merge(optr, count, (value, newValue) -> value + newValue));


            resultPerOptr.put(MobileOperator.Mobile, new Float(TotalCountPerOptr.get(MobileOperator.Mobile))/total);
            resultPerOptr.put(MobileOperator.Unicom, new Float(TotalCountPerOptr.get(MobileOperator.Unicom))/total);
            resultPerOptr.put(MobileOperator.Telecom, new Float(TotalCountPerOptr.get(MobileOperator.Telecom))/total);

            result.put(eachCallType.getKey(), resultPerOptr);
        }
        System.out.println("市话: " + "移动: " + result.get(CallType.City).get(MobileOperator.Mobile) +
                                      "联通: " + result.get(CallType.City).get(MobileOperator.Unicom) +
                                      "电信: " + result.get(CallType.City).get(MobileOperator.Telecom));
    }
}
