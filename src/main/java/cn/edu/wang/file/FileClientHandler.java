package cn.edu.wang.file;

import io.netty.channel.SimpleChannelInboundHandler;


import java.io.File;
import java.io.FileOutputStream;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileClientHandler extends SimpleChannelInboundHandler {
    private String dest;

    /**
     *
     * @param dest 文件生成路径
     */
    public FileClientHandler(String dest) {
        this.dest = dest;
    }



    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object msg) throws Exception {
        System.out.println(msg);
        System.out.println("this is client");
        channelHandlerContext.writeAndFlush("hhhhhh");
//        File file = new File(dest);
//        if (!file.exists()) {
//            file.createNewFile();
//        }
//
//        FileOutputStream fos = new FileOutputStream(file);
//
//        fos.write(((String)msg).getBytes());
//        fos.close();

    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
//        super.channelActive(ctx);
        System.out.println("connect");
        File file = new File(dest);
        ctx.write("hello! this is clinet");
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        cause.printStackTrace();
        ctx.close();
    }
}
