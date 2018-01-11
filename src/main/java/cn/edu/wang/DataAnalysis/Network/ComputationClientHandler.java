package cn.edu.wang.DataAnalysis.Network;


import cn.edu.wang.DataAnalysis.*;
import cn.edu.wang.DataAnalysis.Network.Message.*;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.ReferenceCountUtil;

import java.util.EnumMap;
import java.util.HashMap;
import java.util.Map;

public class ComputationClientHandler extends SimpleChannelInboundHandler<BaseMsg> {
    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent e = (IdleStateEvent) evt;
            switch (e.state()) {
                case WRITER_IDLE:
                  //  PingMsg pingMsg=new PingMsg();
                  //  ctx.writeAndFlush(pingMsg);
                   // System.out.println("send ping to server----------");
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
                    case 1:
                    {
                        HashMap<String, Pair<Long, Long>> result =  new ProcessOne().Compute(msg.FilePath);
                        NodeJobFinishMsg outMsg = new NodeJobFinishMsg();
                        outMsg.ResultData = result;
                        channelHandlerContext.writeAndFlush(outMsg);
                        break;
                    }
                    case 2:
                    {
                        EnumMap<CallType, EnumMap<MobileOperator, Long>> result =  new ProcessTwo().Compute(msg.FilePath);
                        NodeJobFinishMsg outMsg = new NodeJobFinishMsg();
                        outMsg.ResultData = result;
                        channelHandlerContext.writeAndFlush(outMsg);
                        break;
                    }
                    case 3:
                    {
                        HashMap<String, Map<String, Double>> result =  new ProcessThree().Compute(msg.FilePath);
                        NodeJobFinishMsg outMsg = new NodeJobFinishMsg();
                        outMsg.ResultData = result;
                        channelHandlerContext.writeAndFlush(outMsg);
                        break;
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