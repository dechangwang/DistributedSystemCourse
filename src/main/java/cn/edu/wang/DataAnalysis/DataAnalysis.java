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

public class DataAnalysis {

    //计算出用户的每日平均通话次数，并将结果以<主叫号码, 每日平均通话
    //次数>的格式保存成 txt 或 excel 文件

    //处理1 分布式部分 客户端执行
    public static HashMap<String, Pair<Long, Long>> ProcessOne_Compute(String processFile) {
        System.out.println("ProcessOne_Compute Start");

        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(processFile);

        HashMap<String, Pair<Long, Long>> UserCallTimeMap = new HashMap<>();

        //按照主叫号码 分组
        Map<String, ArrayList<CallDataModel>> distinctUsers =
                datas.parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.calling_nbr, Collectors.toCollection(ArrayList::new)));

        //对于每一个主叫号码分别计算 分布式
        for (Map.Entry<String, ArrayList<CallDataModel>> distinctUser : distinctUsers.entrySet()) {
            Map<String, Long> CallTimePerDay =
                    distinctUser.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.day_id, Collectors.counting()));

            Long sum = CallTimePerDay.values().stream().reduce(new Long(0), Long::sum);// / CallTimePerDay.values().size();
            UserCallTimeMap.put(distinctUser.getKey(), new Pair<>(sum,  new Long(CallTimePerDay.values().size())));
        }
        System.out.println("ProcessOne_Compute Finish");

        return UserCallTimeMap;
    }
    //处理1 汇总 服务器合并客户端传来的中间数据
    public static void ProcessOne_Collect(HashMap<String, Pair<Long, Long>> big, HashMap<String, Pair<Long, Long>> small, Object mutex) {
        System.out.println("ProcessOne_Collect");

        synchronized (mutex) {
            for (Map.Entry<String, Pair<Long, Long>> set : small.entrySet()) {
                big.merge(set.getKey(), set.getValue(), (oldV, newV) -> new Pair<>(oldV.GetFirst() + newV.GetFirst(), oldV.GetSecond() + newV.GetSecond()));
            }
        }
    }
    //处理1 汇总后部分，服务器端执行
    public static void ProcessOne_Final(HashMap<String, Pair<Long, Long>> totalData, String outputFile) {
        //汇总，除以总量
        System.out.println("ProcessOne_Final");

        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(outputFile, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = {"主叫号码", "每日平均通话次数"};
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for (Map.Entry<String, Pair<Long, Long>> pair : totalData.entrySet()) {
                Float avg = new Float((float)pair.getValue().GetFirst() / pair.getValue().GetSecond());
                String[] csvLine = {pair.getKey(), avg.toString()};
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
    public static void ProcessTwo(String processFile, String excelFilePath) {
        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(processFile);

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


            resultPerOptr.put(MobileOperator.Mobile, new Float(TotalCountPerOptr.get(MobileOperator.Mobile)) / total);
            resultPerOptr.put(MobileOperator.Unicom, new Float(TotalCountPerOptr.get(MobileOperator.Unicom)) / total);
            resultPerOptr.put(MobileOperator.Telecom, new Float(TotalCountPerOptr.get(MobileOperator.Telecom)) / total);

            result.put(eachCallType.getKey(), resultPerOptr);
        }

        try
        {
            InputStream is = new FileInputStream(new File(excelFilePath));
            Workbook wb = new XSSFWorkbook(is);
            is.close();
            Sheet sheet = wb.getSheetAt(0);
            sheet.getRow(1).getCell(1).setCellValue(result.get(CallType.City).get(MobileOperator.Mobile));
            sheet.getRow(2).getCell(1).setCellValue(result.get(CallType.City).get(MobileOperator.Unicom));
            sheet.getRow(3).getCell(1).setCellValue(result.get(CallType.City).get(MobileOperator.Telecom));
            sheet.getRow(1).getCell(2).setCellValue(result.get(CallType.LongDistance).get(MobileOperator.Mobile));
            sheet.getRow(2).getCell(2).setCellValue(result.get(CallType.LongDistance).get(MobileOperator.Unicom));
            sheet.getRow(3).getCell(2).setCellValue(result.get(CallType.LongDistance).get(MobileOperator.Telecom));
            sheet.getRow(1).getCell(3).setCellValue(result.get(CallType.Roaming).get(MobileOperator.Mobile));
            sheet.getRow(2).getCell(3).setCellValue(result.get(CallType.Roaming).get(MobileOperator.Unicom));
            sheet.getRow(3).getCell(3).setCellValue(result.get(CallType.Roaming).get(MobileOperator.Telecom));

            OutputStream os = new FileOutputStream(new File(excelFilePath));
            wb.write(os);
            wb.close();
            System.out.println("--------excel文件已修改--------");
        }catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
        System.out.println("市话: " + "移动: " + result.get(CallType.City).get(MobileOperator.Mobile) +
                "联通: " + result.get(CallType.City).get(MobileOperator.Unicom) +
                "电信: " + result.get(CallType.City).get(MobileOperator.Telecom));
    }

    //计算出用户在各个时间段（时间段的划分如表 1 所示）通话时长所占比
    //例，并将结果以<主叫号码, 时间段 1 占比, ..., 时间段 8 占比>的格式保
    //存成 txt 或 excel 文件
    public static void ProcessThree(String processFile, String outputFile) {
        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(processFile);

        Map<String, ArrayList<CallDataModel>> distinctUsers =
                datas.parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.calling_nbr, Collectors.toCollection(ArrayList::new)));
        HashMap<String, Map<String, Double>> result = new HashMap<>();
        for (Map.Entry<String, ArrayList<CallDataModel>> distinctUser : distinctUsers.entrySet()) {
            ArrayList<TimeFragment> frags = distinctUser.getValue().parallelStream().map(x -> new TimeFragment(LocalTime.parse(x.start_time), LocalTime.parse(x.end_time))).collect(Collectors.toCollection(ArrayList::new));
            TimeFragments totalTimeFrags = new TimeFragments();
            for (TimeFragment f : frags) {
                for (TimeFragment period : totalTimeFrags.Frags.keySet()) {
                    totalTimeFrags.Frags.merge(period, period.DuringTimeInThisFragment(f), (old, newFrag) -> {
                        if (old == null) return newFrag;
                        else
                            return old.plus(newFrag);
                    });
                }
            }
            result.put(distinctUser.getKey(), totalTimeFrags.Frags.entrySet().stream().collect(Collectors.toMap(e -> e.getKey().Name,
                    e -> new Double(e.getValue().toMillis()))));
        }

        //以上操作可以分布式完成，然后统一汇总算比例
        for (Map.Entry<String, Map<String, Double>> distinctUser : result.entrySet()) {
            Double sumTime = distinctUser.getValue().entrySet().stream().mapToDouble(x -> x.getValue()).sum();
            distinctUser.getValue().forEach((string, time) ->
            {
                if (sumTime == 0)
                    distinctUser.getValue().put(string, new Double(0));
                else {
                    Double propTime = time / sumTime;
                    distinctUser.getValue().put(string, propTime);
                }

            });
        }

        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(outputFile, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = {"主叫号码", " 时间段 1 占比", " 时间段 2 占比", " 时间段 3 占比", " 时间段 4 占比", " 时间段 5 占比", " 时间段 6 占比", " 时间段 7 占比", " 时间段 8 占比"};
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for (Map.Entry<String, Map<String, Double>> pair : result.entrySet()) {
                String[] csvLine = {
                        pair.getKey(),
                        DoubleFormat4(pair.getValue().get("时间段 1")),
                        DoubleFormat4(pair.getValue().get("时间段 2")),
                        DoubleFormat4(pair.getValue().get("时间段 3")),
                        DoubleFormat4(pair.getValue().get("时间段 4")),
                        DoubleFormat4(pair.getValue().get("时间段 5")),
                        DoubleFormat4(pair.getValue().get("时间段 6")),
                        DoubleFormat4(pair.getValue().get("时间段 7")),
                        DoubleFormat4(pair.getValue().get("时间段 8"))
                };
                csvWriter.writeRecord(csvLine);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static String DoubleFormat4(double value) {
 /*
  * %.2f % 表示 小数点前任意位数 2 表示两位小数 格式后的结果为 f 表示浮点型
  */
        return new Formatter().format("%.4f", value).toString();
    }

}
