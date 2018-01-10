package cn.edu.wang.communication;

import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import cn.edu.wang.config.Configure;
import cn.edu.wang.fileUtils.MergeFile;
import cn.edu.wang.heartBeat.HeartBeatsClient;
import cn.edu.wang.log.Log;
import cn.edu.wang.utils.IpUtils;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElementDecl;
import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by wangdechang on 2018/1/7
 */
public class CServerHandler extends ChannelInboundHandlerAdapter{
    private static Logger logger = LoggerFactory
            .getLogger(CServerHandler.class);
    Gson gson = new Gson();

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg)
            throws Exception {
        logger.info("HelloServerInHandler.channelRead");
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        // msg中存储的是ByteBuf类型的数据，把数据读取到byte[]中
        result.readBytes(result1);
        String resultStr = new String(result1);
        // 接收并打印客户端的信息
        System.out.println("Client said:" + resultStr);
        String response = "received your message!";

        try{
            Message message = null;
            if (resultStr.equals("start connect")){
                message = gson.fromJson("{\"status\":\"message\",\"ip\":\"127.0.0.1\"}",Message.class);
            }else{
                message = gson.fromJson(resultStr,Message.class);
            }
            //接收到有节点宕机的信息
            if (message.getStatus().equals("dead")){
                String ip = message.getIp();
                if (ip.indexOf(":") > 0){
                    ip = ip.substring(ip.indexOf(":")+1,ip.length());
                }
                Global.activateIps.remove(ip);
                Global.deadIps.add(ip);
                for(String s:Global.deadIps){
                    System.out.println(s);
                }
                System.out.println(ip);
            }
            //接收节点重新加入的信息
            else if (message.getStatus().equals("relive")){
                System.out.println("准备节点监听");
                String ip = message.getIp();
                Global.activateIps.add(ip.trim());
                if (Global.deadIps.contains(ip.trim())){
                    Global.deadIps.remove(ip.trim());
                }

                //查找重新加入的节点在集群中上一个节点的位置 IP
                String preIp = IpUtils.preIp(ip,Global.allIps);
                System.out.println("relive");
                System.out.println(preIp);
                for (String s:Global.activateIps){
                    System.out.print("\t" + s);
                }
                System.out.println("===============");
                System.out.println("currentIp + "+Global.currentIp);
                //TODO 测试Wie Global.currentIp.equals("8092")
//                if (preIp.length() > 0 && Global.currentIp.equals(preIp)){
//                    //TODO 这里在实际部署时，nextAliveIp是Ip，端口需要读取配置文件
//                    Configure configure = Configure.getConfigureInstance();
//                    configure.loadProperties();
//                    int heart_beat_port = configure.getIntProperties("heart_beat_port");
//                    HeartBeatsClient client = new HeartBeatsClient(heart_beat_port,preIp);
//                    client.start();
//                }
            }else if(message.getStatus().equals("search")){
                response = searchFile(message.getFileName());
            }
        }catch (Exception e){
            e.printStackTrace();
            Log.log("communication：服务端接收消息异常");
        }

        // 释放资源
        result.release();

        // 向客户端发送消息

        // 在当前场景下，发送的数据必须转换成ByteBuf数组
        ByteBuf encoded = ctx.alloc().buffer(4 * response.length());
        encoded.writeBytes(response.getBytes());
        ctx.write(encoded);
        ctx.flush();
    }

    private String searchFile(String fileName){
        String response = "";
        Configure configure = Configure.getConfigureInstance();
        configure.loadProperties();
        String path = configure.getProperties("save_file_path");
        List<File> files = new ArrayList<>();
        MergeFile.findFiles(path, fileName.trim() + "*", files);
        if (files!= null && files.size() > 0){
            Message message = new Message("searchResult",Global.currentIp);
            message.setFileName(fileName);
            ArrayList<String> fileNames = new ArrayList<>();
            for (File file:files){
                if (!file.getName().equals(fileName)){
                    fileNames.add(file.getAbsolutePath());
                }
            }
            message.setFileNameList(fileNames);
            message.setIp(Global.currentIp);
            response = gson.toJson(message);
        }

        return response;
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx) throws Exception {
        ctx.flush();
    }
}
