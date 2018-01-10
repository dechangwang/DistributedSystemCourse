package cn.edu.wang.file;

import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import cn.edu.wang.communication.CommunicateWithCallbackClient;
import cn.edu.wang.config.Configure;
import cn.edu.wang.fileUtils.MergeFile;
import cn.edu.wang.fileUtils.SplitFile;
import cn.edu.wang.fileUtils.Utils;
import cn.edu.wang.uploadFile.FileUploadClient;
import cn.edu.wang.uploadFile.FileUploadFile;
import cn.edu.wang.utils.DownloadFile;
import cn.edu.wang.utils.IpUtils;
import com.google.gson.Gson;
import io.netty.channel.*;

import java.io.*;
import java.nio.file.Files;
import java.util.*;

/**
 * Created by wangdechang on 2018/1/3
 */

/**
 * 从本地上传文件的Handler
 */
public class FileServerHandler extends SimpleChannelInboundHandler<String> {
    private static final String CR = System.getProperty("line.separator");
    Gson gson = new Gson();


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
        if (msg.contains("upload")) {
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
                    ArrayList<String> arrayList = IpUtils.neighborIps(Global.currentIp, Global.allIps);
                    int server2Port = configure.getIntProperties("server_port");
                    if (arrayList.size() > 0) {
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
                                index++;
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
        } else if (msg.contains("download")) { //处理文件下载的东西
            String[] orders = msg.split(" ");
            String filenName = orders[1];
            String targetPath = orders[2];
            if (targetPath!= null && targetPath.length() > 0){
                Global.targetpath = targetPath;
            }
            configure.loadProperties();
            String path = configure.getProperties("save_file_path");
            List<File> files = new ArrayList<>();
            MergeFile.findFiles(path, filenName.trim() + "*", files);
            boolean hasFound = false;
            if (files.size() > 0) {
                for (File file : files) {
                    if (file.getName().trim().equals(filenName)) {
                        Utils.writeFile2TargetPath(file.getAbsolutePath(), targetPath + File.separator + filenName);
                        files.remove(file);
                        hasFound = true;
                        break;
                    }
                }
            }

            if (!hasFound) {
                searchFile(files,filenName,ctx);
            }
        } else {

        }
    }

    private void searchFile(List<File> files,String fileName,ChannelHandlerContext ctx) {
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        int port = configure.getIntProperties("comm_port");
        ArrayList<String> activateIps = IpUtils.findOtherActivateNode(Global.currentIp);
        ChannelFutureListener channelFutureListener = new ChannelFutureListener() {
            @Override
            public void operationComplete(ChannelFuture channelFuture) throws Exception {
                //TODO 在这里处理请求处理结束的结果
                if (Global.searchResultNumber >= activateIps.size()){
                    Global.searchResultNumber = 0;
                }
                selectFileDownload(ctx,files);

            }
        };
        Message message = new Message("search",Global.currentIp);
        message.setFileName(fileName);
        message.setOrder("search");
        for (String ip : activateIps) {
            CommunicateWithCallbackClient client = new CommunicateWithCallbackClient(channelFutureListener);
            try {
                client.connect(ip,port,gson.toJson(message));
            } catch (Exception e) {
                e.printStackTrace();
            }

        }

    }


    private void selectFileDownload(ChannelHandlerContext ctx,List<File> files){

        //filename ip
        HashMap<String,String> otherNodeFiles = new HashMap<>();
        HashSet<String> allFileName = new HashSet<>();

        //本地机器上存在的文件
        HashSet<String> localFiles = new HashSet<>();
        for (File f:files){
            localFiles.add(f.getName());
            allFileName.add(f.getName().trim());
        }
        HashSet<Message> res = Global.searchResult;
        for(Message message:res){
            if (message != null && message.getFileNameList() != null && message.getFileNameList().size() > 0){
                for (String name:message.getFileNameList()){
                    int fileSeparator = name.lastIndexOf(File.separator);
                    String subName = name.substring(fileSeparator + 1,name.length()).trim();
                    if (!allFileName.contains(subName)){
                        allFileName.add(subName);
                        otherNodeFiles.put(name,message.getIp());
                    }
                }

            }
        }
        Global.searchResult.clear();

        //接下來需要处理文件的下载
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        String projectName = configure.getProperties("DistributedFileSystem_jar");
        String downloadPort = configure.getProperties("download_file_port");
        String tempPath = configure.getProperties("temp_path");
        File file = new File(tempPath);
        if (!file.exists()){
            file.mkdir();
        }
        for (Map.Entry<String,String> entry:otherNodeFiles.entrySet()){
            String name = entry.getKey();
            String ip = entry.getValue();
            String postfixName = name.substring(name.indexOf(projectName+File.separator) + projectName.length() + 1,name.length());
            String url = "http://"+ip.trim()+":"+ downloadPort + "/" + postfixName;
            int fileSeparator = name.lastIndexOf(File.separator);
            String subName = name.substring(fileSeparator + 1,name.length()).trim();
            String path = tempPath + File.separator + File.separator + subName;
            if(DownloadFile.downloadFile(url,path)){
                files.add(new File(path));
            }

        }

        if (files != null && files.size() >0){
            try {
                MergeFile.mergerFile(Global.targetpath,files);
            } catch (IOException e) {
                e.printStackTrace();
            }
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
