package cn.edu.wang.DataAnalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;

class CSVFileLoaderTest {
    @Test
    void TestReadCsv() {
        ArrayList<CallDataModel> datas = CSVFileLoader.ReadCsv("d:/1-tb_call_201202_random.txt");
        Assertions.assertNotNull(datas);
        CallDataModel first = datas.get(0);
        Assertions.assertEquals("20120201", first.day_id);
        Assertions.assertEquals("349723", first.calling_nbr);
        Assertions.assertEquals("y24373194378", first.called_nbr);
    }

}