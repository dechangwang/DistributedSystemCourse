package cn.edu.wang.DataAnalysis;

import cn.edu.wang.config.Configure;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;

import java.io.*;
import java.util.*;
import java.util.stream.Collectors;

public class ProcessTwo extends DataAnalysis<EnumMap<CallType, EnumMap<MobileOperator, Long>>> {
    public ProcessTwo() {
        super(2);
        CollectedResult = new EnumMap<>(CallType.class);
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        OutputFilePath = configure.getProperties("calc_output_path_2");//"d:/data/tb_call_201202_random_output_2.xlsx";

    }

    //计算出不同通话类型（市话、长途、国际）下各个运营商（移动，联通，
    //电信）的占比，并画出饼状图
    public EnumMap<CallType, EnumMap<MobileOperator, Long>> Compute(String path) {
        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile(path);

        Map<CallType, ArrayList<CallDataModel>> distinctUsers =
                datas.parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.call_type, Collectors.toCollection(ArrayList::new)));

        EnumMap<CallType, EnumMap<MobileOperator, Long>> result = new EnumMap<>(CallType.class);

        for (Map.Entry<CallType, ArrayList<CallDataModel>> eachCallType : distinctUsers.entrySet()) {
            Map<MobileOperator, Long> CallingCountPerOptr =
                    eachCallType.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.calling_optr, Collectors.counting()));
            Map<MobileOperator, Long> CalledCountPerOptr =
                    eachCallType.getValue().parallelStream().collect(Collectors.groupingBy((CallDataModel x) -> x.called_optr, Collectors.counting()));

            Long total = CallingCountPerOptr.values().stream().reduce(new Long(0), Long::sum);
            total += CalledCountPerOptr.values().stream().reduce(new Long(0), Long::sum);


            EnumMap<MobileOperator, Long> TotalCountPerOptr = new EnumMap<>(MobileOperator.class);
            //Map<MobileOperator, Float> resultPerOptr = new EnumMap<>(MobileOperator.class);

            //上面那个map合并到一个新的map中
            TotalCountPerOptr.putAll(CallingCountPerOptr);
            CalledCountPerOptr.forEach((optr, count) -> TotalCountPerOptr.merge(optr, count, (value, newValue) -> value + newValue));


            // resultPerOptr.put(MobileOperator.Mobile, new Float(TotalCountPerOptr.get(MobileOperator.Mobile)) / total);
            // resultPerOptr.put(MobileOperator.Unicom, new Float(TotalCountPerOptr.get(MobileOperator.Unicom)) / total);
            // resultPerOptr.put(MobileOperator.Telecom, new Float(TotalCountPerOptr.get(MobileOperator.Telecom)) / total);

            result.put(eachCallType.getKey(), TotalCountPerOptr);
        }
        return result;
    }

    public synchronized void Collect(EnumMap<CallType, EnumMap<MobileOperator, Long>> small) {
        System.out.println("ProcessTwo_Collect");

        // synchronized (mutex) {
        for (EnumMap.Entry<CallType, EnumMap<MobileOperator, Long>> set : small.entrySet()) {
            CollectedResult.merge(set.getKey(), set.getValue(), (oldV, newV) ->
            {
                newV.forEach((newKey, newValue) -> oldV.merge(newKey, newValue, (oldKey, oldValue) -> oldValue + newValue));
                return oldV;
            });
        }
    }

    public void Final() {
        String excelFilePath = OutputFilePath;

        System.out.println("ProcessTwo_Final");

        try {
            InputStream is = new FileInputStream(new File(excelFilePath));
            Workbook wb = new XSSFWorkbook(is);
            is.close();
            Sheet sheet = wb.getSheetAt(0);
            float sum1 = (CollectedResult.get(CallType.City).get(MobileOperator.Mobile) + CollectedResult.get(CallType.City).get(MobileOperator.Unicom) + CollectedResult.get(CallType.City).get(MobileOperator.Telecom));
            float sum2 = (CollectedResult.get(CallType.LongDistance).get(MobileOperator.Mobile) + CollectedResult.get(CallType.LongDistance).get(MobileOperator.Unicom) + CollectedResult.get(CallType.LongDistance).get(MobileOperator.Telecom));
            float sum3 = (CollectedResult.get(CallType.Roaming).get(MobileOperator.Mobile) + CollectedResult.get(CallType.Roaming).get(MobileOperator.Unicom) + CollectedResult.get(CallType.Roaming).get(MobileOperator.Telecom));
            sheet.getRow(1).getCell(1).setCellValue(CollectedResult.get(CallType.City).get(MobileOperator.Mobile)/sum1);
            sheet.getRow(2).getCell(1).setCellValue(CollectedResult.get(CallType.City).get(MobileOperator.Unicom)/sum1);
            sheet.getRow(3).getCell(1).setCellValue(CollectedResult.get(CallType.City).get(MobileOperator.Telecom)/sum1);
            sheet.getRow(1).getCell(2).setCellValue(CollectedResult.get(CallType.LongDistance).get(MobileOperator.Mobile)/sum2);
            sheet.getRow(2).getCell(2).setCellValue(CollectedResult.get(CallType.LongDistance).get(MobileOperator.Unicom)/sum2);
            sheet.getRow(3).getCell(2).setCellValue(CollectedResult.get(CallType.LongDistance).get(MobileOperator.Telecom)/sum2);
            sheet.getRow(1).getCell(3).setCellValue(CollectedResult.get(CallType.Roaming).get(MobileOperator.Mobile)/sum3);
            sheet.getRow(2).getCell(3).setCellValue(CollectedResult.get(CallType.Roaming).get(MobileOperator.Unicom)/sum3);
            sheet.getRow(3).getCell(3).setCellValue(CollectedResult.get(CallType.Roaming).get(MobileOperator.Telecom)/sum3);

            OutputStream os = new FileOutputStream(new File(excelFilePath));
            wb.write(os);
            wb.close();
            System.out.println("--------excel文件已修改--------");
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
