package cn.edu.wang.fileUtils;

import java.io.*;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Vector;

/**
 * Created by wangdechang on 2018/1/2
 */
public class MergeFile {
    /**
     * 文件的合并   （方法一）
     */
    /*public void mergeFile1(String destPath) {
        //创建源
        File dest = new File(destPath);
        //选择流
        BufferedOutputStream bos = null;//输出流
        BufferedInputStream bis = null;//输入流
        try {
            bos = new BufferedOutputStream(new FileOutputStream(dest, true));//表示追加

            for (int i = 0; i < this.blockPath.size(); i++) {
                //读取
                bis = new BufferedInputStream(new FileInputStream
                        (new File(this.blockPath.get(i))));
                //缓冲区
                byte[] flush = new byte[1024];
                //接收长度
                int len = 0;
                while (-1 != (len = bis.read(flush))) {
                    //打印到控制台
                    //System.out.println(new String(flush,0,len));
                    bos.write(flush, 0, len);
                }
                bos.flush();
                Utils.close(bis);
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            Utils.close(bos);
        }
    }

    *//**
     * 文件的合并  （方法二）
     */
    public static void mergerFile(String destPath,String basePath,String prefixFileName) throws IOException {
        //1、创建源
        File dest = new File(destPath);

        //2、选择流
        //SequenceInputStream 表示其他输入流的逻辑串联。它从输入流的有序集合开始，
        //并从第一个输入流开始读取，直到到达文件末尾，接着从第二个输入流读取，依次类推，
        //直到到达包含的最后一个输入流的文件末尾为止。

        SequenceInputStream sis = null;//输入流
        BufferedOutputStream bos = null;//输出源

        List<File> resultList = new ArrayList<File>();
        findFiles(basePath, prefixFileName,resultList);

        if (resultList.size() == 0) {
            System.out.println("No File Fount.");
        } else {
            Collections.sort(resultList,new FileComparator());
            for (int i = 0; i < resultList.size(); i++) {
                System.out.println(resultList.get(i));//显示查找结果。
            }
        }

        //创建一个容器
        Vector<InputStream> vi = new Vector<InputStream>();
        for (int i = 0; i < resultList.size(); i++) {
            vi.add(new BufferedInputStream(
                    new FileInputStream(resultList.get(i))));
        }
        //SequenceInputStream sis = new SequenceInputStream(vi.elements());
        bos = new BufferedOutputStream(new FileOutputStream(dest, true));//表示追加
        sis = new SequenceInputStream(vi.elements());

        //缓冲区
        byte[] flush = new byte[1024];
        //接收长度
        int len = 0;
        while (-1 != (len = sis.read(flush))) {
            //打印到控制台
            //System.out.println(new String(flush,0,len));
            bos.write(flush, 0, len);
        }
        bos.flush();
        Utils.close(sis);
    }


    /**
     * 递归查找文件
     * @param baseDirName  查找的文件夹路径
     * @param targetFileName  需要查找的文件名
     * @param fileList  查找到的文件集合
     */
    public static void findFiles(String baseDirName, String targetFileName, List<File> fileList) {

        File baseDir = new File(baseDirName);       // 创建一个File对象
        if (!baseDir.exists() || !baseDir.isDirectory()) {  // 判断目录是否存在
            System.out.println("文件查找失败：" + baseDirName + "不是一个目录！");
        }
        String tempName = null;
        //判断目录是否存在
        File tempFile;
        File[] files = baseDir.listFiles();
        for (int i = 0; i < files.length; i++) {
            tempFile = files[i];
            if(tempFile.isDirectory()){
                findFiles(tempFile.getAbsolutePath(), targetFileName, fileList);
            }else if(tempFile.isFile()){
                tempName = tempFile.getName();
                if(wildcardMatch(targetFileName, tempName)){
                    // 匹配成功，将文件名添加到结果集
                    fileList.add(tempFile.getAbsoluteFile());
                }
            }
        }
    }


    /**
     * 通配符匹配
     * @param pattern    通配符模式
     * @param str    待匹配的字符串
     * @return    匹配成功则返回true，否则返回false
     */
    private static boolean wildcardMatch(String pattern, String str) {
        int patternLength = pattern.length();
        int strLength = str.length();
        int strIndex = 0;
        char ch;
        for (int patternIndex = 0; patternIndex < patternLength; patternIndex++) {
            ch = pattern.charAt(patternIndex);
            if (ch == '*') {
                //通配符星号*表示可以匹配任意多个字符
                while (strIndex < strLength) {
                    if (wildcardMatch(pattern.substring(patternIndex + 1),
                            str.substring(strIndex))) {
                        return true;
                    }
                    strIndex++;
                }
            } else if (ch == '?') {
                //通配符问号?表示匹配任意一个字符
                strIndex++;
                if (strIndex > strLength) {
                    //表示str中已经没有字符匹配?了。
                    return false;
                }
            } else {
                if ((strIndex >= strLength) || (ch != str.charAt(strIndex))) {
                    return false;
                }
                strIndex++;
            }
        }
        return (strIndex == strLength);
    }
}
