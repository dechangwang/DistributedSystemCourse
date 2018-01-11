package cn.edu.wang.DataAnalysis.Network;

import io.netty.channel.Channel;
import io.netty.channel.socket.SocketChannel;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentLinkedQueue;

public class NettyChannelMap {
    public static Map<String, SocketChannel> getClients() {
        return map;
    }

    public static void setMap(Map<String, SocketChannel> map) {
        NettyChannelMap.map = map;
    }

    private static Map<String,SocketChannel> map=new ConcurrentHashMap<String, SocketChannel>();
    private static Map<SocketChannel,String> _socketStringMap=new ConcurrentHashMap<>();
    public static Map<String, SocketChannel> DispatchClientMap = new ConcurrentHashMap<>();
    public static ConcurrentLinkedQueue<SocketChannel> IdleClient = new ConcurrentLinkedQueue<>();

    public static void add(String clientId,SocketChannel socketChannel){

        map.put(clientId,socketChannel);
        _socketStringMap.put(socketChannel, clientId);
    }
    public static SocketChannel get(String clientId){
        return map.get(clientId);
    }
    public static String GetSocketID(SocketChannel socket)
    {
        return _socketStringMap.get(socket);
    }
    public static void remove(SocketChannel socketChannel){
        for (Map.Entry entry:map.entrySet()){
            if (entry.getValue()==socketChannel){
                map.remove(entry.getKey());
            }
        }
    }
}
