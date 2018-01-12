package cn.edu.wang.DataAnalysis.Network;

import cn.edu.wang.DataAnalysis.*;
import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchClient;
import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchServer;
import cn.edu.wang.DataAnalysis.Network.Message.NodeStartJobMsg;
import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import cn.edu.wang.communication.CommunicateClient;
import cn.edu.wang.config.Configure;
import cn.edu.wang.uploadFile.FileUploadFile;
import com.google.gson.Gson;
import io.netty.channel.socket.SocketChannel;


import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.serialization.ClassResolvers;
import io.netty.handler.codec.serialization.ObjectDecoder;
import io.netty.handler.codec.serialization.ObjectEncoder;

import java.io.BufferedReader;
import java.io.File;
import java.io.InputStreamReader;
import java.util.LinkedList;
import java.util.concurrent.ConcurrentLinkedQueue;

/**
 * Created by yaozb on 15-4-11.
 */
public class ComputationServer {
    private int port;
    private String ip;
    private SocketChannel socketChannel;
    private ComputationServerHandler _handler;

    public ComputationServer(String ip, int port) throws InterruptedException {
        this.ip = ip;
        this.port = port;

    }


    //  public ConcurrentLinkedQueue<String> AnalysisJobQueue = new ConcurrentLinkedQueue<>();
    public static int JobFinished = 0;

    public void StartComputation(DataAnalysis process, String path, String name, String ext) {
        JobFinished = 0;
        ComputationServerHandler.AnalysisJob = process;
        //大文件分割后每一个子计算块放入队列，等待空闲client分发
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();

        LinkedList<String> filePaths = CSVUtil.Split(path, name, ext, 10);
        //分发
        while (filePaths.size() > 0) {
            if (NettyChannelMap.IdleClient.size() > 0) {
                SocketChannel client = NettyChannelMap.IdleClient.poll();
                String clientID = NettyChannelMap.GetSocketID(client);
                DispatchFile(filePaths.poll(), clientID);
            }
        }
    }

    public void DispatchFile(String fileName, String clientID) {
        System.out.println("向客户端：" + clientID + " 分发文件：" + fileName);
        SocketChannel client = NettyChannelMap.get(clientID);
        FileUploadFile uploadFile = new FileUploadFile();
        File file = new File(fileName);
        String fileMd5 = file.getName();// 文件名
        uploadFile.setFile(file);
        uploadFile.setFile_md5(fileMd5);
        uploadFile.setStarPos(0);// 文件开始位置
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Configure configure = Configure.getConfigureInstance();
                    int file_port = configure.getIntProperties("file_dispatch_port");
                    new FileDispatchClient().connect(file_port, client.remoteAddress().getHostString(), uploadFile);
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }).start();


    }

    public void OnNodeFileReceived(String socketName, String filePath) {
        System.out.println("请求节点： " + socketName + " 开始执行计算任务---------");
        SocketChannel client = NettyChannelMap.get(socketName);
        NodeStartJobMsg msg = new NodeStartJobMsg();
        msg.ActionID = ComputationServerHandler.AnalysisJob.ID;
        msg.FilePath = filePath;
        client.writeAndFlush(msg);
    }

    public void bind() throws InterruptedException {
        EventLoopGroup boss = new NioEventLoopGroup();
        EventLoopGroup worker = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(boss, worker);
            bootstrap.channel(NioServerSocketChannel.class);
            bootstrap.option(ChannelOption.SO_BACKLOG, 1024);
            //  bootstrap.option(ChannelOption.TCP_NODELAY, true);
            bootstrap.childOption(ChannelOption.SO_KEEPALIVE, true);

            bootstrap.childHandler(new ChannelInitializer<SocketChannel>() {
                @Override
                protected void initChannel(SocketChannel socketChannel) throws Exception {
                    ChannelPipeline p = socketChannel.pipeline();
                    p.addLast(new ObjectEncoder());
                    p.addLast(new ObjectDecoder(Integer.MAX_VALUE, ClassResolvers.cacheDisabled(null)));
                    p.addLast(new ComputationServerHandler());
                }
            });


            ChannelFuture f = bootstrap.bind(ip, port).sync();
            if (f.isSuccess()) {
                System.out.println("computation server start---------------");
                System.out.println("Computation Server Active!");

            }
            f.channel().closeFuture().sync();

        } finally {
            // 优雅停机
            //  boss.shutdownGracefully();
            // worker.shutdownGracefully();
        }
    }

    public static ComputationServer Instance;
public  void StartComputation(String number) {
    Configure configure = Configure.getConfigureInstance();
    configure.loadProperties();

    String path = configure.getProperties("calc_file_path");
    String name = configure.getProperties("calc_file_name");
    String ext = configure.getProperties("calc_file_ext");
    if (number.equals("1")) {
        Instance.StartComputation(new ProcessOne(), path, name, ext);
    } else if (number.equals("2")) {
        Instance.StartComputation(new ProcessTwo(), path, name, ext);
    } else if (number.equals("3")) {
        Instance.StartComputation(new ProcessThree(), path, name, ext);
    }
}
    public static void start() throws InterruptedException {
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        int computation_port = configure.getIntProperties("computation_port");

        String currentIp = configure.getProperties("current_ip");
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    Instance = new ComputationServer(currentIp, computation_port);
                    Instance.bind();

                } catch (Exception e) {

                }
            }
        }).start();



    }
    public static void main(String[] args) throws InterruptedException {


/*
        while (true){
            SocketChannel channel=(SocketChannel)NettyChannelMap.get("001");
            if(channel!=null){
                AskMsg askMsg=new AskMsg();
                channel.writeAndFlush(askMsg);
            }
            TimeUnit.SECONDS.sleep(10);
        }*/
    }

}

