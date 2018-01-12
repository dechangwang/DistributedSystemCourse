package cn.edu.wang.DataAnalysis.Network.FileDispatch;

import cn.edu.wang.DataAnalysis.Network.NettyChannelMap;
import cn.edu.wang.uploadFile.FileUploadFile;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileDispatchServer {
    public void bind(int port) throws Exception {
        EventLoopGroup bossGroup = new NioEventLoopGroup();
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).option(ChannelOption.SO_BACKLOG, 1024).childHandler(new ChannelInitializer<Channel>() {

                @Override
                protected void initChannel(Channel ch) throws Exception {
                    ch.pipeline().addLast(new ObjectEncoder());
                    ch.pipeline().addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.weakCachingConcurrentResolver(null))); // 最大长度
                    ch.pipeline().addLast(new FileDispatchServerHandler());
                }
            });
            ChannelFuture f = b.bind(port).sync();
            if (f.isSuccess()) {
                System.out.println("file dispatch server start---------------");
            }
            f.channel().closeFuture().sync();

        }
        catch(Exception e)
        {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
        finally {

        }
    }
    public static FileDispatchServer Instance;
    public static void main(String[] args) {
        int port = 8081;
        if (args != null && args.length > 0) {
            try {
                port = Integer.valueOf(args[0]);
            } catch (NumberFormatException e) {
                e.printStackTrace();
            }
        }
        try {
            Instance = new FileDispatchServer();
         //   Instance.bind("7.81.11.123",port);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
