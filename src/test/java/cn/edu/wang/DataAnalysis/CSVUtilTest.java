package cn.edu.wang.DataAnalysis;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.LinkedList;

class CSVUtilTest {
    @Test
    void TestReadCsv() {
        ArrayList<CallDataModel> datas = CSVUtil.ReadCsvFromFile("d:/1-tb_call_201202_random.txt");
        Assertions.assertNotNull(datas);
        CallDataModel first = datas.get(0);
        Assertions.assertEquals("20120201", first.day_id);
        Assertions.assertEquals("349723", first.calling_nbr);
        Assertions.assertEquals("y24373194378", first.called_nbr);
    }
    @Test
    void FileSplitTest()
    {
        LinkedList<String> paths = CSVUtil.Split("d:/data/", "tb_call_201202_random" , "txt", 10);
        Assertions.assertEquals(10, paths.size());
    }
}