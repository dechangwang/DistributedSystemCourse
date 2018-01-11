package cn.edu.wang.DataAnalysis.Network.FileDispatch;

import cn.edu.wang.DataAnalysis.Network.ComputationServer;
import cn.edu.wang.DataAnalysis.Network.Message.LoginMsg;
import cn.edu.wang.DataAnalysis.Network.Message.NodeFileReceivedMsg;
import cn.edu.wang.DataAnalysis.Network.NettyChannelMap;
import cn.edu.wang.uploadFile.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;
import java.util.concurrent.Callable;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileDispatchClientHandler extends ChannelInboundHandlerAdapter  {
    private int byteRead;
    private volatile int start = 0;
    private volatile int lastLength = 0;
    public RandomAccessFile randomAccessFile;
    private FileUploadFile fileUploadFile;

    public FileDispatchClientHandler(FileUploadFile ef) {
        if (ef.getFile().exists()) {
            if (!ef.getFile().isFile()) {
                System.out.println("Not a file :" + ef.getFile());
                return;
            }
        }
        this.fileUploadFile = ef;
    }

    public void channelActive(ChannelHandlerContext ctx) {
        try {
            randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
            randomAccessFile.seek(fileUploadFile.getStarPos());
            lastLength = (int) randomAccessFile.length() / 10;
            fileUploadFile.setTotalSize(randomAccessFile.length());
            byte[] bytes = new byte[lastLength];
            if ((byteRead = randomAccessFile.read(bytes)) != -1) {
                fileUploadFile.setEndPos(byteRead);
                fileUploadFile.setBytes(bytes);
                ctx.writeAndFlush(fileUploadFile);
            } else {
                /*fileUploadFile.setEndPos(-1);
                fileUploadFile.setBytes(bytes);
                ctx.writeAndFlush(fileUploadFile);*/
                System.out.println("文件已经读完");
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException i) {
            i.printStackTrace();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Integer) {
            start = (Integer) msg;
            if (start != -1) {
                randomAccessFile = new RandomAccessFile(fileUploadFile.getFile(), "r");
                randomAccessFile.seek(start);
              //  System.out.println("块儿长度：" + (randomAccessFile.length() / 10));
              //  System.out.println("长度：" + (randomAccessFile.length() - start));
                int a = (int) (randomAccessFile.length() - start);
                int b = (int) (randomAccessFile.length() / 10);
                if (a < b) {
                    lastLength = a;
                }
                byte[] bytes = new byte[lastLength];
               // System.out.println("-----------------------------" + bytes.length);
                if ((byteRead = randomAccessFile.read(bytes)) != -1 && (randomAccessFile.length() - start) > 0) {
                  //  System.out.println("byte 长度：" + bytes.length);
                    fileUploadFile.setEndPos(byteRead);
                    fileUploadFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(fileUploadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                } else {
                    fileUploadFile.setEndPos(0);
                    fileUploadFile.setBytes(bytes);
                    try {
                        ctx.writeAndFlush(fileUploadFile);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    randomAccessFile.close();
                    //ctx.close();
                  //  System.out.println("文件已经读完--------" + byteRead);
                }
            }
        }
        else if(msg instanceof NodeFileReceivedMsg)
        {
            NodeFileReceivedMsg fileMsg = (NodeFileReceivedMsg)msg;
            ComputationServer.Instance.OnNodeFileReceived(fileMsg.getClientId(), fileMsg.FilePath);
            ctx.close();
        }
    }



    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }

}
