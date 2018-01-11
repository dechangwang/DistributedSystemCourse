package cn.edu.wang.DataAnalysis.Network;

import cn.edu.wang.DataAnalysis.*;
import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchClient;
import cn.edu.wang.DataAnalysis.Network.FileDispatch.FileDispatchServer;
import cn.edu.wang.DataAnalysis.Network.Message.NodeStartJobMsg;
import cn.edu.wang.uploadFile.FileUploadFile;
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

    public void StartComputation(DataAnalysis process, int processID, String filePath) {
        JobFinished = 0;
        ComputationServerHandler.AnalysisJob = process;
        //大文件分割后每一个子计算块放入队列，等待空闲client分发
        LinkedList<String> filePaths = CSVUtil.Split("d:/data/", "tb_call_201202_random", "txt", 10);
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
        try {
            new FileDispatchClient().connect(8081, client.remoteAddress().getHostString(), uploadFile);
        }
        catch (Exception e)
        {
            e.printStackTrace();
        }

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
            }
           // f.channel().closeFuture().sync();

        } finally {
            // 优雅停机
            //  boss.shutdownGracefully();
            // worker.shutdownGracefully();
        }
    }

    public static ComputationServer Instance;

    public static void main(String[] args) throws InterruptedException {
        Instance = new ComputationServer("7.81.11.123", 8080);
        Instance.bind();

        String filePath = "d:/data/tb_call_201202_random.txt";
        while (true) {
            try {
                BufferedReader in = new BufferedReader(new InputStreamReader(
                        System.in));
                String line = in.readLine();
                if (line.equals("start 1")) {
                    Instance.StartComputation(new ProcessOne(), 1, filePath);
                } else if (line.equals("start 2")) {
                    Instance.StartComputation(new ProcessTwo(), 2, filePath);
                } else if (line.equals("start 3")) {
                    Instance.StartComputation(new ProcessThree(), 3, filePath);
                }
            } catch (Exception e) {
            }
        }
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

