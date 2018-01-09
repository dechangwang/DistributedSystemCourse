package cn.edu.wang.DataAnalysis.Computation;

import cn.edu.wang.DataAnalysis.Computation.Message.*;
import cn.edu.wang.DataAnalysis.DataAnalysis;
import cn.edu.wang.DataAnalysis.Pair;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.socket.SocketChannel;
import io.netty.util.ReferenceCountUtil;

import javax.xml.crypto.Data;
import java.util.HashMap;

public class ComputationServerHandler extends SimpleChannelInboundHandler<BaseMsg> {
    public ComputationServerHandler() {
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        NettyChannelMap.remove((SocketChannel)ctx.channel());
    }

    //请求分布式计算的客户端id
    private String _computationAskedClientID;
    private int _clientReturnCountprocessOne = 0;
    final Object _mutex = new Object();
    private HashMap<String, Pair<Long, Long>> _resultDataProcessOne = new HashMap<>();
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, BaseMsg baseMsg) throws Exception {

        if(MsgType.LOGIN.equals(baseMsg.getType())){
            LoginMsg loginMsg=(LoginMsg)baseMsg;
                //登录成功,把channel存到服务端的map中
                NettyChannelMap.add(loginMsg.getClientId(),(SocketChannel)channelHandlerContext.channel());
                System.out.println("client"+loginMsg.getClientId()+" 登录成功");
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
            case ASK_COMPUTATION:
            {
                System.out.println("ASK_COMPUTATION");
                //客户端请求分布式计算
                NodeStartJobMsg msg = new NodeStartJobMsg();
                msg.FilePath = "d:/1-tb_call_201202_random.csv";
                msg.ActionID = 1;
                _computationAskedClientID = msg.getClientId();
                //向所有连接的客户端发布计算请求
                NettyChannelMap.getClients().values().forEach(c->c.writeAndFlush(msg));
            }
            break;
            case NODE_JOB_FINISH:
            {
                System.out.println("NODE_JOB_FINISH");

                NodeJobFinishMsg msg = (NodeJobFinishMsg)baseMsg;
                DataAnalysis.ProcessOne_Collect(_resultDataProcessOne, msg.ResultData, _mutex);
                synchronized (_mutex)
                {
                    _clientReturnCountprocessOne++;
                }
                if(_clientReturnCountprocessOne == NettyChannelMap.getClients().size())
                {
                    //计算 收集完毕 输出结果
                    //TODO 写死
                    DataAnalysis.ProcessOne_Final(_resultDataProcessOne, "D://1-tb_call_201202_random_output.csv");
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