package util;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

/**
 * @author : suiyuan
 * @description :
 * @date : Created in 2020-04-23 10:27
 * @modified by :
 **/
public class FileUtils {
    public static void write2File(String filePath, byte[] bytes) {
        File file = new File(filePath);
        try {
            file.createNewFile();
            if (bytes == null) {
                return;
            }
            FileOutputStream fileOutputStream = new FileOutputStream(file);
            fileOutputStream.write(bytes);
            fileOutputStream.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
