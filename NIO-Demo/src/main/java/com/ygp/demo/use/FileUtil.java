package com.ygp.demo.use;

import java.io.File;
import java.io.IOException;
import java.util.Scanner;

public class FileUtil {
    private final static String MODULE_NAME = "NIO-Demo";

    public static String getMavenResourceDictionary(){
        String relativePath = System.getProperty("user.dir");
        final String mavenResource = "src/main/resources";
        StringBuilder sb = new StringBuilder();
        return sb.append(relativePath).append("/").append(MODULE_NAME).append("/").append(mavenResource).toString();
    }

    public static void createFile(String filePath) {
        File file = new File(filePath);
        if (!file.exists()) {
            try {
                file.createNewFile();
            }catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
