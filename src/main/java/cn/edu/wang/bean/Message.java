package cn.edu.wang.bean;

import java.io.Serializable;
import java.util.ArrayList;

/**
 * Created by wangdechang on 2018/1/9
 */
public class Message implements Serializable {
    private String status;
    private String ip;
    private String order;
    private String fileName;
    private String targetpath;
    private ArrayList<String> fileNameList;

    public Message() {
    }

    public Message(String status, String ip) {
        this.status = status;
        this.ip = ip;
    }

    public Message(String order, String fileName, String targetpath) {
        this.order = order;
        this.fileName = fileName;
        this.targetpath = targetpath;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getIp() {
        return ip;
    }

    public void setIp(String ip) {
        this.ip = ip;
    }

    public String getOrder() {
        return order;
    }

    public void setOrder(String order) {
        this.order = order;
    }

    public String getFileName() {
        return fileName;
    }

    public void setFileName(String fileName) {
        this.fileName = fileName;
    }

    public String getTargetpath() {
        return targetpath;
    }

    public void setTargetpath(String targetpath) {
        this.targetpath = targetpath;
    }

    public ArrayList<String> getFileNameList() {
        return fileNameList;
    }

    public void setFileNameList(ArrayList<String> fileNameList) {
        this.fileNameList = fileNameList;
    }
}
