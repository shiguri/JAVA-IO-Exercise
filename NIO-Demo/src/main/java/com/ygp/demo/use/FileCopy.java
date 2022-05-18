package com.ygp.demo.use;

import java.io.*;
import java.nio.ByteBuffer;
import java.nio.channels.ReadableByteChannel;
import java.nio.channels.WritableByteChannel;
import java.util.Scanner;

/**
 * 使用 Channel 来复制文件内容
 */
public class FileCopy {
    public static void main(String[] args) {
        final String fileDictionary = FileUtil.getMavenResourceDictionary();

        boolean fileReading = true;
        FileInputStream inputStream = null;
        String inputFileName = null;
        while (fileReading) {
            String fileName = getFileName();
            try {
                inputStream = new FileInputStream(fileDictionary + "/" + fileName);
            }catch (FileNotFoundException e) {
                System.out.println(fileName + " 不存在！请重新输入");
                continue;
            }

            // 读取文件流成功，退出。
            inputFileName = fileName;
            fileReading = false;
        }

        String outputFileName = fileDictionary + "/" + "(cp)" + inputFileName;
        FileUtil.createFile(outputFileName);

        FileOutputStream outputStream = null;
        try {
            outputStream = new FileOutputStream(outputFileName);
        }catch (FileNotFoundException e){
            e.printStackTrace();
        }

        try {
            copyFromChannels(inputStream.getChannel(), outputStream.getChannel());
            System.out.println("复制完成");
        }catch (IOException e) {
            e.printStackTrace();
        }

        try {
            inputStream.close();
            outputStream.close();
        }catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static String getFileName() {
        Scanner scanner = new Scanner(System.in);
        String next = scanner.next();
        return next.trim();
    }

    private static void copyFromChannels(ReadableByteChannel source, WritableByteChannel destination) throws IOException {
        final int capacity = 20 * 1024;
        ByteBuffer byteBuffer = ByteBuffer.allocate(capacity);


        while(source.read(byteBuffer) != -1) {
            /**
             * flip 方法将buffer从写模式切换到读模式。
             * 原理涉及到 position 和 limit 的变换
             */
            byteBuffer.flip();
            while(byteBuffer.hasRemaining()) {
                destination.write(byteBuffer);
            }

            //写完数据了，清空byteBuffer
            byteBuffer.clear();
        }


    }

}
