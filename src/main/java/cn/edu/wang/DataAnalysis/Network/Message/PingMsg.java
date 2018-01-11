package cn.edu.wang.DataAnalysis.Network.Message;

public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}

