package cn.edu.wang.DataAnalysis.Network.FileDispatch;

import cn.edu.wang.uploadFile.FileUploadFile;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileDispatchClient {
    public SocketChannel SocketChannel;

    public void connect(int port, String host, FileUploadFile filePath) throws Exception {
        EventLoopGroup group = new NioEventLoopGroup();
        try {
            Bootstrap b = new Bootstrap();
            b.group(group).channel(NioSocketChannel.class).option(ChannelOption.TCP_NODELAY, true).handler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE,ClassResolvers.weakCachingConcurrentResolver(null)));
                    ch.pipeline().addLast(new FileDispatchClientHandler(filePath));
                }
            });
            ChannelFuture f = b.connect(host, port).sync();
            if (f.isSuccess()) {
                SocketChannel = (SocketChannel) f.channel();
                System.out.println("connect file dispatch server  成功---------");
            }
            f.channel().closeFuture().sync();

        } finally {
            group.shutdownGracefully();
        }
    }
    public static void main(String[] args) {
        int port = 8080;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {

            //new FileDispatchClient().connect(8081, "7.81.11.123");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
