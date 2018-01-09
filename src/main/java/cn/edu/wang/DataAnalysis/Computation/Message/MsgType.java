package cn.edu.wang.DataAnalysis.Computation.Message;

import java.io.Serializable;

public enum  MsgType {
    PING,ASK,REPLY,LOGIN,
    //1.请求分布式计算
    ASK_COMPUTATION,
    //2.请求客户端开始计算（附送文件？）
    NODE_START_JOB,
    //3.计算结束返回结果
    NODE_JOB_FINISH,
    
}

