package cn.edu.wang.config;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * Created by wangdechang on 2016/4/6.
 */
public class Configure {
    private static Configure configureInstance = null;
    private String configPath = "config.properties";
    private Properties properties = null;

    private Configure() {
        properties = new Properties();
    }

    public static Configure getConfigureInstance() {
        if (configureInstance == null) {
            configureInstance = new Configure();
        }
        return configureInstance;
    }

    public void setConfiguPath(String path) {
        configPath = path;
    }

    public void loadProperties() {
        InputStream is = null;
        try {
            is = new FileInputStream(configPath);
            properties.load(is);

        } catch (FileNotFoundException e) {
            System.err.println("config file not found!");
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("when call load(),exception happened!");
            e.printStackTrace();
        }finally {
            try {
                is.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    public String getProperties(String key){
        String value = properties.getProperty(key);
        return value;
    }

    public Map<String,String> getAllProperties(){
        Map<String,String> propertiesMap = new HashMap<String, String>();
        for(Object k : properties.keySet()){
            String key = (String) k;
            String value = properties.getProperty(key);
            propertiesMap.put(key,value);
        }
        return propertiesMap;
    }
}
