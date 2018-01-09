package cn.edu.wang.DataAnalysis.Computation.Message;

import java.util.HashMap;

public class NodeJobFinishMsg extends BaseMsg {
    public NodeJobFinishMsg() {
        super();
        setType(MsgType.NODE_JOB_FINISH);
    }


    public HashMap getResultData() {
        return ResultData;
    }

    public void setResultData(HashMap resultData) {
        ResultData = resultData;
    }

    public HashMap ResultData;
}
