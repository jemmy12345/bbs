package com.ruoyi.common.core.utils;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

/**
 * Properties工具类
 * 
 * @author ruoyi
 */
public class PropertiesUtil {

    public static String loadProperties(String url, String propName, String defaultValue) throws FileNotFoundException, IOException {
        Properties prop = new Properties();
        prop.load(new FileInputStream(url));
        String returnValue = prop.getProperty(propName, defaultValue);
        return returnValue;
    }

    public static String getProperty(String fileName, String key) {
        Properties prop = new Properties();
        InputStream in = PropertiesUtil.class.getResourceAsStream("/" + fileName);
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        String value = prop.getProperty(key);
        return value;
    }

    public Properties getProperties(String fileName) {
        Properties prop = new Properties();
        InputStream in = PropertiesUtil.class.getResourceAsStream("/" + fileName);
        try {
            prop.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (in != null) {
                try {
                    in.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        return prop;
    }
}
