package cn.edu.wang.DataAnalysis;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class DataAnalysisTest {

    @Test
    void DoAnalysis() {
        DataAnalysis.ProcessOne("d:/1-tb_call_201202_random.txt", "D://1-tb_call_201202_random_output.csv");
        DataAnalysis.ProcessTwo("d:/1-tb_call_201202_random.txt");
    }

}