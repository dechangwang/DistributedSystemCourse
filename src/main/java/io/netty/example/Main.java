package io.netty.example;

import cn.edu.wang.Global;
import cn.edu.wang.communication.CommunicateServer;
import cn.edu.wang.config.Configure;
import cn.edu.wang.file.FileServer;
import cn.edu.wang.heartBeat.HeartBeatServer;
import cn.edu.wang.heartBeat.HeartBeatsClient;
import cn.edu.wang.uploadFile.FileUploadServer;
import cn.edu.wang.utils.IpUtils;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import cn.edu.wang.download.FileServerHandler;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;

import javax.swing.*;
import java.util.ArrayList;
import java.util.Collections;

/**
 * Created by wangdechang on 2018/1/4
 */
public class Main {
    public static void main(String[] args){
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        final int port = configure.getIntProperties("server_port");
        final int heart_beat_port = configure.getIntProperties("heart_beat_port");
        final int observerHeartBeatPort = configure.getIntProperties("heart_beat_port2");
        final int upload_file_port = configure.getIntProperties("upload_server_port");
        final int comm_port = configure.getIntProperties("comm_port");
        //TODO 在具体使用的时候 要把这里这里的要从Global中的allIps中获取
        final String ip = configure.getProperties("ip");
        final int delay = configure.getIntProperties("delay");
        String ips = configure.getProperties("ips");
        final int downloadFilePort = configure.getIntProperties("download_file_port");
        String[] allIps = ips.split(",");
        ArrayList<String> ipList = new ArrayList<>();
        for(String s:allIps){
            Global.activateIps.add(s);
            ipList.add(s);
        }
        Collections.sort(ipList);
        Global.allIps = ipList.toArray(new String[ipList.size()]);
        System.out.println("============allIps===========");
        for (String ip1: Global.allIps){
            System.out.println(ip1);
        }
        System.out.println("=============================");
        //Global.allIps = allIps;
        System.out.println(Global.allIps);
        String currentIp = configure.getProperties("current_ip");
        Global.currentIp = currentIp;
        final String nextIp = IpUtils.observerIp(Global.currentIp);
        System.out.println("currentIp"+ currentIp);
        System.out.println("nextIp "+ nextIp);
        //Global.currentIp = "8091";
        try {

            new Thread(new Runnable() {
                public void run() {
                    try {
                        new FileServer().run(upload_file_port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    try {
                        new FileUploadServer().bind(port);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();
            new Thread(new Runnable() {
                public void run() {
                    new HeartBeatServer(heart_beat_port).start();
                }
            }).start();
//
           /* new HeartBeatsClient(observerHeartBeatPort,ip).start();*/
            new Thread(new Runnable() {
                public void run() {
                    System.out.println("main1");
                    try {
                        //心跳检测需要等10s后启动为了确保
                        Thread.sleep(1000 * delay);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }

                    System.out.println(">>>>nextIp " + nextIp);
                    HeartBeatsClient client = new HeartBeatsClient(heart_beat_port,nextIp);
                    client.start();
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {

                    try {
                        CommunicateServer server = new CommunicateServer();
                        server.start(comm_port);
                        //TODO 未来需要删除 目前为了方便这样子写
                        //Global.currentIp = "8091";
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }).start();

            new Thread(new Runnable() {
                @Override
                public void run() {
                    try {
                        startFileServer();
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }).start();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void startFileServer() throws InterruptedException {
        EventLoopGroup bossGroup = new NioEventLoopGroup(1);
        EventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap b = new ServerBootstrap();
            b.group(bossGroup, workerGroup).channel(NioServerSocketChannel.class).handler(new LoggingHandler(LogLevel.INFO))
                    .childHandler(new ChannelInitializer<SocketChannel>() {// 有连接到达时会创建一个channel
                        @Override
                        protected void initChannel(SocketChannel ch) throws Exception {
                            ChannelPipeline pipeline = ch.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            pipeline.addLast(new HttpObjectAggregator(65536));
                            pipeline.addLast(new ChunkedWriteHandler());
                            pipeline.addLast(new FileServerHandler());
                        }
                    });

            Channel ch = b.bind(7878).sync().channel();
            System.err.println("打开浏览器，输入： " + ("http") + "://127.0.0.1:" + 7878 + '/');
            ch.closeFuture().sync();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }
    }
}

