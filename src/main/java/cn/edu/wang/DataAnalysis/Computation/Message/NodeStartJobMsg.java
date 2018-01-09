package cn.edu.wang.DataAnalysis.Computation.Message;

import java.util.HashMap;

public class NodeStartJobMsg extends BaseMsg {
    public NodeStartJobMsg() {
        super();
        setType(MsgType.NODE_START_JOB);
    }
    public int ActionID;

    public String FilePath;
    public void setParams(int actionId) {
        this.ActionID = actionId;
    }

    public int getActionID() {
        return ActionID;
    }

    public void setActionID(int actionID) {
        ActionID = actionID;
    }
}


