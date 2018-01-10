package cn.edu.wang;

import cn.edu.wang.bean.Message;

import java.util.ArrayList;
import java.util.HashSet;

/**
 * Created by wangdechang on 2018/1/7
 */
public class Global {
//    public static ArrayList<String> allIps = new ArrayList<>();
    public volatile static String[] allIps;
    public static HashSet<String> activateIps = new HashSet<>();
    public static HashSet<String> deadIps = new HashSet<>();
    public volatile static String currentIp = "";
    public volatile static boolean heartBeatServer = true;
    public volatile static boolean hearBeatClient = true;
    public static HashSet<Message> searchResult = new HashSet<>();
    public static volatile int searchResultNumber = 0;
    public static volatile String targetpath = "D:\\";
}
