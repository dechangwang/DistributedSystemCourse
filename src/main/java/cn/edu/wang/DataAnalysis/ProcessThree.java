package cn.edu.wang.DataAnalysis;

import cn.edu.wang.config.Configure;
import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.time.LocalTime;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessThree extends DataAnalysis<HashMap<String, Map<String, Double>>> {
    public ProcessThree() {
        super(3);
        CollectedResult = new HashMap<>();
        //OutputFilePath = "d:/data/tb_call_201202_random_output_3.csv";
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        OutputFilePath = configure.getProperties("calc_output_path_3");//"d:/data/tb_call_201202_random_output_2.xlsx";

    }

    //计算出用户在各个时间段（时间段的划分如表 1 所示）通话时长所占比
    //例，并将结果以<主叫号码, 时间段 1 占比, ..., 时间段 8 占比>的格式保
    //存成 txt 或 excel 文件
    public  HashMap<String, Map<String, Double>> Compute(String path) {
        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(path);

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
        return result;
    }

    public synchronized void Collect(HashMap<String, Map<String, Double>> small) {
        for (EnumMap.Entry<String, Map<String, Double>> set : small.entrySet()) {
            CollectedResult.merge(set.getKey(), set.getValue(), (oldV, newV) ->
            {
                newV.forEach((newKey, newValue) -> oldV.merge(newKey, newValue, (oldKey, oldValue) -> oldValue + newValue));
                return oldV;
            });
        }
    }

    public  void Final() {
        String outputFile = OutputFilePath;


        //以上操作可以分布式完成，然后统一汇总算比例
        /*
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
        }*/

        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(outputFile, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = {"主叫号码", " 时间段 1 占比", " 时间段 2 占比", " 时间段 3 占比", " 时间段 4 占比", " 时间段 5 占比", " 时间段 6 占比", " 时间段 7 占比", " 时间段 8 占比"};
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for (Map.Entry<String, Map<String, Double>> pair : CollectedResult.entrySet()) {
                Double sumTime = pair.getValue().entrySet().stream().mapToDouble(x -> x.getValue()).sum();

                String[] csvLine = {
                        pair.getKey(),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 1") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 2") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 3") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 4") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 5") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 6") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 7") / sumTime),
                        sumTime == 0? "0" : DoubleFormat4(pair.getValue().get("时间段 8") / sumTime)
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
