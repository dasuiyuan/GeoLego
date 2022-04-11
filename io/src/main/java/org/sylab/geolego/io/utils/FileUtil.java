package org.sylab.geolego.io.utils;

import java.io.File;
import java.io.FileInputStream;

/**
 * @author Sui Yuan
 * @description
 * @date 2020/12/1 14:55
 */
public class FileUtil {

    /**
     * 读取全部内容到String内
     *
     * @param fileName 文件路径
     * @return
     */
    public static String readAll(String fileName) {
        String encoding = "UTF-8";
        File file = new File(fileName);
        Long filelength = file.length();
        byte[] filecontent = new byte[filelength.intValue()];
        try (FileInputStream in = new FileInputStream(file)) {
            in.read(filecontent);
            return new String(filecontent, encoding);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
