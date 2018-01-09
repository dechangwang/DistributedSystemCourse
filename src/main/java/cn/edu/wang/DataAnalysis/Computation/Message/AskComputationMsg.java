package cn.edu.wang.DataAnalysis.Computation.Message;

public class AskComputationMsg extends BaseMsg {
    public AskComputationMsg() {
        super();
        setType(MsgType.ASK_COMPUTATION);
    }
    public int ActionID;


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
