package cn.edu.wang.DataAnalysis.Network;

import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchClient;
import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchServer;
import cn.edu.wang.DataAnalysis.Network.Message.AskComputationMsg;
import cn.edu.wang.DataAnalysis.Network.Message.LoginMsg;
import cn.edu.wang.config.Configure;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;
import io.netty.handler.timeout.IdleStateHandler;
import io.netty.util.concurrent.DefaultEventExecutorGroup;
import io.netty.util.concurrent.EventExecutorGroup;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.net.InetAddress;


public class ComputationClient {
    private int port;
    private String host;
    private SocketChannel socketChannel;
    private static final EventExecutorGroup group = new DefaultEventExecutorGroup(20);
    public ComputationClient(int port, String host) throws InterruptedException {
        this.port = port;
        this.host = host;

    }
    public void start() throws InterruptedException {
        EventLoopGroup eventLoopGroup=new NioEventLoopGroup();
        try {
            Bootstrap bootstrap = new Bootstrap();
            bootstrap.channel(NioSocketChannel.class);
            bootstrap.option(ChannelOption.SO_KEEPALIVE, true);
            bootstrap.group(eventLoopGroup);
            bootstrap.remoteAddress(host, port);
            bootstrap.handler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    socketChannel.pipeline().addLast(new IdleStateHandler(20, 10, 0));
                    socketChannel.pipeline().addLast(new ObjectEncoder());
                    socketChannel.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                    socketChannel.pipeline().addLast(new ComputationClientHandler());
                }
            });
            ChannelFuture future = bootstrap.connect(host, port).sync();
            if (future.isSuccess()) {
                socketChannel = (SocketChannel) future.channel();
                System.out.println("connect  computation server  成功---------");
            }
            //future.channel().closeFuture().sync();

        }finally {
            //eventLoopGroup.shutdownGracefully().sync();
        }
    }

    public static void start(String severIp) throws InterruptedException {
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        int computation_port = configure.getIntProperties("computation_port");
        int file_port = configure.getIntProperties("file_dispatch_port");
        try{Constants.setClientId(InetAddress.getLocalHost().toString());}catch(Exception e){}

        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    ComputationClient bootstrap = new ComputationClient(computation_port, severIp);
                    bootstrap.start();

                    LoginMsg loginMsg=new LoginMsg();

                    bootstrap.socketChannel.writeAndFlush(loginMsg);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();
        new Thread(new Runnable() {
            @Override
            public void run() {
                try
                {
                    FileDispatchServer.Instance = new FileDispatchServer();
                    FileDispatchServer.Instance.bind(file_port);
                }
                catch (Exception e)
                {
                    e.printStackTrace();
                }
            }
        }).start();

    }
    public static void main(String[]args) throws InterruptedException {


       // FileDispatchClient.Instance.SocketChannel.writeAndFlush(loginMsg);
    }
}
