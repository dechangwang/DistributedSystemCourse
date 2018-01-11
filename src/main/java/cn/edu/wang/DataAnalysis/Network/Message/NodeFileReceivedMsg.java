package cn.edu.wang.DataAnalysis.Network.Message;

public class NodeFileReceivedMsg  extends BaseMsg {
    public String FilePath;
    public NodeFileReceivedMsg() {
        super();
        setType(MsgType.NODE_RECEIVED_FILE);
    }
}
