package cn.edu.wang.communication;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;

/**
 * Created by wangdechang on 2018/1/7
 */
public class CommunicateWithCallbackClient {
    private ChannelFutureListener iCommunicationCallback;


    public CommunicateWithCallbackClient(ChannelFutureListener iCommunicationCallback) {
        this.iCommunicationCallback = iCommunicationCallback;

    }

    public void connect(String host, int port, String message) throws Exception {
        EventLoopGroup workerGroup = new NioEventLoopGroup();

        try {
            Bootstrap b = new Bootstrap();
            b.group(workerGroup);
            b.channel(NioSocketChannel.class);
            b.option(ChannelOption.SO_KEEPALIVE, true);
            b.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                public void initChannel(SocketChannel ch) throws Exception {
                    ch.pipeline().addLast(new CClientHandlerWithCallback(message));
                }
            });

            // Start the client.
            ChannelFuture f = b.connect(host, port).sync();
            f.addListener(iCommunicationCallback);

            // Wait until the connection is closed.
            f.channel().closeFuture().sync();
        } finally {
            workerGroup.shutdownGracefully();
        }

    }

    public static void main(String[] args) throws Exception {
//        CommunicateWithCallbackClient client = new CommunicateWithCallbackClient();
//        client.connect("127.0.0.1", 8000,"message");
        ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //在这里处理请求处理结束的结果
            }
        };
    }
}
