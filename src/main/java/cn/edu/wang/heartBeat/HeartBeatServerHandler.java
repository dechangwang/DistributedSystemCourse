package cn.edu.wang.heartBeat;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.handler.timeout.IdleState;
import io.netty.handler.timeout.IdleStateEvent;

/**
 * Created by wangdechang on 2018/1/4
 */
public class HeartBeatServerHandler extends ChannelInboundHandlerAdapter{
    private int loss_connect_time = 0;

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        super.channelInactive(ctx);
        System.out.println("client 断开了连接");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        System.out.println(ctx.channel().remoteAddress() + "Server :" + msg.toString());
        Thread.sleep(10000);
        ctx.writeAndFlush("Heartbeat");

    }

    @Override
    public void userEventTriggered(ChannelHandlerContext ctx, Object evt) throws Exception {
        if(evt instanceof IdleStateEvent){
            //服务端对应着读事件，当为READER_IDLE时触发
            IdleStateEvent event = (IdleStateEvent)evt;
            if(event.state() == IdleState.READER_IDLE){
                loss_connect_time++;
                System.out.println("接收消息超时");
                if(loss_connect_time > 2){
                    System.out.println("关闭不活动的链接");
                    ctx.channel().close();
                }
            }else{
                super.userEventTriggered(ctx,evt);
            }
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        ctx.close();
    }
}
