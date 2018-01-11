package cn.edu.wang.DataAnalysis.Network.Message;

public class LoginMsg extends BaseMsg {

    public LoginMsg() {
        super();
        setType(MsgType.LOGIN);
    }
}
