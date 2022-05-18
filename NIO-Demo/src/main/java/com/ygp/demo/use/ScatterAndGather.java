package com.ygp.demo.use;

import com.sun.org.apache.xml.internal.security.utils.UnsyncByteArrayOutputStream;
import sun.text.normalizer.UTF16;

import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.nio.ByteBuffer;
import java.nio.channels.FileChannel;
import java.nio.channels.GatheringByteChannel;
import java.nio.channels.ScatteringByteChannel;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Arrays;
import java.util.Date;
import java.util.Random;
import java.util.Scanner;

/**
 * JAVA NIO 中，提供了分散(Scatter)和聚集(Gather)的功能。
 * Scatter 读取，指从单个 Channel 读取多个缓冲区。
 * Gather 写入，指从多个缓冲区写入单个 Channel。
 */
public class ScatterAndGather {
    private final static String fileName = "gatherData.txt";
    // yyyy-MM-dd 共 10个字符，共20Bytes
    private final static int capacity1 = 20;
    private final static int capacity2 = 1024;

    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        String data = scanner.nextLine();
        gatherData(data);
        scatterData();
    }

    private static void gatherData(String str) {
        // 第一个buffer用来写入日期作为header
        ByteBuffer buffer1 = ByteBuffer.allocate(capacity1);
        // 第二个buffer用来写入存放的String
        ByteBuffer buffer2 = ByteBuffer.allocate(capacity2);

        String basePath = FileUtil.getMavenResourceDictionary();
        String filePath = basePath + "/" + fileName;
        FileUtil.createFile(filePath);

        String date = new SimpleDateFormat("yyyy-MM-dd").format(new Date(System.currentTimeMillis()));

        char[] chars = str.toCharArray();

        buffer1.asCharBuffer().put(date);
        buffer2.asCharBuffer().put(str);
        try {
            GatheringByteChannel channel = new FileOutputStream(filePath).getChannel();
            // 多个缓冲区写入一个Channel。
            channel.write(new ByteBuffer[]{buffer1, buffer2});
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void scatterData() {
        ByteBuffer buffer1 = ByteBuffer.allocate(capacity1);
        ByteBuffer buffer2 = ByteBuffer.allocate(capacity2);

        String basePath = FileUtil.getMavenResourceDictionary();
        String filePath = basePath + "/" + fileName;
        FileUtil.createFile(filePath);

        try {
            ScatteringByteChannel channel = new FileInputStream(filePath).getChannel();
            channel.read(new ByteBuffer[]{buffer1, buffer2});
        }catch (IOException e) {
            e.printStackTrace();
        }

        buffer1.rewind();
        buffer2.rewind();

        String bufferOneVal = buffer1.asCharBuffer().toString();
        String bufferTwoVal = buffer2.asCharBuffer().toString();
        System.out.println(bufferOneVal);
        System.out.println(bufferTwoVal);
    }
}
