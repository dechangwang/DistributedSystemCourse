package cn.edu.wang.heartBeat;

import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import cn.edu.wang.communication.CommunicateClient;
import cn.edu.wang.config.Configure;
import cn.edu.wang.log.Log;
import cn.edu.wang.utils.IpUtils;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;
import io.netty.util.CharsetUtil;
import io.netty.util.ReferenceCountUtil;

import java.util.Date;

/**
 * Created by wangdechang on 2018/1/4
 */
public class HeartBeatClientHandler extends ChannelInboundHandlerAdapter{
    private static final ByteBuf HEARTBEAT_SEQUENCE = Unpooled.unreleasableBuffer(Unpooled.copiedBuffer("Heartbeat",
            CharsetUtil.UTF_8));

    private static final int TRY_TIMES = 3;

    private int currentTime = 0;
    Gson gson = new Gson();

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String str = "激活时间是："+new Date();
        System.out.println(str);
        System.out.println("链接已经激活");
        Log.log(str+"\r\n链接已经激活");
        ctx.writeAndFlush("start connect");
//        ctx.fireChannelActive();
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        String str = "停止时间是："+new Date();
        System.out.println(str);
        Log.log(str);
        String remoteIp = ctx.channel().remoteAddress().toString();
        System.out.println("ip=" +remoteIp+" port = ");

        //TODO 目前在测试中存端口 真实部署需要用第二行注释掉的代码
//        String deadIp = remoteIp.substring(remoteIp.indexOf(":"),remoteIp.length());
        String deadIp = remoteIp.substring(1,remoteIp.indexOf(":"));

        Global.deadIps.add(deadIp);
        Global.activateIps.remove(deadIp);
        String[] allIps = Global.allIps;
        String currentIp = Global.currentIp;

        String nextAliveIp = IpUtils.nextIp(deadIp,allIps);
        System.out.println("nextIp +" + nextAliveIp);
        System.out.println("current Ip = "+Global.currentIp);
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        int heart_beat_port = configure.getIntProperties("heart_beat_port");
        if (nextAliveIp.length() > 0 && Global.currentIp.equals(nextAliveIp)){
            //TODO 这里在实际部署时，nextAliveIp是Ip，端口需要读取配置文件
            //nextAliveIp = "8087";//TODO 只测试第三台机器宕机的情况
            HeartBeatsClient client = new HeartBeatsClient(heart_beat_port,nextAliveIp);
            client.start();
        }

        final int comm_port = configure.getIntProperties("comm_port");
        System.out.println("client "+ currentIp + " said dead "+ deadIp);
        for (final String s:Global.activateIps){

            //TODO 目前测试的都是端口，真实部署时需改成ip
            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        Message message = new Message("dead",deadIp);
                        if (!s.equals(Global.currentIp)){
                            new CommunicateClient()
                                    .connect(s,comm_port,gson.toJson(message));

                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        }


        System.out.println("关闭链接");
    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        System.out.println("当前轮询时间："+new Date());
        if (evt instanceof IdleStateEvent) {
            IdleStateEvent event = (IdleStateEvent) evt;
            if (event.state() == IdleState.WRITER_IDLE) {
                if(currentTime <= TRY_TIMES){
                    System.out.println("currentTime:"+currentTime);
                    Log.log("currentTime:"+currentTime);
                    currentTime++;
                    ctx.channel().writeAndFlush(HEARTBEAT_SEQUENCE.duplicate());
                }
            }
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        String message = (String) msg;
//        System.out.println(message);
        Log.log(message);
        if (message.equals("Heartbeat")) {
            Thread.sleep(10000);
            ctx.write("receive message from server");
            ctx.flush();
        }
        ReferenceCountUtil.release(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
