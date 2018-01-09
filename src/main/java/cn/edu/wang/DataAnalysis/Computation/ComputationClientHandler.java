package cn.edu.wang.DataAnalysis.Computation;


import cn.edu.wang.DataAnalysis.Computation.Message.*;
import cn.edu.wang.DataAnalysis.DataAnalysis;
import cn.edu.wang.DataAnalysis.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;

public class ComputationClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                    PingMsg pingMsg=new PingMsg();
                    ctx.writeAndFlush(pingMsg);
                    System.out.println("send ping to server----------");
                    break;
                default:
                    break;
            }
        }
    }
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {
        MsgType msgType=baseMsg.getType();
        switch (msgType){
            case LOGIN:{
                //向服务器发起登录
                LoginMsg loginMsg=new LoginMsg();
                channelHandlerContext.writeAndFlush(loginMsg);
            }break;
            case PING:{
                System.out.println("receive ping from server----------");
            }break;
            case ASK:{
                ReplyClientBody replyClientBody=new ReplyClientBody("client info **** !!!");
                ReplyMsg replyMsg=new ReplyMsg();
                replyMsg.setBody(replyClientBody);
                channelHandlerContext.writeAndFlush(replyMsg);
            }break;
            case NODE_START_JOB:
            {
                System.out.println("NODE_START_JOB");

                NodeStartJobMsg msg = (NodeStartJobMsg)baseMsg;
                int jobType = msg.ActionID;
                switch(jobType)
                {
                    //进行第一个数据处理 目前写死，之后可以用工厂动态创建指定类
                    //文件分割，处理制定文本可以放在StartJobMsg里面，目前写死
                    case 1:
                    {
                        HashMap<String, Pair<Long, Long>> result =  DataAnalysis.ProcessOne_Compute(msg.FilePath);
                        NodeJobFinishMsg outMsg = new NodeJobFinishMsg();
                        outMsg.ResultData = result;
                        channelHandlerContext.writeAndFlush(outMsg);
                    }
                }
            }
            break;
            case REPLY:{
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyServerBody replyServerBody=(ReplyServerBody)replyMsg.getBody();
                //System.out.println("receive client msg: "+replyServerBody.getServerInfo());
            }
            default:break;
        }
        ReferenceCountUtil.release(msgType);
    }
}