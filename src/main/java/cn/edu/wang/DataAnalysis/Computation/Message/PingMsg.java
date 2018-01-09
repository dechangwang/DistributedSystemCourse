package cn.edu.wang.DataAnalysis.Computation.Message;

import java.io.Serializable;

public class PingMsg extends BaseMsg {
    public PingMsg() {
        super();
        setType(MsgType.PING);
    }
}

