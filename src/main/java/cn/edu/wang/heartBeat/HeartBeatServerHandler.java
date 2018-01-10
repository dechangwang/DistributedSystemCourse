package cn.edu.wang.heartBeat;

import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import cn.edu.wang.communication.CommunicateClient;
import cn.edu.wang.config.Configure;
import cn.edu.wang.log.Log;
import com.google.gson.Gson;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by wangdechang on 2018/1/4
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter {
    private int loss_connect_time = 0;
    Gson gson = new Gson();

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("client 断开了连接");
        Log.log(ctx.channel().remoteAddress() + "断开了连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
//        System.out.println(ctx.channel().remoteAddress() + "Server :" + msg.toString());
        Log.log(ctx.channel().remoteAddress() + "Server :" + msg.toString());
        if (msg.toString().equals("start connect")) {
            String remoteIp = ctx.channel().remoteAddress().toString();
            System.out.println(remoteIp+"  start connect ----");
            //TODO 目前在测试中存端口 真实部署需要用第二行注释掉的代码,而且需要使用IP，测试时 使用port
//            String deadIp = remoteIp.substring(remoteIp.indexOf(":") + 1,remoteIp.length()).trim();
            String deadIp = remoteIp.substring(1, remoteIp.indexOf(":")).trim();

            //节点重新加入时 通知集群中的其他节点
            System.out.println("this is start connect" + deadIp);
            for (String s : Global.deadIps) {
                System.out.print("\t" + s);
            }
            if (Global.deadIps.size() > 0 && Global.deadIps.contains(deadIp)) {//Global.deadIps.contains(deadIp)
                System.out.println("通知节点重新加入了");
//                final String deadIp1 = (String) Global.deadIps.toArray()[0];
                Global.deadIps.remove(deadIp);
                Global.activateIps.add(deadIp);

                Configure configure = Configure.getConfigureInstance();
                configure.loadProperties();
                final int comm_port = configure.getIntProperties("comm_port");
                for (final String s : Global.activateIps) {//TODO 真实使用这个 Global.activateIps

                    //TODO 目前测试的都是端口，真实部署时需改成ip
                    new Thread(new Runnable() {
                        @Override
                        public void run() {
                            try {
                                Message message = new Message("relive", deadIp);
                                if (!s.equals(Global.currentIp)){
                                    new CommunicateClient().
                                            connect(s, comm_port, gson.toJson(message));

                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }).start();
                }
                Global.activateIps.add(deadIp);

            }
        }
        Thread.sleep(9000);
        ctx.writeAndFlush("Heartbeat");

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if (evt instanceof IdleStateEvent) {
            //服务端对应着读事件，当为READER_IDLE时触发
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.READER_IDLE) {
                loss_connect_time++;
                System.out.println("接收消息超时");
                if (loss_connect_time > 5) {
                    System.out.println("关闭不活动的链接");
                    ctx.channel().close();
                }
            } else {
                super.userEventTriggered(ctx, evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
