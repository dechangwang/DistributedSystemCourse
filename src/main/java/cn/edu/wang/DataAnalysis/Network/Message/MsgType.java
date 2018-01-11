package cn.edu.wang.DataAnalysis.Network.Message;

public enum  MsgType {
    PING,ASK,REPLY,LOGIN,
    //1.请求分布式计算
    ASK_COMPUTATION,
    //2.服务端向客户端下载文件
    NODE_RECEIVED_FILE,
    //3.请求客户端开始计算
    NODE_START_JOB,
    //4.计算结束返回结果
    NODE_JOB_FINISH,
    
}

