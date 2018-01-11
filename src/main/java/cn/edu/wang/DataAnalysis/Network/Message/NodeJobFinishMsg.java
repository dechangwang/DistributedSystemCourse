package cn.edu.wang.DataAnalysis.Network.Message;

import java.util.HashMap;
import java.util.Map;

public class NodeJobFinishMsg extends BaseMsg {
    public NodeJobFinishMsg() {
        super();
        setType(MsgType.NODE_JOB_FINISH);
    }


    public Map getResultData() {
        return ResultData;
    }

    public void setResultData(HashMap resultData) {
        ResultData = resultData;
    }

    public Map ResultData;
}
