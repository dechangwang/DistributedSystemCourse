package cn.edu.wang.DataAnalysis.Network;

import cn.edu.wang.DataAnalysis.Network.Message.*;
import cn.edu.wang.DataAnalysis.DataAnalysis;
import cn.edu.wang.DataAnalysis.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

import java.util.HashMap;

public class ComputationServerHandler extends SimpleChannelInboundHandler<BaseMsg> {
    public ComputationServerHandler() {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelMap.remove((SocketChannel)ctx.channel());
    }


    //请求分布式计算的客户端id

    private static int _clientReturnCountprocessOne = 0;

    public  static DataAnalysis AnalysisJob;


    static  final Object _mutex = new Object();
    private HashMap<String, Pair<Long, Long>> _resultDataProcessOne = new HashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

        if(MsgType.LOGIN.equals(baseMsg.getType())){
            LoginMsg loginMsg=(LoginMsg)baseMsg;
                //登录成功,把channel存到服务端的map中
                NettyChannelMap.add(loginMsg.getClientId(),(SocketChannel)channelHandlerContext.channel());
                //一开始节点都是空闲的
            //channelHandlerContext.channel().localAddress()
                NettyChannelMap.IdleClient.add((SocketChannel)channelHandlerContext.channel());
                System.out.println(loginMsg.getClientId()+" computation client 登录成功");
            System.out.println("当前已有"+ NettyChannelMap.getClients().size() + "个节点");

        }else{
            if(NettyChannelMap.get(baseMsg.getClientId())==null){
                //说明未登录，或者连接断了，服务器向客户端发起登录请求，让客户端重新登录
                LoginMsg loginMsg=new LoginMsg();
                channelHandlerContext.channel().writeAndFlush(loginMsg);
            }
        }
        switch (baseMsg.getType()){
            case PING:{
                PingMsg pingMsg=(PingMsg)baseMsg;
                PingMsg replyPing=new PingMsg();
                NettyChannelMap.get(pingMsg.getClientId()).writeAndFlush(replyPing);
            }break;
            case ASK:{
                //收到客户端的请求
                AskMsg askMsg=(AskMsg)baseMsg;
                if("authToken".equals(askMsg.getParams().getAuth())){
                    ReplyServerBody replyBody=new ReplyServerBody("server info $$$$ !!!");
                    ReplyMsg replyMsg=new ReplyMsg();
                    replyMsg.setBody(replyBody);
                    NettyChannelMap.get(askMsg.getClientId()).writeAndFlush(replyMsg);
                }
            }break;
            /*
            case ASK_COMPUTATION:
            {
                System.out.println("ASK_COMPUTATION");
                //客户端请求分布式计算

                //向所有连接的客户端发布计算请求 测试 两个节点
                if(NettyChannelMap.getClients().size() == 2)
                {
                    int i = 1;
                    for(SocketChannel s :  NettyChannelMap.getClients().values())
                    {
                        NodeStartJobMsg msg = new NodeStartJobMsg();
                        msg.FilePath = "d:/"+i+"-tb_call_201202_random.csv";
                        msg.ActionID = 1;
                        s.writeAndFlush(msg);
                        i++;
                    }

                }
                //NettyChannelMap.getClients().values().forEach(c->c.writeAndFlush(msg));

            }
            break;
            */
            case NODE_JOB_FINISH:
            {
                System.out.println("NODE_JOB_FINISH");

                NodeJobFinishMsg msg = (NodeJobFinishMsg)baseMsg;

                //客户端空闲
                NettyChannelMap.IdleClient.add(NettyChannelMap.get(baseMsg.getClientId()));

                AnalysisJob.Collect(msg.ResultData);

                synchronized (_mutex)
                {
                    ComputationServer.JobFinished++;
                    if(ComputationServer.JobFinished == 10)
                    {
                        AnalysisJob.Final();
                    }
                }
            }
            break;
            case REPLY:{
                //收到客户端回复
                ReplyMsg replyMsg=(ReplyMsg)baseMsg;
                ReplyClientBody clientBody=(ReplyClientBody)replyMsg.getBody();
                //System.out.println("receive client msg: "+clientBody.getClientInfo());
            }break;
            default:break;
        }
        ReferenceCountUtil.release(baseMsg);
    }
}