package cn.edu.wang.utils;

import cn.edu.wang.Global;

import java.util.ArrayList;

/**
 * Created by wangdechang on 2018/1/9
 */
public class IpUtils {

    public static String nextIp(String deadIp, String[] allIps) {
        int pos = 0;

        //查找宕机节点在集群中的位置
        for (int i = 0; i < allIps.length; i++) {
            if (allIps[i].equals(deadIp)) {
                pos = i;
                break;
            }
        }

        String nextAliveIp = "";
        //查找下一个还存活的节点
        for (int i = 1; i < allIps.length; i++) {
            String isAliveIp = allIps[(pos + i) % allIps.length];
            if (Global.activateIps.contains(isAliveIp)) {
                nextAliveIp = isAliveIp.trim();
                break;
            }

        }
        return nextAliveIp;
    }

    public static String observerIp(String currentIp) {
        String[] allIps = Global.allIps;

        int pos = 0;
        //查找宕机节点在集群中的位置
        for (int i = 0; i < allIps.length; i++) {
            if (allIps[i].trim().equals(currentIp.trim())) {
                pos = i;
                break;
            }
        }
        System.out.println((pos + 1) % allIps.length);
        return allIps[(pos + 1) % allIps.length];

    }

    public static String preIp(String deadIp, String[] allIps) {
        int pos = 0;

        //查找宕机节点在集群中的位置
        for (int i = 0; i < allIps.length; i++) {
            if (allIps[i].equals(deadIp)) {
                pos = i;
                break;
            }
        }

        String preAliveIp = "";
        //查找上一个还存活的节点
        for (int i = 1; i < allIps.length; i++) {
            String isAliveIp = allIps[(pos - i + allIps.length) % allIps.length];
            if (Global.activateIps.contains(isAliveIp)) {
                preAliveIp = isAliveIp.trim();
                break;
            }

        }
        return preAliveIp;
    }


    public static ArrayList<String> neighborIps(String currentIp, String[] allIps) {
        ArrayList<String> arrayList = new ArrayList<>();
        int pos = 0;
        int len = allIps.length;
        //查找当前节点在集群中的位置
        for (int i = 0; i < len; i++) {
            if (allIps[i].equals(currentIp)) {
                pos = i;
                break;
            }
        }

        String pre = "";
        String next = "";
        int preIndex = (pos + 1) % len;
        int backIndex = (pos - 1 + len) % len;
        while (preIndex != backIndex){
            if (Global.activateIps.contains(allIps[preIndex])){
                pre = allIps[preIndex];
            }
            if (Global.activateIps.contains(allIps[backIndex])){
                next = allIps[backIndex];
            }
            if (pre.length() > 0 && next.length() > 0)break;
            if (pre.length() == 0)preIndex = (preIndex - 1 + len) % len;
            if (next.length() == 0)backIndex = (backIndex + 1) % len;

        }
//        String pre = allIps[(pos - 1 + allIps.length) % allIps.length];
//        String next = allIps[(pos + 1) % allIps.length];
        if (next.length() > 0 && pre.length() > 0){
            arrayList.add(pre);
            arrayList.add(next);

        }else if (pre.length() > 0){
            arrayList.add(pre);
        }else if (next.length() > 0){
            arrayList.add(next);
        }
        return arrayList;
    }

    public static ArrayList<String> findOtherActivateNode(String currentIp){
        ArrayList<String> arrayList = new ArrayList<>();
        for (String ip:Global.activateIps){
            if (!ip.equals(currentIp)){
                arrayList.add(ip);
            }
        }
        return arrayList;
    }

}
