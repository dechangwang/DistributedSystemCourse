package cn.edu.wang.file;

import cn.edu.wang.Global;
import cn.edu.wang.config.Configure;
import cn.edu.wang.fileUtils.MergeFile;
import cn.edu.wang.fileUtils.SplitFile;
import cn.edu.wang.fileUtils.Utils;
import cn.edu.wang.uploadFile.FileUploadClient;
import cn.edu.wang.uploadFile.FileUploadFile;
import cn.edu.wang.utils.IpUtils;
import io.netty.channel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdechang on 2018/1/3
 */

/**
 * 从本地上传文件的Handler
 */
public class FileServerHandler extends SimpleChannelInboundHandler<String> {
    private static final String CR = System.getProperty("line.separator");


    @Override
    public void channelActive(ChannelHandlerContext ctx) {
//        ctx.writeAndFlush("Type the path of the file to upload.\r\n");
        ctx.writeAndFlush("Welcome!!!\r\n");
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
        System.out.println("this is fileUpload server");
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        if(msg.contains("upload")){
            String[] orders = msg.split(" ");
            File file = new File(orders[1]);

            String path = configure.getProperties("save_file_path");
            saveReceivedFile(file, path);

            int blockSize = configure.getIntProperties("block_size");
            if (blockSize == 0) {
                blockSize = 5;
            }
            String splitPath = configure.getProperties("split_file_path");


            //文件分割
            try {
                SplitFile split = new SplitFile(file.getAbsolutePath(), 1024 * 1024 * blockSize, splitPath);
                split.init();
                split.split(split.getDestBlockPath());

                List<String> splitFiles = split.getBlockPath();
                if (splitFiles != null && splitFiles.size() > 0) {
                    String ip = configure.getProperties("ip");
                    //TODO 更改
                    ArrayList<String> arrayList = IpUtils.neighborIps(Global.currentIp,Global.allIps);
                    int server2Port = configure.getIntProperties("server_port");
                    if (arrayList.size() > 0){
                        int index = 0;
                        for (String splitfile : splitFiles) {
                            try {
                                FileUploadFile uploadFile = new FileUploadFile();
                                File moveFile = new File(splitfile);
                                String fileMd5 = moveFile.getName();// 文件名
                                uploadFile.setFile(moveFile);
                                uploadFile.setFile_md5(fileMd5);
                                uploadFile.setStarPos(0);// 文件开始位置
                                new FileUploadClient().connect(server2Port, arrayList.get(index % arrayList.size()), uploadFile);
                                index ++;
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }


            if (file.exists()) {
                if (!file.isFile()) {
                    ctx.writeAndFlush("Not a file : " + file + CR);
                    return;
                }

                ctx.writeAndFlush("Upload successfully" + CR);

            } else {
                ctx.writeAndFlush("File not found: " + file + CR);
            }
        }else if(msg.contains("download")){ //处理文件下载的东西
            String[] orders = msg.split(" ");
            String filenName = orders[1];
            String targetPath = orders[2];
            configure.loadProperties();
            String path = configure.getProperties("save_file_path");
            List<File> files = new ArrayList<>();
            MergeFile.findFiles(path,filenName.trim()+"*",files);
            boolean hasFound = false;
            if (files.size() > 0){
                for (File file: files){
                    if (file.getName().trim().equals(filenName)){
                        Utils.writeFile2TargetPath(file.getAbsolutePath(),targetPath+File.separator+filenName);
                        hasFound = true;
                    }
                }
            }

            if (!hasFound){

            }
        }
        else{

        }
    }


    public void saveReceivedFile(File srcFile, String destPath) {
        BufferedOutputStream outputStream = null;
        BufferedInputStream inputStream = null;
        File directory = new File(destPath);
        if (!directory.exists()) {
            directory.mkdir();
        }
        File destFile = new File(destPath + File.separator + srcFile.getName());
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(destFile, true));
            inputStream = new BufferedInputStream(new FileInputStream(srcFile));
            byte[] flush = new byte[1024];
            //接收长度
            int len = 0;
            while (-1 != (len = inputStream.read(flush))) {
                outputStream.write(flush, 0, len);
            }
            outputStream.flush();
            Utils.close(inputStream);
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(outputStream);
        }
    }

   /* protected void channelRead0(ChannelHandlerContext channelHandlerContext, File file) throws Exception {
        System.out.println("this is server handler");
        saveReceivedFile(file,"D:\\cup");
    }*/

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        super.channelRead(ctx, msg);
        System.out.println("this is channleRead");
    }


}
