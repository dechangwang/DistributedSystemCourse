package cn.edu.wang.DataAnalysis;

import com.csvreader.CsvWriter;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.stream.Collectors;

//计算出用户的每日平均通话次数，并将结果以<主叫号码, 每日平均通话
//次数>的格式保存成 txt 或 excel 文件
public class ProcessOne extends DataAnalysis<HashMap<String, Pair<Long, Long>>>
{
    public ProcessOne() {
        super(1);
        CollectedResult = new HashMap<>();
        OutputFilePath = "d:/data/tb_call_201202_random_output_1.csv";


    }

    //处理1 分布式部分 客户端执行
    public  HashMap<String, Pair<Long, Long>> Compute(String path) {
        System.out.println("ProcessOne_Compute Start");

        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(path);

        HashMap<String, Pair<Long, Long>> UserCallTimeMap = new HashMap<>();

        //按照主叫号码 分组
        Map<String, ArrayList<CallDataModel>> distinctUsers =
                datas.stream().collect(Collectors.groupingBy((CallDataModel x) -> x.calling_nbr, Collectors.toCollection(ArrayList::new)));

        //对于每一个主叫号码分别计算 分布式
        for (Map.Entry<String, ArrayList<CallDataModel>> distinctUser : distinctUsers.entrySet()) {
            Map<String, Long> CallTimePerDay =
                    distinctUser.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.day_id, Collectors.counting()));

            Long sum = CallTimePerDay.values().stream().reduce(new Long(0), Long::sum);// / CallTimePerDay.values().size();
            UserCallTimeMap.put(distinctUser.getKey(), new Pair<>(sum, new Long(CallTimePerDay.values().size())));
        }
        System.out.println("ProcessOne_Compute Finish");

        return UserCallTimeMap;
    }

    //处理1 汇总 服务器合并客户端传来的中间数据
    public synchronized void Collect(HashMap<String, Pair<Long, Long>> small) {

        System.out.println("ProcessOne_Collect");

        // synchronized (mutex) {
        for (Map.Entry<String, Pair<Long, Long>> set : small.entrySet()) {
            CollectedResult.merge(set.getKey(), set.getValue(), (oldV, newV) -> new Pair<>(oldV.GetFirst() + newV.GetFirst(), oldV.GetSecond() + newV.GetSecond()));
        }
        //    }
    }

    //处理1 汇总后部分，服务器端执行
    @Override
    public  void Final() {
        String outputFile = OutputFilePath;
        //汇总，除以总量
        System.out.println("ProcessOne_Final");

        try {
            // 创建CSV写对象 例如:CsvWriter(文件路径，分隔符，编码格式);
            CsvWriter csvWriter = new CsvWriter(outputFile, ',', Charset.forName("UTF-8"));
            // 写表头
            String[] csvHeaders = {"主叫号码", "每日平均通话次数"};
            csvWriter.writeRecord(csvHeaders);
            // 写内容
            for (Map.Entry<String, Pair<Long, Long>> pair : CollectedResult.entrySet()) {
                Float avg = new Float((float) pair.getValue().GetFirst() / pair.getValue().GetSecond());
                String[] csvLine = {pair.getKey(), avg.toString()};
                csvWriter.writeRecord(csvLine);
            }
            csvWriter.close();
            System.out.println("--------CSV文件已经写入--------");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
