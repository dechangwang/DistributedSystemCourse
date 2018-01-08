package cn.edu.wang.DataAnalysis;

import org.junit.jupiter.api.Test;

class DataAnalysisTest {

    @Test
    void DoAnalysis1() {
        DataAnalysis.ProcessOne("d:/1-tb_call_201202_random.txt", "D://1-tb_call_201202_random_output.csv");
    }
    @Test
    void DoAnalysis2()
    {
        DataAnalysis.ProcessTwo("d:/1-tb_call_201202_random.txt", "d://PieChart.xlsx");

    }
    @Test
    void DoAnalysis3()
    {
        DataAnalysis.ProcessThree("d:/1-tb_call_201202_random.txt", "D://1-tb_call_201202_random_output_3.csv");
    }
}