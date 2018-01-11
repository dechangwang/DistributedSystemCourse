package cn.edu.wang.communication;

import cn.edu.wang.Global;
import cn.edu.wang.bean.Message;
import com.google.gson.Gson;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.xml.bind.annotation.XmlElementDecl;

/**
 * Created by wangdechang on 2018/1/7
 */
public class CClientHandlerWithCallback extends ChannelInboundHandlerAdapter  {
    private static Logger logger = LoggerFactory.getLogger(CClientHandlerWithCallback.class);
    private String message = "{\"status\":\"message\",\"ip\":\"127.0.0.1\"}";
    private ICommunicationCallback callback;
    Gson gson = new Gson();

    public CClientHandlerWithCallback(String message,ICommunicationCallback callback){
        this.message = message;
        this.callback = callback;
    }

    // 接收server端的消息，并打印出来
    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        ByteBuf result = (ByteBuf) msg;
        byte[] result1 = new byte[result.readableBytes()];
        result.readBytes(result1);
        String message = new String(result1);
        System.out.println("Server said:" + message);
        //将获取的结果进行保存

        Message resMessage = null;
        try{
            resMessage = gson.fromJson(message,Message.class);
        }catch (Exception e){
            e.printStackTrace();
        }
        if (resMessage != null && resMessage.getStatus().equals("searchResult")){
            Global.searchResult.add(resMessage);
            Global.searchResultNumber.incrementAndGet();
            callback.callback(resMessage);
        }
        if (resMessage != null && resMessage.getStatus().equals("deleteResult")){
            System.out.println("deleteResult >>>>");
            Global.deleteResultNumber.incrementAndGet();
            System.out.println(Global.deleteResultNumber.get());
            callback.callback(resMessage);
        }
        result.release();
        ctx.close();
    }

    // 连接成功后，向server发送消息
    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        logger.info("HelloClientIntHandler.channelActive");
        String msg = message;
        ByteBuf encoded = ctx.alloc().buffer(4 * msg.length());
        encoded.writeBytes(msg.getBytes());
        ctx.write(encoded);
        ctx.flush();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        super.exceptionCaught(ctx, cause);
        ctx.close();
    }
}
