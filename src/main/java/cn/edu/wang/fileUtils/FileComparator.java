package cn.edu.wang.fileUtils;

import java.io.File;
import java.util.Comparator;

/**
 * Created by wangdechang on 2018/1/3
 */
public class FileComparator implements Comparator {
    public int compare(Object o1, Object o2) {
        if (o1 == null && o2 == null) return 0;
        File file1 = (File) o1;
        File file2 = (File) o2;
        // > -1 == 0 else 1
        try{
            String fileName1 = file1.getName();
            String fileName2 = file2.getName();
            int len1 = fileName1.length();
            int len2 = fileName2.length();
            if(len1 == len2){
                return fileName1.compareTo(fileName2);
            }else{
                if(len1 > len2) return 1;
                else return -1;
            }
        }catch(Exception e){
            e.printStackTrace();
        }

        return 0;
    }
}
