package cn.edu.wang.DataAnalysis.Network.FileDispatch;

import cn.edu.wang.DataAnalysis.Network.ComputationServer;
import cn.edu.wang.DataAnalysis.Network.Message.LoginMsg;
import cn.edu.wang.DataAnalysis.Network.Message.NodeFileReceivedMsg;
import cn.edu.wang.DataAnalysis.Network.NettyChannelMap;
import cn.edu.wang.config.Configure;
import cn.edu.wang.uploadFile.FileUploadFile;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import io.netty.channel.socket.SocketChannel;

import java.io.*;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileDispatchServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir;// = "D:\\data\\output";

    public FileDispatchServerHandler()
    {
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        file_dir = configure.getProperties("save_file_path");//"d:/data/tb_call_201202_random_output_2.xlsx";
    }
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileUploadFile) {
            FileUploadFile ef = (FileUploadFile) msg;
           // System.out.println("开始接受文件："  + ef.getFile_md5());

            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();//文件名
            String path = file_dir + File.separator + md5;
            File file = new File(path);
            /*RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);*/

            BufferedOutputStream outputStream;
            if(start == 0)
                outputStream= new BufferedOutputStream(new FileOutputStream(file,false));
            else
                outputStream= new BufferedOutputStream(new FileOutputStream(file,true));

            outputStream.write(bytes,0,byteRead);
            outputStream.close();

            start = start + byteRead;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);
            } else {
                start = 0;
                System.out.println("文件 "+ef.getFile_md5()+" 接收成功");
                NodeFileReceivedMsg fileMsg = new NodeFileReceivedMsg();
                fileMsg.FilePath = path;
                ctx.writeAndFlush(fileMsg);
               // ctx.writeAndFlush("asdasdasdasd");
//                ctx.writeAndFlush(start);
//                randomAccessFile.close();

            }
            if (byteRead <=0){
//                randomAccessFile.close();
               // ctx.close();
            }
           /* ctx.writeAndFlush(start);
            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file,true));
            outputStream.write(bytes,0,byteRead);
            if(byteRead <= 0){
                outputStream.close();
                ctx.close();
            }*/

        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        cause.printStackTrace();
        ctx.close();
    }
}
