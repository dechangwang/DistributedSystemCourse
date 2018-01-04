package cn.edu.wang.file;

import cn.edu.wang.fileUtils.Utils;
import io.netty.channel.*;

import java.io.*;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileServerHandler extends SimpleChannelInboundHandler<String>{
    private static final String CR = System.getProperty("line.separator");


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
        ctx.writeAndFlush("HELLO: Type the path of the file to upload.\r\n");
    }

    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause)
            throws Exception {
        cause.printStackTrace();

        if (ctx.channel().isActive()) {
            ctx.writeAndFlush("ERR: " +
                    cause.getClass().getSimpleName() + ": " +
                    cause.getMessage() + '\n').addListener(ChannelFutureListener.CLOSE);
        }
        ctx.close();
    }

    protected void channelRead0(ChannelHandlerContext ctx, String msg) throws Exception {
        System.out.println("this is server");
        File file = new File(msg);
        saveReceivedFile(file,"D:\\cup");
        if (file.exists()) {
            if (!file.isFile()) {
                ctx.writeAndFlush("Not a file : " + file + CR);
                return;
            }
//            ctx.write(file + " " + file.length() + CR);
//            RandomAccessFile randomAccessFile = new RandomAccessFile(msg, "r");
//            FileRegion region = new DefaultFileRegion(
//                    randomAccessFile.getChannel(), 0, randomAccessFile.length());
//            ctx.write(region);
//            ctx.writeAndFlush(CR);
//            randomAccessFile.close();
        } else {
            ctx.writeAndFlush("File not found: " + file + CR);
        }
    }


    public void saveReceivedFile(File srcFile,String destPath){
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        File destFile = new File(destPath+File.separator+srcFile.getName());
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile,true));
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            byte[] flush = new byte[1024];
            //接收长度
            int len = 0;
            while (-1 != (len = inputStream.read(flush))){
                outputStream.write(flush,0,len);
            }
            outputStream.flush();
            Utils.close(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }finally {
            Utils.close(outputStream);
        }
    }

    protected void channelRead0(ChannelHandlerContext channelHandlerContext, File file) throws Exception {
        System.out.println("this is server handler");
        saveReceivedFile(file,"D:\\cup");
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("this is channleRead");
    }
}
