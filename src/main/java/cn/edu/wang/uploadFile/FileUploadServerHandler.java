package cn.edu.wang.uploadFile;

import cn.edu.wang.config.Configure;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import java.io.*;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileUploadServerHandler extends ChannelInboundHandlerAdapter {
    private int byteRead;
    private volatile int start = 0;
    private String file_dir = "D:\\saveFiles";

    public FileUploadServerHandler() {
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        file_dir = configure.getProperties("copy_file_path");
        File copyDir = new File(file_dir);
        if (!copyDir.exists()){
            copyDir.mkdir();
            copyDir.mkdir();
        }
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof FileUploadFile) {
            FileUploadFile ef = (FileUploadFile) msg;
            byte[] bytes = ef.getBytes();
            byteRead = ef.getEndPos();
            String md5 = ef.getFile_md5();//文件名

            String path = file_dir + File.separator + md5;
            File file = new File(path);
            /*RandomAccessFile randomAccessFile = new RandomAccessFile(file, "rw");
            randomAccessFile.seek(start);
            randomAccessFile.write(bytes);*/

            BufferedOutputStream outputStream = new BufferedOutputStream(new FileOutputStream(file, true));
            outputStream.write(bytes, 0, byteRead);
            outputStream.close();

            start = start + byteRead;
            if (byteRead > 0) {
                ctx.writeAndFlush(start);
            } else {
                System.out.println("close");
//                ctx.writeAndFlush(start);
//                randomAccessFile.close();

            }
            if (byteRead <= 0) {
//                randomAccessFile.close();
                ctx.close();
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
